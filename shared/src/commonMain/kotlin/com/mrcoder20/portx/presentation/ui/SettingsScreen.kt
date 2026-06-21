package com.mrcoder20.portx.presentation.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcoder20.portx.domain.Language
import com.mrcoder20.portx.domain.LocalizedStrings
import com.mrcoder20.portx.domain.SettingsManager
import com.mrcoder20.portx.presentation.ui.theme.*
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(settingsManager: SettingsManager = koinInject()) {
    val state by settingsManager.settings.collectAsState()
    val accent = LocalAccentColor.current
    val isDark = state.theme == "DARK"
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/mr-coder20/PortX"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 1. LANGUAGE SELECTOR
        SettingsSectionTitle(LocalizedStrings.get("language", state.language))
        var showLanguageMenu by remember { mutableStateOf(false) }
        val currentLang = Language.entries.find { it.code == state.language } ?: Language.EN
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(8.dp)) {
                Surface(
                    onClick = { showLanguageMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Text(currentLang.label, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextMuted)
                    }
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                    modifier = Modifier
                        .background(if (isDark) SurfaceDark.copy(alpha = 0.95f) else Color.White)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                ) {
                    val targetLanguages = listOf(Language.EN, Language.FA, Language.RU, Language.ZH)
                    targetLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    lang.label, 
                                    color = if (state.language == lang.code) accent else if (isDark) Color.White else Color.Black 
                                ) 
                            },
                            onClick = {
                                settingsManager.updateLanguage(lang.code)
                                showLanguageMenu = false
                            },
                            leadingIcon = {
                                if (state.language == lang.code) {
                                    Icon(Icons.Default.Check, null, tint = accent, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. APPEARANCE (Theme & Color)
        SettingsSectionTitle(LocalizedStrings.get("theme", state.language))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (state.theme == "DARK") LocalizedStrings.get("dark", state.language)
                        else LocalizedStrings.get("light", state.language),
                        color = if (state.theme == "DARK") Color.White else Color.Black
                    )
                    Switch(
                        checked = state.theme == "DARK",
                        onCheckedChange = { settingsManager.updateTheme(if(it) "DARK" else "LIGHT") },
                        colors = SwitchDefaults.colors(checkedThumbColor = accent)
                    )
                }
                
                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    LocalizedStrings.get("accent", state.language),
                    style = MaterialTheme.typography.labelSmall, 
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val colors = listOf(
                        Color(0xFF00D1FF), // Blue
                        Color(0xFFBD00FF), // Purple
                        Color(0xFF00FFA3), // Green
                        Color(0xFFF0D400), // Yellow
                        Color(0xFFFF4B4B)  // Red
                    )
                    colors.forEach { color ->
                        ColorCircle(
                            color = color, 
                            isSelected = state.accentColor == color,
                            onClick = { settingsManager.updateAccentColor(color) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. COMMUNICATION
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CommunicationGlassButton("About", Icons.Default.Info, Modifier.weight(1f)) { uriHandler.openUri(githubUrl) }
            CommunicationGlassButton("Support", Icons.Default.HeadsetMic, Modifier.weight(1f)) { uriHandler.openUri(githubUrl) }
            CommunicationGlassButton("Feedback", Icons.Default.Feedback, Modifier.weight(1f)) { uriHandler.openUri(githubUrl) }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PortX Professional Suite v1.0.0", color = if (isDark) TextSecondary else Color.Black.copy(alpha = 0.6f), fontSize = 12.sp)
                Icon(Icons.Default.Code, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            color = if (isDark) TextMuted else Color.Black.copy(alpha = 0.5f), 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
    )
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (isSelected) 3.dp else 0.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            .clickable { onClick() }
    )
}

@Composable
fun CommunicationGlassButton(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = LocalAccentColor.current
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isDark) GlassBorder else GlassBorderLight, RoundedCornerShape(16.dp)),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f),
        onClick = onClick
    ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    label, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = if (isDark) Color.White else Color.Black
                )
            }
    }
}
