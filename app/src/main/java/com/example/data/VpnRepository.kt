package com.example.data

import android.content.Context
import android.util.Log
import com.example.vpn.WireGuardTunnel
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class VpnRepository(
    private val context: Context,
    private val profileDao: VpnProfileDao
) {
    private val tag = "VpnRepository"
    private var backend: GoBackend? = null
    private val tunnel = WireGuardTunnel("DvalTunnel")

    private val _connectionState = MutableStateFlow<Tunnel.State>(Tunnel.State.DOWN)
    val connectionState: StateFlow<Tunnel.State> = _connectionState.asStateFlow()

    private val _bytesUploaded = MutableStateFlow<Long>(0L)
    val bytesUploaded: StateFlow<Long> = _bytesUploaded.asStateFlow()

    private val _bytesDownloaded = MutableStateFlow<Long>(0L)
    val bytesDownloaded: StateFlow<Long> = _bytesDownloaded.asStateFlow()

    private val _currentSpeedUp = MutableStateFlow<Long>(0L) // bytes/sec
    val currentSpeedUp: StateFlow<Long> = _currentSpeedUp.asStateFlow()

    private val _currentSpeedDown = MutableStateFlow<Long>(0L) // bytes/sec
    val currentSpeedDown: StateFlow<Long> = _currentSpeedDown.asStateFlow()

    val allProfiles: Flow<List<VpnProfile>> = profileDao.getAllProfiles()
    val recentLogs: Flow<List<VpnConnectionLog>> = profileDao.getRecentLogs()

    private var statsJob: kotlinx.coroutines.Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        try {
            backend = GoBackend(context)
            tunnel.setOnStateChangedListener { state ->
                _connectionState.value = state
                if (state == Tunnel.State.UP) {
                    startStatsMonitoring()
                } else {
                    stopStatsMonitoring()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize GoBackend", e)
        }

        // No longer populating initial default high-speed servers.
        // Users will import their own WireGuard configurations.
    }

    suspend fun insertProfile(profile: VpnProfile) {
        profileDao.insertProfile(profile)
    }

    suspend fun deleteProfile(profile: VpnProfile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: Int) {
        profileDao.deleteProfileById(id)
    }

    suspend fun clearLogs() {
        profileDao.clearLogs()
    }

    fun getTunnelState(): Tunnel.State {
        return backend?.let {
            try {
                it.getState(tunnel)
            } catch (e: Exception) {
                Tunnel.State.DOWN
            }
        } ?: Tunnel.State.DOWN
    }

    fun connect(profile: VpnProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val goBackend = backend
        if (goBackend == null) {
            onError("VPN service backend is not initialized.")
            return
        }

        repositoryScope.launch(Dispatchers.IO) {
            try {
                // Ensure any previous tunnel is fully down first
                if (goBackend.getState(tunnel) == Tunnel.State.UP) {
                    goBackend.setState(tunnel, Tunnel.State.DOWN, null)
                    delay(500)
                }

                // Parse WireGuard configuration
                val configStream = ByteArrayInputStream(profile.configText.toByteArray(StandardCharsets.UTF_8))
                val config = Config.parse(configStream)

                // Start the VPN
                goBackend.setState(tunnel, Tunnel.State.UP, config)
                
                // Add success log
                profileDao.insertLog(
                    VpnConnectionLog(
                        profileName = profile.name,
                        duration = 0,
                        bytesUploaded = 0,
                        bytesDownloaded = 0,
                        status = "Connected"
                    )
                )

                launch(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(tag, "VPN Connection Failed", e)
                profileDao.insertLog(
                    VpnConnectionLog(
                        profileName = profile.name,
                        duration = 0,
                        bytesUploaded = 0,
                        bytesDownloaded = 0,
                        status = "Failed: ${e.localizedMessage}"
                    )
                )
                launch(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Unknown Error during configuration parsing.")
                }
            }
        }
    }

    fun disconnect(profileName: String?, durationSeconds: Long) {
        val goBackend = backend ?: return
        repositoryScope.launch(Dispatchers.IO) {
            try {
                val upBytes = _bytesUploaded.value
                val downBytes = _bytesDownloaded.value

                goBackend.setState(tunnel, Tunnel.State.DOWN, null)

                _bytesUploaded.value = 0L
                _bytesDownloaded.value = 0L
                _currentSpeedUp.value = 0L
                _currentSpeedDown.value = 0L

                profileDao.insertLog(
                    VpnConnectionLog(
                        profileName = profileName ?: "Unknown Server",
                        duration = durationSeconds,
                        bytesUploaded = upBytes,
                        bytesDownloaded = downBytes,
                        status = "Disconnected"
                    )
                )
            } catch (e: Exception) {
                Log.e(tag, "VPN Disconnection error", e)
            }
        }
    }

    private fun startStatsMonitoring() {
        statsJob?.cancel()
        statsJob = repositoryScope.launch {
            var lastTx = 0L
            var lastRx = 0L

            while (_connectionState.value == Tunnel.State.UP) {
                val goBackend = backend
                if (goBackend != null) {
                    try {
                        val stats = goBackend.getStatistics(tunnel)
                        val rx = stats.getRx()
                        val tx = stats.getTx()

                        if (lastTx > 0) {
                            _currentSpeedUp.value = (tx - lastTx).coerceAtLeast(0)
                        }
                        if (lastRx > 0) {
                            _currentSpeedDown.value = (rx - lastRx).coerceAtLeast(0)
                        }

                        _bytesUploaded.value = tx
                        _bytesDownloaded.value = rx

                        lastTx = tx
                        lastRx = rx
                    } catch (e: Exception) {
                        Log.e(tag, "Error reading statistics", e)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopStatsMonitoring() {
        statsJob?.cancel()
        statsJob = null
        _currentSpeedUp.value = 0L
        _currentSpeedDown.value = 0L
    }

}
