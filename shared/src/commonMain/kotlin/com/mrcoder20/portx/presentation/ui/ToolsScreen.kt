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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcoder20.portx.domain.LocalizedStrings
import com.mrcoder20.portx.presentation.ui.theme.*
import com.mrcoder20.portx.presentation.viewmodel.ToolsViewModel
import org.koin.compose.koinInject

@Composable
fun ToolsScreen(viewModel: ToolsViewModel = koinInject()) {
    val state by viewModel.uiState.collectAsState()
    val appSettings = LocalAppSettings.current
    val accent = LocalAccentColor.current
    val lang = appSettings.language

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. TOOL SELECTOR (NEON CHIPS)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "LOCAL" to Icons.Default.Lan,
                    "PING" to Icons.Default.NetworkPing,
                    "DNS" to Icons.Default.Dns,
                    "WHOIS" to Icons.Default.Public
                ).forEach { (type, icon) ->
                    ToolChip(
                        label = type,
                        icon = icon,
                        isSelected = state.activeTool == type,
                        accent = accent,
                        modifier = Modifier.width(100.dp)
                    ) {
                        viewModel.selectTool(type)
                    }
                }
            }

            // 2. INTEGRATED ACTION BAR (Hides automatically for LOCAL)
            AnimatedVisibility(
                visible = state.activeTool != "LOCAL",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
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
                        Icon(
                            imageVector = when(state.activeTool) {
                                "DNS" -> Icons.Default.Dns
                                "WHOIS" -> Icons.Default.Public
                                else -> Icons.Default.NetworkPing
                            },
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.padding(start = 12.dp).size(20.dp)
                        )

                        OutlinedTextField(
                            value = state.target,
                            onValueChange = { viewModel.onTargetChange(it) },
                            placeholder = { Text(LocalizedStrings.get("target", lang), color = if (appSettings.theme == "DARK") TextMuted else TextMutedLight) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedTextColor = if (appSettings.theme == "DARK") Color.White else Color.Black,
                                focusedTextColor = if (appSettings.theme == "DARK") Color.White else Color.Black,
                                cursorColor = accent
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Row(
                            modifier = Modifier.padding(end = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.copyResultsToClipboard() },
                                modifier = Modifier.size(40.dp).background(if (appSettings.theme == "DARK") Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copy", tint = if (appSettings.theme == "DARK") Color.White else Color.Black, modifier = Modifier.size(18.dp))
                            }

                            val infiniteTransition = rememberInfiniteTransition()
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.6f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                            )

                            Button(
                                onClick = {
                                    if (state.isLoading) {
                                        viewModel.stopActiveTool()
                                    } else {
                                        when(state.activeTool) {
                                            "PING" -> viewModel.runPing()
                                            "DNS" -> viewModel.runDnsLookup()
                                            "WHOIS" -> viewModel.runWhois()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isLoading) DangerNeon.copy(alpha = pulseAlpha) else accent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(48.dp).width(80.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    if (state.isLoading) Icons.Default.Stop else Icons.Default.FlashOn,
                                    null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (state.isLoading) LocalizedStrings.get("stop", lang) else LocalizedStrings.get("go", lang),
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    it, color = DangerNeon, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // 3. RESULTS DISPLAY
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = state.activeTool,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { tool ->
                    when (tool) {
                        "PING" -> PingResultPanel(state.pingResults, state.isLoading, accent) { viewModel.copyIndividualResult(it) }
                        "DNS" -> DnsResultPanel(state.dnsResults, state.isLoading, accent) { viewModel.copyIndividualResult(it) }
                        "WHOIS" -> WhoisResultPanel(state.whoisResult, state.isLoading)
                        else -> LocalInfoPanel(
                        state.localIp, state.publicIp, state.isLoading, accent, lang,
                        onCopy = { viewModel.copyIndividualResult(it) },
                        onRefresh = { viewModel.refreshLocalInfo() }
                    )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.snackbarMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
        ) {
            state.snackbarMessage?.let { msg ->
                PremiumSnackbar(message = msg)
            }
        }
    }
}

@Composable
fun ToolChip(label: String, icon: ImageVector, isSelected: Boolean, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accent.copy(alpha = 0.15f) else GlassBackground,
        border = BorderStroke(1.dp, if (isSelected) accent else GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (isSelected) accent else (if (isDark) TextMuted else TextMutedLight), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = if (isSelected) (if (isDark) Color.White else Color.Black) else (if (isDark) TextMuted else TextMutedLight), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PingResultPanel(results: List<com.mrcoder20.portx.domain.PingResult>, isLoading: Boolean, accent: Color, onCopyItem: (String) -> Unit) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    val listState = rememberLazyListState()
    LaunchedEffect(results.size) { if (results.isNotEmpty()) listState.animateScrollToItem(results.size - 1) }
    GlassCard(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TACTICAL ICMP OUTPUT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
                if (isLoading) Text("QUERYING...", style = MaterialTheme.typography.labelSmall, color = accent)
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(results) { res ->
                        Row(modifier = Modifier.clickable { onCopyItem(res.message) }) {
                            Text("> ", color = accent, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Text(res.message, color = if (res.isSuccess) (if (isDark) Color.White else Color.Black) else DangerNeon, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                    if (results.isEmpty() && !isLoading) item { Text("Engine ready...", color = if (isDark) TextMuted else TextMutedLight, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                }
            }
        }
    }
}

@Composable
fun DnsResultPanel(results: List<String>, isLoading: Boolean, accent: Color, onCopyItem: (String) -> Unit) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    GlassCard(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        Column {
            Text("DNS RESOLUTION RECORDS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
            Spacer(Modifier.height(12.dp))
            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = accent)
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { ip ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onCopyItem(ip) }.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Adjust, null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(ip, color = if (isDark) Color.White else Color.Black, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                        }
                    }
                    if (results.isEmpty() && !isLoading) item { Text("Awaiting target resolution...", color = if (isDark) TextMuted else TextMutedLight, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                }
            }
        }
    }
}

@Composable
fun WhoisResultPanel(result: String?, isLoading: Boolean) {
    val accent = LocalAccentColor.current
    val isDark = LocalAppSettings.current.theme == "DARK"
    GlassCard(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        Column {
            Text("WHOIS AUTHORITY DATA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
            Spacer(Modifier.height(12.dp))
            if (isLoading && result == null) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = accent)
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).verticalScroll(rememberScrollState()).padding(12.dp)) {
                Text(result ?: "Ready to query WHOIS...", color = if (result != null) (if (isDark) Color.White else Color.Black) else (if (isDark) TextMuted else TextMutedLight), fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun LocalInfoPanel(
    info: com.mrcoder20.portx.domain.LocalIpInfo?, 
    publicIp: String?, 
    isLoading: Boolean, 
    accent: Color, 
    lang: String, 
    onCopy: (String) -> Unit,
    onRefresh: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("DEVICE ENVIRONMENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accent, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                }
            }

            info?.let {
                InfoItem("INTERNAL IP", it.ipAddress, Icons.Default.Lan, accent) { onCopy(it.ipAddress) }
                InfoItem("ADAPTER", it.interfaceName, Icons.Default.SettingsInputComponent, accent) { onCopy(it.interfaceName) }
                InfoItem("CONNECTION", if (it.isWifi) "WI-FI" else "WIRED", if (it.isWifi) Icons.Default.Wifi else Icons.Default.SettingsEthernet, accent) {}
            }

            publicIp?.let {
                InfoItem("PUBLIC IP", it, Icons.Default.Public, accent) { onCopy(it) }
            }
            
            if (info == null && publicIp == null && !isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, accent)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = if (LocalAppSettings.current.theme == "DARK") Color.White else Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(LocalizedStrings.get("refresh", lang), color = if (LocalAppSettings.current.theme == "DARK") Color.White else Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Row(
        modifier = Modifier.fillMaxWidth().background(if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDark) TextMuted else TextMutedLight, letterSpacing = 1.sp)
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = if (isDark) Color.White else Color.Black)
        }
    }
}
