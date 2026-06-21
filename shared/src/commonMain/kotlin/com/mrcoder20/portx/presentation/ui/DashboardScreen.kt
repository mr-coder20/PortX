package com.mrcoder20.portx.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcoder20.portx.domain.LocalizedStrings
import com.mrcoder20.portx.presentation.ui.theme.*
import com.mrcoder20.portx.presentation.viewmodel.ScanViewModel
import com.mrcoder20.portx.presentation.viewmodel.ScanUIState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// --- Data Models & Helpers ---

data class DisplayPort(val number: Int, val description: String, val color: Color)

fun getPortColor(port: Int, accent: Color): Color {
    return when(port) {
        80, 443 -> accent
        22 -> SecondaryNeon
        else -> TertiaryNeon
    }
}

fun getMockDescription(port: Int): String {
    return when(port) {
        80 -> "HTTP Web Server"
        443 -> "HTTPS Secure Web"
        22 -> "SSH Remote Access"
        21 -> "FTP File Transfer"
        else -> "Active Service"
    }
}

fun getServiceTitle(port: Int): String {
    return when(port) {
        80 -> "HTTP Web Service"
        443 -> "HTTPS Secure Web"
        22 -> "SSH Remote Access"
        21 -> "FTP File Transfer"
        3306 -> "MySQL Database"
        5432 -> "PostgreSQL DB"
        else -> "Service on Port $port"
    }
}

// --- Dashboard Implementation ---

@Composable
fun DashboardScreen(viewModel: ScanViewModel) {
    val state by viewModel.uiState.collectAsState()
    val appSettings = LocalAppSettings.current
    val accent = LocalAccentColor.current
    val lang = appSettings.language

    BoxWithConstraints(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        val width = maxWidth
        
        when {
            width > 1200.dp -> LargeDesktopDashboard(state, viewModel, accent, lang)
            width > 800.dp -> DesktopDashboard(state, viewModel, accent, lang)
            else -> MobileDashboard(state, viewModel, accent, lang)
        }
    }
}

@Composable
fun LargeDesktopDashboard(state: ScanUIState, viewModel: ScanViewModel, accent: Color, lang: String) {
    Row(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            DashboardIpInput(
                state = state,
                accent = accent,
                lang = lang,
                onIpChange = { viewModel.onIpChange(it) },
                onStartScan = { viewModel.startScan() },
                onStopScan = { viewModel.stopScan() }
            )
            
            state.error?.let {
                Surface(
                    color = DangerNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DangerNeon.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it, color = DangerNeon, modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            AdvancedParametersCard(state, viewModel, accent, lang)
            EngineConfigurationCard(state, viewModel, accent)
            EngineLogsCard(state, modifier = Modifier.weight(1f), accent)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            SecurityVisualizerCard(state, modifier = Modifier.fillMaxWidth().aspectRatio(1.1f), accent = accent, lang = lang)
        }

        Column(modifier = Modifier.weight(1.1f)) {
            ActiveServicesCard(state, modifier = Modifier.fillMaxHeight(), accent = accent, lang = lang)
        }
    }
}

@Composable
fun DesktopDashboard(state: ScanUIState, viewModel: ScanViewModel, accent: Color, lang: String) {
    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            DashboardIpInput(
                state = state,
                accent = accent,
                lang = lang,
                onIpChange = { viewModel.onIpChange(it) },
                onStartScan = { viewModel.startScan() },
                onStopScan = { viewModel.stopScan() }
            )

            state.error?.let {
                Text(it, color = DangerNeon, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
            }

            SecurityVisualizerCard(state, modifier = Modifier.weight(1.2f), accent = accent, lang = lang)
            EngineLogsCard(state, modifier = Modifier.weight(0.8f), accent = accent)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ActiveServicesCard(state, modifier = Modifier.weight(1f), accent = accent, lang = lang)
            AdvancedParametersCard(state, viewModel, accent = accent, lang = lang)
            EngineConfigurationCard(state, viewModel, accent = accent)
        }
    }
}

@Composable
fun MobileDashboard(state: ScanUIState, viewModel: ScanViewModel, accent: Color, lang: String) {
    var showSettings by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        
        DashboardIpInput(
            state = state, 
            accent = accent,
            lang = lang,
            onIpChange = { viewModel.onIpChange(it) },
            onStartScan = { 
                viewModel.startScan()
                showSettings = false 
            },
            onStopScan = { viewModel.stopScan() },
            showSettingsToggle = true, 
            onSettingsToggle = { showSettings = !showSettings }
        )

        AnimatedVisibility(
            visible = showSettings,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdvancedParametersCard(state, viewModel, accent, lang)
                EngineConfigurationCard(state, viewModel, accent)
            }
        }

        SecurityVisualizerCard(state, modifier = Modifier.height(240.dp), smallSize = true, accent = accent, lang = lang)
        ActiveServicesCard(state, modifier = Modifier.weight(1f), accent = accent, lang = lang)
        
        state.error?.let { errorMsg ->
            Text(
                text = errorMsg, color = DangerNeon, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun DashboardIpInput(
    state: ScanUIState, 
    accent: Color,
    lang: String,
    onIpChange: (String) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    showSettingsToggle: Boolean = false,
    onSettingsToggle: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSettingsToggle) {
                IconButton(onClick = onSettingsToggle, modifier = Modifier.padding(start = 4.dp)) {
                    Icon(Icons.Default.Tune, null, tint = accent)
                }
            } else {
                Icon(Icons.Default.Language, null, tint = accent, modifier = Modifier.padding(start = 12.dp).size(24.dp))
            }
            
            OutlinedTextField(
                value = state.ip,
                onValueChange = onIpChange,
                placeholder = { Text(LocalizedStrings.get("target", lang), color = if (LocalAppSettings.current.theme == "DARK") TextMuted else TextMutedLight, maxLines = 1) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedTextColor = if (LocalAppSettings.current.theme == "DARK") Color.White else Color.Black,
                    focusedTextColor = if (LocalAppSettings.current.theme == "DARK") Color.White else Color.Black,
                    cursorColor = accent
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            
            val infiniteTransition = rememberInfiniteTransition()
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
            )

            Button(
                onClick = { if (state.isLoading) onStopScan() else onStartScan() },
                colors = ButtonDefaults.buttonColors(containerColor = if (state.isLoading) DangerNeon.copy(alpha = pulseAlpha) else accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 2.dp).height(52.dp).width(96.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(if (state.isLoading) Icons.Default.Stop else Icons.Default.FlashOn, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (state.isLoading) LocalizedStrings.get("stop", lang) else LocalizedStrings.get("scan", lang), fontWeight = FontWeight.Black, color = Color.Black, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedParametersCard(state: ScanUIState, viewModel: ScanViewModel, accent: Color, lang: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(LocalizedStrings.get("advanced", lang), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScanChip("Banner Grabbing", state.bannerGrabbing, accent) { viewModel.toggleBannerGrabbing(it) }
                ScanChip("Full Port Scan", state.allPorts, accent) { viewModel.toggleAllPorts(it) }
                ScanChip("Multi-Protocol", state.allProtocols, accent) { viewModel.toggleAllProtocols(it) }
                ScanChip("Stealth Mode", state.scanType == "SYN", accent) { viewModel.onScanTypeChange(if(it) "SYN" else "TCP") }
            }
        }
    }
}

@Composable
fun EngineConfigurationCard(state: ScanUIState, viewModel: ScanViewModel, accent: Color) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("ENGINE CONFIGURATION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = if (isDark) TextMuted else TextMutedLight, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Parallel Threads", color = if (isDark) Color.White else Color.Black, style = MaterialTheme.typography.bodySmall)
                    Text("${state.concurrentScans} connections", color = accent, style = MaterialTheme.typography.labelMedium)
                }
                Slider(
                    value = state.concurrentScans.toFloat(),
                    onValueChange = { viewModel.onConcurrentScansChange(it.toInt()) },
                    valueRange = 10f..2000f,
                    modifier = Modifier.weight(2f),
                    colors = SliderDefaults.colors(
                        thumbColor = accent, 
                        activeTrackColor = accent, 
                        inactiveTrackColor = if (isDark) GlassBorder else GlassBorderLight
                    )
                )
            }
        }
    }
}

@Composable
fun SecurityVisualizerCard(state: ScanUIState, modifier: Modifier = Modifier, smallSize: Boolean = false, accent: Color, lang: String) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    GlassCard(modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            val displayProgress = if (state.isLoading) state.progress / 100f else {
                val result = state.result
                if (result != null) result.securityScore / 100f else 0f
            }
            val statusColor = when {
                state.isLoading -> accent
                state.result != null -> {
                    val score = state.result.securityScore
                    when {
                        score > 80 -> TertiaryNeon
                        score > 50 -> if (isDark) WarningNeon else WarningLight
                        else -> if (isDark) DangerNeon else DangerLight
                    }
                }
                else -> (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f)
            }
            AdvancedLiquidGauge(progress = displayProgress, isLoading = state.isLoading, color = statusColor, gaugeSize = if (smallSize) 180.dp else 220.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isLoading) {
                    Text("${state.progress}%", style = (if (smallSize) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium).copy(fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black, shadow = Shadow(color = statusColor, blurRadius = 30f)))
                    Text("ENGINE RUNNING", style = MaterialTheme.typography.labelMedium.copy(color = statusColor, letterSpacing = 2.sp))
                } else {
                    val result = state.result
                    if (result != null) {
                        Text("${result.securityScore}%", style = (if (smallSize) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium).copy(fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black, shadow = Shadow(color = statusColor, blurRadius = 30f)))
                        Text(LocalizedStrings.get("security_score", lang), style = MaterialTheme.typography.labelMedium.copy(color = statusColor, letterSpacing = 2.sp))
                    } else {
                        Icon(Icons.Default.Radar, null, tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f), modifier = Modifier.size(if (smallSize) 32.dp else 48.dp))
                        Text("READY", style = MaterialTheme.typography.titleMedium.copy(color = if (isDark) TextMuted else TextMutedLight, letterSpacing = 2.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun EngineLogsCard(state: ScanUIState, modifier: Modifier = Modifier, accent: Color) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    val logListState = rememberLazyListState()
    LaunchedEffect(state.logs.size) { if (state.logs.isNotEmpty()) logListState.animateScrollToItem(state.logs.size - 1) }
    GlassCard(modifier = modifier, contentPadding = PaddingValues(12.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Terminal, null, tint = accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("LIVE ENGINE LOGS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = if (isDark) TextMuted else TextMutedLight)
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                LazyColumn(state = logListState, modifier = Modifier.fillMaxSize()) {
                    items(state.logs) { log ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("> ", color = accent, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            Text(log, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                        }
                    }
                    if (state.logs.isEmpty()) item { Text("Waiting for engine activity...", color = (if (isDark) TextMuted else TextMutedLight).copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)) }
                }
            }
        }
    }
}

@Composable
fun ActiveServicesCard(state: ScanUIState, modifier: Modifier = Modifier, accent: Color, lang: String) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    GlassCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(LocalizedStrings.get("active_services", lang), style = MaterialTheme.typography.labelLarge.copy(color = if (isDark) TextMuted else TextMutedLight, fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val result = state.result
                    if (result != null && result.openPorts.isNotEmpty()) {
                        Text("${result.openPorts.size} FOUND", style = MaterialTheme.typography.labelSmall, color = accent, modifier = Modifier.background(accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Row(modifier = Modifier.background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), CircleShape).border(1.dp, GlassBorder, CircleShape)) {
                        IconButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowUp, null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = { scope.launch { val count = state.result?.openPorts?.size ?: 0; if (count > 0) listState.animateScrollToItem(count - 1) } }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowDown, null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    val result = state.result
                    val portsToShow = result?.openPorts?.distinct()?.sorted()?.map { portNumber ->
                        val banner = result.portBanners[portNumber] ?: ""
                        val service = result.portServices[portNumber] ?: getMockDescription(portNumber)
                        val description = banner.ifEmpty { service }
                        DisplayPort(portNumber, description, getPortColor(portNumber, accent))
                    } ?: emptyList()
                    if (portsToShow.isEmpty()) item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(if (state.isLoading) Icons.Default.HourglassEmpty else Icons.Default.SearchOff, null, tint = (if (isDark) TextMuted else TextMutedLight).copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(if (state.isLoading) "SCANNING NETWORK..." else LocalizedStrings.get("no_services", lang), color = if (isDark) TextMuted else TextMutedLight, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else items(portsToShow, key = { it.number }) { port -> PortItem(port) }
                }
                Box(modifier = Modifier.fillMaxWidth().height(32.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, (if (isDark) BackgroundDark else BackgroundLight).copy(alpha = 0.5f)))))
            }
        }
    }
}

@Composable
fun ScanChip(label: String, checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(onClick = { onCheckedChange(!checked) }, shape = RoundedCornerShape(12.dp), color = if (checked) accent.copy(alpha = 0.15f) else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)), border = BorderStroke(1.dp, if (checked) accent else GlassBorder)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (checked) accent else (if (isDark) TextMuted else TextMutedLight)))
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (checked) (if (isDark) Color.White else Color.Black) else (if (isDark) TextMuted else TextMutedLight))
        }
    }
}

@Composable
fun AdvancedLiquidGauge(progress: Float, isLoading: Boolean, color: Color, gaugeSize: androidx.compose.ui.unit.Dp = 220.dp) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(if (isLoading) 2500 else 8000, easing = LinearEasing)))
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = spring(stiffness = Spring.StiffnessLow))
    Box(modifier = Modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(gaugeSize * 0.85f)) {
            val strokeWidth = (gaugeSize.toPx() * 0.045f)
            drawCircle(color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.05f), style = Stroke(width = strokeWidth))
            drawArc(brush = Brush.sweepGradient(0f to color.copy(alpha = 0.2f), 0.5f to color, 1f to color.copy(alpha = 0.2f), center = center), startAngle = rotation, sweepAngle = 360f * animatedProgress, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
        Box(modifier = Modifier.size(gaugeSize * 0.68f).clip(CircleShape).background(Brush.verticalGradient(listOf((if (isDark) Color.White else Color.Black).copy(alpha = 0.08f), Color.Transparent))).border(1.dp, color.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            if (isLoading) {
                repeat(3) { index ->
                    val dotRotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(2000 + index * 400, easing = LinearEasing), repeatMode = RepeatMode.Restart))
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val angleRad = (dotRotation * (3.14159 / 180)).toFloat()
                        val radius = size.width * 0.42f
                        val x = center.x + radius * kotlin.math.cos(angleRad)
                        val y = center.y + radius * kotlin.math.sin(angleRad)
                        drawCircle(color = color, radius = (gaugeSize.toPx() * 0.012f), center = Offset(x, y), alpha = 0.8f - (index * 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun PortItem(port: DisplayPort) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, if (isDark) GlassBorder else GlassBorderLight, RoundedCornerShape(16.dp)).clickable { /* Detail */ }, color = if (isDark) GlassSurface else Color.Black.copy(alpha = 0.02f)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(port.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, port.color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(port.number.toString(), color = port.color, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), fontSize = if (port.number > 9999) 9.sp else 11.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(getServiceTitle(port.number), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black))
                Text(port.description, style = MaterialTheme.typography.bodySmall.copy(color = if (isDark) TextSecondary else TextSecondaryLight), maxLines = 1)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = if (isDark) TextMuted else TextMutedLight, modifier = Modifier.size(20.dp))
        }
    }
}

