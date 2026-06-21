package com.mrcoder20.portx

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.foundation.window.WindowDraggableArea
import org.jetbrains.compose.resources.painterResource
import com.mrcoder20.portx.shared.Res
import com.mrcoder20.portx.shared.ic1
import com.mrcoder20.portx.di.initKoin

fun main() {
    initKoin()
    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
        val icon = painterResource(Res.drawable.ic1)
        
        Window(
            onCloseRequest = ::exitApplication,
            title = "PortX",
            state = windowState,
            undecorated = true, // Removes standard title bar and borders
            icon = icon
        ) {
            WindowDraggableArea {
                App(
                    onMinimize = { windowState.isMinimized = true },
                    onMaximize = { 
                        windowState.placement = if (windowState.placement == WindowPlacement.Maximized) 
                            WindowPlacement.Floating else WindowPlacement.Maximized 
                    },
                    onClose = { exitApplication() }
                )
            }
        }
    }
}
