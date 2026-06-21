package com.mrcoder20.portx.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrcoder20.portx.domain.LocalizedStrings
import com.mrcoder20.portx.domain.model.ScanResult
import com.mrcoder20.portx.presentation.ui.theme.*
import com.mrcoder20.portx.presentation.viewmodel.ReportsViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = koinInject()) {
    val state by viewModel.uiState.collectAsState()
    val appSettings = LocalAppSettings.current
    val accent = LocalAccentColor.current
    val lang = appSettings.language
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            LocalizedStrings.get("reports", lang).uppercase(), 
                            style = MaterialTheme.typography.labelMedium.copy(color = if (appSettings.theme == "DARK") TextMuted else TextMutedLight, fontWeight = FontWeight.Bold),
                        )
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, null, tint = DangerNeon.copy(alpha = 0.7f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Security Trend", style = MaterialTheme.typography.bodySmall, color = if (appSettings.theme == "DARK") TextMuted else TextMutedLight)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ScanScoreTrendChart(
                        scans = state.scans,
                        accent = accent,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = accent)
                        }
                    } else if (state.scans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(LocalizedStrings.get("no_services", lang), color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.scans) { scan ->
                                ReportHistoryItem(
                                    scan = scan,
                                    accent = accent,
                                    onDelete = { scan.id?.let { viewModel.deleteScan(it) } },
                                    onExport = { viewModel.shareScan(scan) },
                                    onDownload = { viewModel.downloadScan(scan) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "GLOBAL EXPORT FORMAT", 
                style = MaterialTheme.typography.labelMedium.copy(color = if (appSettings.theme == "DARK") TextMuted else TextMutedLight, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val formats = listOf("MD", "CSV", "JSON")
                formats.forEach { format ->
                    GlassExportButton(
                        label = if(format == "MD") "Markdown" else format, 
                        isSelected = state.exportFormat == format,
                        accent = accent,
                        onClick = { viewModel.onFormatChange(format) },
                        modifier = Modifier.weight(1f)
                    )
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

    if (showDeleteConfirm) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val isDark = LocalAppSettings.current.theme == "DARK"
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = if (isDark) SurfaceDark else SurfaceLight,
                titleContentColor = if (isDark) Color.White else Color.Black,
                textContentColor = if (isDark) TextSecondary else TextSecondaryLight,
                title = { Text("Clear History?") },
                text = { Text("Permanently delete all records?") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.clearAll()
                        showDeleteConfirm = false
                    }) {
                        Text("CLEAR ALL", color = if (isDark) DangerNeon else DangerLight)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("CANCEL", color = if (isDark) Color.White else Color.Black)
                    }
                },
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@Composable
fun ScanScoreTrendChart(scans: List<ScanResult>, accent: Color, modifier: Modifier = Modifier) {
    val scores = scans.sortedBy { it.timestamp }.map { it.securityScore.toFloat() / 100f }.takeLast(10)
    if (scores.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("NOT ENOUGH DATA", color = TextMuted, fontSize = 10.sp)
        }
        return
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (scores.size - 1)
        
        val points = scores.mapIndexed { index, score ->
            Offset(index * spacing, height * (1 - score))
        }

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(SecondaryNeon, accent)),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ReportHistoryItem(
    scan: ScanResult,
    accent: Color,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onDownload: () -> Unit
) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    val dateTime = kotlin.time.Instant.fromEpochMilliseconds(scan.timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Target: ${scan.target}", style = MaterialTheme.typography.bodyMedium, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                Text("${dateTime.date} ${dateTime.time.hour}:${dateTime.time.minute}", style = MaterialTheme.typography.bodySmall, color = if (isDark) TextMuted else TextMutedLight)
            }
            
            Text(
                "${scan.securityScore}%", 
                color = if (scan.securityScore > 70) TertiaryNeon else (if (isDark) DangerNeon else DangerLight),
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, null, tint = accent, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onExport) {
                Icon(Icons.Default.Share, null, tint = SecondaryNeon, modifier = Modifier.size(20.dp))
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, null, tint = if (isDark) TextMuted else TextMutedLight, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun GlassExportButton(
    label: String, 
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalAppSettings.current.theme == "DARK"
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) accent else GlassBorder, RoundedCornerShape(12.dp)),
        color = if (isSelected) accent.copy(alpha = 0.15f) else GlassBackground
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, 
                style = MaterialTheme.typography.labelLarge, 
                color = if (isSelected) (if (isDark) Color.White else Color.Black) else (if (isDark) TextMuted else TextMutedLight)
            )
        }
    }
}
