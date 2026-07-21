package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "VpnViewModel"
    private val database = VpnDatabase.getDatabase(application)
    private val repository = VpnRepository(application, database.vpnProfileDao())

    // All VPN Profiles list
    val profiles: StateFlow<List<VpnProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Connection logs
    val logs: StateFlow<List<VpnConnectionLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Profile
    private val _selectedProfile = MutableStateFlow<VpnProfile?>(null)
    val selectedProfile: StateFlow<VpnProfile?> = _selectedProfile.asStateFlow()

    // Connection State
    val connectionState: StateFlow<Tunnel.State> = repository.connectionState

    // Live statistics
    val bytesUploaded: StateFlow<Long> = repository.bytesUploaded
    val bytesDownloaded: StateFlow<Long> = repository.bytesDownloaded
    val currentSpeedUp: StateFlow<Long> = repository.currentSpeedUp
    val currentSpeedDown: StateFlow<Long> = repository.currentSpeedDown

    // Active connection timer
    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    // Server metrics state flow (profileId -> ServerMetrics)
    private val _serverMetrics = MutableStateFlow<Map<Int, ServerMetrics>>(emptyMap())
    val serverMetrics: StateFlow<Map<Int, ServerMetrics>> = _serverMetrics.asStateFlow()

    // Refreshing state
    private val _isRefreshingMetrics = MutableStateFlow(false)
    val isRefreshingMetrics: StateFlow<Boolean> = _isRefreshingMetrics.asStateFlow()

    // UI Error / Status Message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Automatically select the first profile once loaded & initialize server metrics
        viewModelScope.launch {
            profiles.collect { list ->
                if (_selectedProfile.value == null && list.isNotEmpty()) {
                    _selectedProfile.value = list.first()
                }

                // Initialize server metrics for profiles if they don't exist yet
                val currentMetrics = _serverMetrics.value
                val newMetrics = currentMetrics.toMutableMap()
                var updated = false
                for (profile in list) {
                    if (!newMetrics.containsKey(profile.id)) {
                        val baseLoad = when (profile.countryCode) {
                            "US" -> 34
                            "DE" -> 45
                            "JP" -> 68
                            "SG" -> 18
                            else -> (20..85).random()
                        }
                        val basePing = when (profile.countryCode) {
                            "US" -> 42
                            "DE" -> 118
                            "JP" -> 185
                            "SG" -> 88
                            else -> (30..250).random()
                        }
                        newMetrics[profile.id] = ServerMetrics(pingMs = basePing, loadPercentage = baseLoad)
                        updated = true
                    }
                }
                if (updated) {
                    _serverMetrics.value = newMetrics
                }
            }
        }

        // Monitor connection state to start/stop the timer
        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == Tunnel.State.UP) {
                    startTimer()
                } else {
                    stopTimer()
                }
            }
        }
    }

    fun selectProfile(profile: VpnProfile) {
        if (connectionState.value == Tunnel.State.DOWN) {
            _selectedProfile.value = profile
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Cannot change servers while connected. Please disconnect first."
        }
    }

    fun toggleConnection() {
        val profile = _selectedProfile.value
        if (profile == null) {
            _errorMessage.value = "Please select a VPN profile first."
            return
        }

        val currentState = connectionState.value
        if (currentState == Tunnel.State.DOWN) {
            _statusMessage.value = "Connecting to ${profile.name}..."
            _errorMessage.value = null
            repository.connect(
                profile = profile,
                onSuccess = {
                    _statusMessage.value = "Connected to ${profile.name}!"
                },
                onError = { err ->
                    _statusMessage.value = null
                    _errorMessage.value = err
                }
            )
        } else {
            _statusMessage.value = "Disconnecting..."
            repository.disconnect(profile.name, _durationSeconds.value)
            _statusMessage.value = "Disconnected"
        }
    }

    fun importConfigFromText(name: String, configText: String, countryCode: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || configText.isBlank()) {
                onComplete(false, "Name and configuration text cannot be empty.")
                return@launch
            }

            try {
                // Validate if it has minimal wireguard interface tag
                if (!configText.contains("[Interface]", ignoreCase = true)) {
                    onComplete(false, "Invalid configuration: Missing [Interface] section.")
                    return@launch
                }

                val newProfile = VpnProfile(
                    name = name,
                    configText = configText,
                    countryCode = countryCode.uppercase(Locale.ROOT),
                    isCustom = true
                )

                repository.insertProfile(newProfile)
                _selectedProfile.value = newProfile
                onComplete(true, "Configuration imported successfully!")
            } catch (e: Exception) {
                Log.e(tag, "Failed to import configuration", e)
                onComplete(false, "Failed to import: ${e.localizedMessage}")
            }
        }
    }

    fun importConfigFromFile(name: String, inputStream: InputStream, countryCode: String, onComplete: (Boolean, String) -> Unit) {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            importConfigFromText(name, sb.toString(), countryCode, onComplete)
        } catch (e: Exception) {
            onComplete(false, "Failed to read file: ${e.localizedMessage}")
        }
    }

    fun deleteProfile(profile: VpnProfile) {
        viewModelScope.launch {
            if (_selectedProfile.value?.id == profile.id) {
                _selectedProfile.value = profiles.value.firstOrNull { it.id != profile.id }
            }
            repository.deleteProfile(profile)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        _durationSeconds.value = 0L
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Byte Formatting Helper
    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1] + ""
        return String.format(Locale.ROOT, "%.2f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    // Speed Formatting Helper
    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec < 1024) return "$bytesPerSec B/s"
        val exp = (Math.log(bytesPerSec.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1] + ""
        return String.format(Locale.ROOT, "%.1f %sB/s", bytesPerSec / Math.pow(1024.0, exp.toDouble()), pre)
    }

    // Duration Formatting Helper
    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.ROOT, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", mins, secs)
        }
    }

    // Flag emoji getter based on country code
    fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    fun refreshMetrics() {
        if (_isRefreshingMetrics.value) return
        viewModelScope.launch {
            _isRefreshingMetrics.value = true
            
            // Staggered refresh animation
            val currentList = profiles.value
            for (profile in currentList) {
                // Set ping to null temporarily to show a beautiful loader
                _serverMetrics.value = _serverMetrics.value.toMutableMap().apply {
                    this[profile.id] = ServerMetrics(pingMs = null, loadPercentage = (10..95).random())
                }
                // Small delay to simulate ping speed
                delay((150..350).random().toLong())
                
                // Update with new random but realistic ping and load
                val newPing = when (profile.countryCode) {
                    "US" -> (35..55).random()
                    "DE" -> (110..130).random()
                    "JP" -> (175..195).random()
                    "SG" -> (80..100).random()
                    else -> (25..240).random()
                }
                val newLoad = (15..95).random()
                
                _serverMetrics.value = _serverMetrics.value.toMutableMap().apply {
                    this[profile.id] = ServerMetrics(pingMs = newPing, loadPercentage = newLoad)
                }
            }
            
            _isRefreshingMetrics.value = false
        }
    }
}

data class ServerMetrics(
    val pingMs: Int?,
    val loadPercentage: Int
)
