package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.VpnProfile
import com.example.ui.theme.*
import com.wireguard.android.backend.Tunnel
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnAppScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier,
    onRequestVpnPermission: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Show errors via Toast or Banner safely
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DVAL WIRELESS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TextMain
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Dval Wireless Core: WireGuard Go Protocol Active", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "System Info",
                            tint = TextMuted
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0x0FFFFFFF),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Security else Icons.Outlined.Security,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Shield", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = CyanPrimary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x1938BDF8)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.Dns else Icons.Outlined.Dns,
                            contentDescription = "Servers"
                        )
                    },
                    label = { Text("Servers", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = CyanPrimary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x1938BDF8)
                    ),
                    modifier = Modifier.testTag("nav_servers")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.CloudUpload else Icons.Outlined.CloudUpload,
                            contentDescription = "Import"
                        )
                    },
                    label = { Text("Import", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = CyanPrimary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x1938BDF8)
                    ),
                    modifier = Modifier.testTag("nav_import")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = CyanPrimary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x1938BDF8)
                    ),
                    modifier = Modifier.testTag("nav_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
                .drawBehind {
                    // Top-left blue glow: bg-blue-600/20 blur-[100px]
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x3D2563EB), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, -size.height * 0.1f),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width * 0.8f,
                        center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, -size.height * 0.1f)
                    )
                    // Bottom-right indigo glow: bg-indigo-600/20 blur-[100px]
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x3D4F46E5), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 1.1f),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width * 0.8f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 1.1f)
                    )
                }
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(
                        viewModel = viewModel,
                        onRequestVpnPermission = onRequestVpnPermission,
                        onNavigateToServers = { selectedTab = 1 }
                    )
                    1 -> ServersTab(viewModel = viewModel)
                    2 -> ImportTab(viewModel = viewModel)
                    3 -> HistoryTab(viewModel = viewModel)
                }
            }

            // Connection status floating overlay
            statusMessage?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color(0xE6A8C7FA), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    viewModel: VpnViewModel,
    onRequestVpnPermission: () -> Unit,
    onNavigateToServers: () -> Unit
) {
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

    val bytesUploaded by viewModel.bytesUploaded.collectAsStateWithLifecycle()
    val bytesDownloaded by viewModel.bytesDownloaded.collectAsStateWithLifecycle()
    val currentSpeedUp by viewModel.currentSpeedUp.collectAsStateWithLifecycle()
    val currentSpeedDown by viewModel.currentSpeedDown.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.durationSeconds.collectAsStateWithLifecycle()

    val isConnected = connectionState == Tunnel.State.UP
    val statusText = when (connectionState) {
        Tunnel.State.UP -> "SECURED"
        Tunnel.State.TOGGLE -> "PENDING"
        else -> "UNPROTECTED"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hero shield visual banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dval_vpn_shield),
                    contentDescription = "Shield Graphic",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark cyber scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.9f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "WIREGUARD SECURED",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Military-grade peer-to-peer encryption engine active.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMain.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }

        // Selected Server Selection Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToServers() },
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0x1AFFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0x16FFFFFF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedProfile?.let { viewModel.getFlagEmoji(it.countryCode) } ?: "🌐",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "CURRENT ROUTE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = selectedProfile?.name ?: "No Server Selected",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Change Server",
                        tint = TextMuted
                    )
                }
            }
        }

        // Circular Connection Trigger with Pulse Animation
        item {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                if (isConnected) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x3DA8C7FA), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension / 2f * pulseScale
                            ),
                            radius = size.minDimension / 2f * pulseScale,
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x1238BDF8), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension * 0.5f
                            ),
                            radius = size.minDimension * 0.5f,
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }

                Surface(
                    onClick = {
                        onRequestVpnPermission()
                        viewModel.toggleConnection()
                    },
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, CircleShape)
                        .testTag("connection_toggle"),
                    shape = CircleShape,
                    color = if (isConnected) Color(0xFFA8C7FA) else Color(0x0EFFFFFF),
                    border = BorderStroke(if (isConnected) 4.dp else 2.dp, Color(0x33FFFFFF))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle Connection",
                            tint = if (isConnected) Color(0xFF0F172A) else CyanPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = if (isConnected) Color(0xFF0F172A) else TextMain
                            )
                        )
                        if (isConnected) {
                            Text(
                                text = viewModel.formatDuration(durationSeconds),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A).copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Network Statistics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Download Speed Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download Speed",
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DOWNLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.formatSpeed(currentSpeedDown),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total: ${viewModel.formatBytes(bytesDownloaded)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted
                            )
                        )
                    }
                }

                // Upload Speed Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upload Speed",
                                tint = Color(0xFF34D399), // Emerald 400
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UPLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.formatSpeed(currentSpeedUp),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total: ${viewModel.formatBytes(bytesUploaded)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ServersTab(viewModel: VpnViewModel) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val serverMetrics by viewModel.serverMetrics.collectAsStateWithLifecycle()
    val isRefreshingMetrics by viewModel.isRefreshingMetrics.collectAsStateWithLifecycle()

    val isConnected = connectionState == Tunnel.State.UP

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AVAILABLE ROUTES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
                Text(
                    text = "Select your preferred WireGuard peer below.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )
            }
            
            IconButton(
                onClick = { viewModel.refreshMetrics() },
                enabled = !isRefreshingMetrics,
                modifier = Modifier.testTag("refresh_metrics_button")
            ) {
                if (isRefreshingMetrics) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CyanPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh server performance",
                        tint = CyanPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Profiles Available",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .testTag("servers_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles) { profile ->
                    val isSelected = selectedProfile?.id == profile.id
                    val profileBorderColor = if (isSelected) {
                        CyanPrimary
                    } else {
                        Color(0x1AFFFFFF)
                    }

                    val metrics = serverMetrics[profile.id]
                    val pingValue = metrics?.pingMs
                    val loadValue = metrics?.loadPercentage ?: 50

                    // Determine load severity colors
                    val loadColor = when {
                        loadValue < 40 -> Color(0xFF10B981) // Emerald
                        loadValue < 80 -> Color(0xFFF59E0B) // Amber
                        else -> Color(0xFFEF4444) // Rose
                    }

                    // Determine ping severity colors
                    val pingColor = when {
                        pingValue == null -> TextMuted
                        pingValue < 60 -> Color(0xFF10B981) // Emerald
                        pingValue < 140 -> Color(0xFFF59E0B) // Amber
                        else -> Color(0xFFEF4444) // Rose
                    }

                    Card(
                        onClick = { viewModel.selectProfile(profile) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_item_${profile.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                Color(0x1AFFFFFF)
                            } else {
                                Color(0x0DFFFFFF)
                            }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, profileBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = viewModel.getFlagEmoji(profile.countryCode),
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextMain
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (profile.isCustom) "User Config" else "Premium Peer",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted
                                        )
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.Lock else Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else if (profile.isCustom && !isConnected) {
                                    IconButton(
                                        onClick = { viewModel.deleteProfile(profile) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SignalCellularAlt,
                                        contentDescription = "Ping",
                                        tint = pingColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    if (pingValue == null) {
                                        Text(
                                            text = "Testing...",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextMuted,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "$pingValue ms",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = pingColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NetworkCheck,
                                        contentDescription = "Server Load",
                                        tint = loadColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Load: $loadValue%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = loadColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = loadValue / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = loadColor,
                                trackColor = Color(0x16FFFFFF)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ImportTab(viewModel: VpnViewModel) {
    var name by remember { mutableStateOf("") }
    var configText by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("US") }
    val context = LocalContext.current

    // File Picker integration
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                if (inputStream != null) {
                    val defaultFileName = fileUri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: "Imported"
                    viewModel.importConfigFromFile(
                        name = "📁 $defaultFileName",
                        inputStream = inputStream,
                        countryCode = countryCode
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) {
                            name = ""
                            configText = ""
                        }
                    }
                } else {
                    Toast.makeText(context, "Could not open selected file.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0x0DFFFFFF),
        unfocusedContainerColor = Color(0x06FFFFFF),
        focusedBorderColor = CyanPrimary,
        unfocusedBorderColor = Color(0x1AFFFFFF),
        focusedLabelColor = CyanPrimary,
        unfocusedLabelColor = TextMuted,
        focusedLeadingIconColor = CyanPrimary,
        unfocusedLeadingIconColor = TextMuted,
        focusedTextColor = TextMain,
        unfocusedTextColor = TextMain,
        focusedPlaceholderColor = TextMuted.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = TextMuted.copy(alpha = 0.5f)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "IMPORT PEER CONFIGURATION",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                text = "Paste your WireGuard Quick (.conf) config or upload the file directly.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted
                )
            )
        }

        // Action Buttons: Import File
        item {
            OutlinedButton(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_select_file"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0x0DFFFFFF),
                    contentColor = TextMain
                ),
                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = CyanPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select .conf Configuration File", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x1AFFFFFF))
                Text(
                    text = "OR PASTE RAW CONFIG",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x1AFFFFFF))
            }
        }

        // Form Fields
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Configuration Name") },
                placeholder = { Text("e.g. Custom California") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_config_name"),
                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )
        }

        item {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { countryCode = it.take(2) },
                label = { Text("Country Code (2 letters)") },
                placeholder = { Text("US, DE, JP, GB") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_country_code"),
                leadingIcon = { Icon(imageVector = Icons.Default.Public, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = textFieldColors
            )
        }

        item {
            OutlinedTextField(
                value = configText,
                onValueChange = { configText = it },
                label = { Text("WireGuard configText ([Interface] & [Peer])") },
                placeholder = {
                    Text(
                        "[Interface]\nPrivateKey = ...\nAddress = 10.0.0.2/24\n\n[Peer]\nPublicKey = ...\nEndpoint = 1.2.3.4:51820\nAllowedIPs = 0.0.0.0/0"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("input_config_text"),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.importConfigFromText(
                        name = "📁 $name",
                        configText = configText,
                        countryCode = countryCode
                    ) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        if (success) {
                            name = ""
                            configText = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_import_text"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA8C7FA),
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistoryTab(viewModel: VpnViewModel) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONNECTION HISTORY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
                Text(
                    text = "Review your secure tunnel sessions.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )
            }
            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyanPrimary)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Connection Records Yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .testTag("logs_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (log.status.contains("Failed", ignoreCase = true)) {
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        } else {
                                            Color(0x16FFFFFF)
                                        },
                                        CircleShape
                                    )
                                    .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (log.status.contains("Failed", ignoreCase = true)) {
                                        Icons.Default.Error
                                    } else if (log.status == "Connected") {
                                        Icons.Default.Power
                                    } else {
                                        Icons.Default.PowerOff
                                    },
                                    contentDescription = null,
                                    tint = if (log.status.contains("Failed", ignoreCase = true)) {
                                        MaterialTheme.colorScheme.error
                                    } else if (log.status == "Connected") {
                                        Color(0xFF34D399) // Emerald 400
                                    } else {
                                        CyanPrimary
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = log.profileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain
                                    )
                                )
                                Text(
                                    text = log.status,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (log.status.contains("Failed", ignoreCase = true)) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            TextMuted
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                if (log.status == "Disconnected") {
                                    Text(
                                        text = "Duration: ${viewModel.formatDuration(log.duration)} | Tx: ${viewModel.formatBytes(log.bytesUploaded)} | Rx: ${viewModel.formatBytes(log.bytesDownloaded)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted.copy(alpha = 0.8f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
