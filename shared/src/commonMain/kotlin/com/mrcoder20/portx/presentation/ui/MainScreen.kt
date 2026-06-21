package com.mrcoder20.portx.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.mrcoder20.portx.domain.SettingsManager
import com.mrcoder20.portx.presentation.viewmodel.ScanViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val viewModel: ScanViewModel = koinInject()
    val settingsManager: SettingsManager = koinInject()
    val settings by settingsManager.settings.collectAsState()

    NavigationWrapper(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        settings = settings,
        onMinimize = onMinimize,
        onMaximize = onMaximize,
        onClose = onClose
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it / 2 }))
                    .togetherWith(fadeOut(animationSpec = tween(400)))
            }
        ) { tab ->
            when (tab) {
                0 -> DashboardScreen(viewModel)
                1 -> ToolsScreen()
                2 -> ReportsScreen()
                3 -> SettingsScreen()
            }
        }
    }
}
