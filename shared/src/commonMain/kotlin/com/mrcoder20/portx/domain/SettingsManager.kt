package com.mrcoder20.portx.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mrcoder20.portx.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppSettings(
    val language: String = "en",
    val theme: String = "DARK",
    val accentColor: Color = Color(0xFF00D1FF)
)

class SettingsManager(private val database: AppDatabase) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val queries = database.appDatabaseQueries

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            val dbSettings = queries.getSettings().executeAsOneOrNull()
            if (dbSettings != null) {
                _settings.update {
                    it.copy(
                        language = dbSettings.language,
                        theme = dbSettings.theme,
                        accentColor = Color(dbSettings.accentColor)
                    )
                }
            }
        }
    }

    fun updateLanguage(lang: String) {
        _settings.update { it.copy(language = lang) }
        saveToDb()
    }

    fun updateTheme(theme: String) {
        _settings.update { it.copy(theme = theme) }
        saveToDb()
    }

    fun updateAccentColor(color: Color) {
        _settings.update { it.copy(accentColor = color) }
        saveToDb()
    }

    private fun saveToDb() {
        scope.launch {
            val current = _settings.value
            queries.upsertSettings(
                language = current.language,
                theme = current.theme,
                accentColor = current.accentColor.toArgb().toLong()
            )
        }
    }
}
