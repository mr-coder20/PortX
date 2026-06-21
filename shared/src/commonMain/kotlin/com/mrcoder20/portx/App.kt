package com.mrcoder20.portx

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrcoder20.portx.di.initKoin
import com.mrcoder20.portx.presentation.ui.MainScreen
import com.mrcoder20.portx.presentation.ui.theme.PortXTheme
import org.koin.compose.KoinContext

@Composable
fun App(
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    PortXTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(
                onMinimize = onMinimize,
                onMaximize = onMaximize,
                onClose = onClose
            )
        }
    }
}
