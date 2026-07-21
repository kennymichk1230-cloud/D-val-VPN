package com.example

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.VpnAppScreen
import com.example.ui.VpnViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: VpnViewModel by viewModels()

    private var pendingPermissionCallback: (() -> Unit)? = null

    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "VPN permission granted! Connecting...", Toast.LENGTH_SHORT).show()
            pendingPermissionCallback?.invoke()
        } else {
            Toast.makeText(this, "VPN permission denied. Cannot establish tunnel.", Toast.LENGTH_LONG).show()
        }
        pendingPermissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VpnAppScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onRequestVpnPermission = { onGranted ->
                        val intent = VpnService.prepare(this)
                        if (intent != null) {
                            pendingPermissionCallback = onGranted
                            vpnPrepareLauncher.launch(intent)
                        } else {
                            onGranted()
                        }
                    }
                )
            }
        }
    }
}
