package com.mrcoder20.portx.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcoder20.portx.domain.AppSettings
import com.mrcoder20.portx.domain.LocalizedStrings
import com.mrcoder20.portx.presentation.ui.theme.*

@Composable
fun NavigationWrapper(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    settings: com.mrcoder20.portx.domain.AppSettings,
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val accent = LocalAccentColor.current
    val lang = settings.language

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth > 600.dp

        if (isExpanded) {
            // Desktop UI
            Column(modifier = Modifier.fillMaxSize().background(if(settings.theme == "DARK") BackgroundDark else Color.White)) {
                // --- CUSTOM TOP TITLE BAR ---
                Surface(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    color = if(settings.theme == "DARK") SurfaceDark else SurfaceLight,
                    border = BorderStroke(0.5.dp, GlassBorder.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Radar, null, tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PortX Professional",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.theme == "DARK") Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WindowControlBtn(Icons.Default.Remove, if(settings.theme == "DARK") TextMuted else TextMutedLight, onMinimize)
                            WindowControlBtn(Icons.Default.AspectRatio, if(settings.theme == "DARK") TextMuted else TextMutedLight, onMaximize)
                            WindowControlBtn(Icons.Default.Close, DangerNeon, onClose)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LiquidGlowBackground()
                    Row(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier
                                .width(320.dp)
                                .fillMaxHeight()
                                .padding(20.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp)),
                            color = GlassBackground
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "PortX",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (settings.theme == "DARK") Color.White else Color.Black,
                                        shadow = Shadow(color = accent, blurRadius = 15f)
                                    ),
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                                
                                DesktopNavItem(Icons.Default.Home, LocalizedStrings.get("dashboard", lang), selectedTab == 0) { onTabSelected(0) }
                                DesktopNavItem(Icons.Default.Build, LocalizedStrings.get("tools", lang), selectedTab == 1) { onTabSelected(1) }
                                DesktopNavItem(Icons.AutoMirrored.Filled.List, LocalizedStrings.get("reports", lang), selectedTab == 2) { onTabSelected(2) }
                                DesktopNavItem(Icons.Default.Settings, LocalizedStrings.get("settings", lang), selectedTab == 3) { onTabSelected(3) }
                            }
                        }
                        
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            content()
                        }
                    }
                }
            }
        } else {
            // Mobile UI
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    InfiniteBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        lang = lang
                    )
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if(settings.theme == "DARK") BackgroundDark else Color.White)
                ) {
                    LiquidGlowBackground()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = padding.calculateBottomPadding())
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun InfiniteBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    lang: String
) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(if (isDark) Color.Transparent else GlassLight)
            .border(1.dp, (if (isDark) GlassBorder else GlassBorderLight).copy(alpha = 0.4f), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDark = LocalAppSettings.current.theme == "DARK"
            BottomNavItem(
                icon = Icons.Default.Home,
                label = LocalizedStrings.get("dashboard", lang),
                isSelected = selectedTab == 0,
                isDark = isDark,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Default.Build,
                label = LocalizedStrings.get("tools", lang),
                isSelected = selectedTab == 1,
                isDark = isDark,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = LocalizedStrings.get("reports", lang),
                isSelected = selectedTab == 2,
                isDark = isDark,
                onClick = { onTabSelected(2) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = LocalizedStrings.get("settings", lang),
                isSelected = selectedTab == 3,
                isDark = isDark,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun WindowControlBtn(icon: ImageVector, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.15f else 0.05f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(CircleShape)
            .background(color.copy(alpha = animatedAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = if (isHovered) 1f else 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val accent = LocalAccentColor.current
    
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (isSelected) {
                            Brush.linearGradient(listOf(accent.copy(alpha = 0.2f), accent.copy(alpha = 0.1f)))
                        } else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) accent else (if (isDark) TextMuted else TextMutedLight),
                    modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)
                )
            }
        }
    }
}

@Composable
fun DesktopNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = LocalAccentColor.current
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accent else (if (isDark) TextSecondary else TextSecondaryLight),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = if (isSelected) (if (isDark) Color.White else Color.Black) else (if (isDark) TextSecondary else TextSecondaryLight),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
