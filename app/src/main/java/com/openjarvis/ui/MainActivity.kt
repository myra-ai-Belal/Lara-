package com.openjarvis.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.openjarvis.LaraApplication
import com.openjarvis.accessibility.JarvisAccessibilityService
import com.openjarvis.agent.AgentCore
import com.openjarvis.agent.AgentState
import com.openjarvis.graphify.GraphifyRepository
import com.openjarvis.graphify.nodes.TaskNode
import com.openjarvis.ui.dashboard.DashboardScreen
import com.openjarvis.ui.settings.SettingsScreen
import com.openjarvis.ui.splash.LaraSplashScreen
import com.openjarvis.ui.theme.OpenJarvisTheme

class MainActivity : ComponentActivity() {
    
    private var graphifyRepo: GraphifyRepository? = null
    private var agentCore: AgentCore? = null
    private var initError: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // If the app crashed last time (from any thread), show that reason now
        // instead of silently trying again and possibly crashing the same way.
        val crashFile = java.io.File(filesDir, LaraApplication.CRASH_LOG_FILE)
        if (crashFile.exists()) {
            initError = "Previous crash:\n\n" + crashFile.readText().take(1000)
            crashFile.delete()
        }
        
        // Wrapped defensively: if ANYTHING here throws (including Error
        // subtypes like NoClassDefFoundError, not just Exception), we show
        // a friendly on-screen message instead of letting the app crash
        // silently with no way to see what went wrong.
        if (initError == null) {
            try {
                graphifyRepo = GraphifyRepository(this)
                agentCore = AgentCore(this)
            } catch (e: Throwable) {
                initError = "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            }
        }
        
        setContent {
            OpenJarvisTheme {
                var showSplash by remember { mutableStateOf(true) }
                var runtimeError by remember { mutableStateOf<String?>(null) }
                
                when {
                    runtimeError != null -> InitErrorScreen(message = runtimeError!!)
                    showSplash -> LaraSplashScreen(onFinished = { showSplash = false })
                    initError != null -> InitErrorScreen(message = initError!!)
                    else -> {
                        try {
                            MainAppContent()
                        } catch (e: Throwable) {
                            runtimeError = "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun MainAppContent() {
        val repo = graphifyRepo ?: return
        val agent = agentCore ?: return
        val context = LocalContext.current
        var recentTasks by remember { mutableStateOf<List<TaskNode>>(emptyList()) }
        
        LaunchedEffect(Unit) {
            recentTasks = repo.getRecentTasks(10)
        }
        
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = Settings.canDrawOverlays(this)
        
        val agentState by agent.state.collectAsState()
        
        if (!accessibilityEnabled || !overlayEnabled) {
            PermissionScreen(
                accessibilityEnabled = accessibilityEnabled,
                overlayEnabled = overlayEnabled,
                onEnableAccessibility = { startAccessibilitySettings() },
                onEnableOverlay = { startOverlaySettings() }
            )
        } else {
            DashboardScreen(
                onStartOverlay = { startOverlayService() },
                onOpenSettings = { openSettings() },
                graphifyRepo = repo,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val componentName = ComponentName(this, JarvisAccessibilityService::class.java)
        return enabledServices.contains(componentName.flattenToString())
    }
    
    private fun startAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
    
    private fun startOverlaySettings() {
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
            android.net.Uri.parse("package:$packageName")))
    }
    
    private fun startOverlayService() {
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Lara is active", Toast.LENGTH_SHORT).show()
    }
    
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}

@Composable
fun InitErrorScreen(message: String) {
    val bg = com.openjarvis.ui.theme.VoidColor.Void
    val text = com.openjarvis.ui.theme.VoidColor.TextPrimary
    val sub = com.openjarvis.ui.theme.VoidColor.TextSecondary
    val red = com.openjarvis.ui.theme.VoidColor.Red

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Lara AI couldn't start",
                color = red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = sub,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Try closing and reopening the app. If this keeps happening, share this message with support.",
                color = text,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PermissionScreen(
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    onEnableAccessibility: () -> Unit,
    onEnableOverlay: () -> Unit
) {
    val primaryColor = com.openjarvis.ui.theme.VoidColor.Violet
    val onSurfaceColor = com.openjarvis.ui.theme.VoidColor.TextPrimary
    val onSurfaceVariantColor = com.openjarvis.ui.theme.VoidColor.TextSecondary
    
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = "Permission Required",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = onSurfaceColor
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        
        androidx.compose.material3.Text(
            text = "Open Jarvis needs accessibility and overlay permissions to control your device.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariantColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        
        if (!accessibilityEnabled) {
            androidx.compose.material3.Button(
                onClick = onEnableAccessibility,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Enable Accessibility Service")
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (!overlayEnabled) {
            androidx.compose.material3.Button(
                onClick = onEnableOverlay,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Enable Overlay Permission")
            }
        }
        
        if (accessibilityEnabled && overlayEnabled) {
            androidx.compose.material3.Text(
                text = "Lara is active",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = primaryColor
            )
        }
    }
}