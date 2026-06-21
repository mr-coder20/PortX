package com.mrcoder20.portx.domain

import com.mrcoder20.portx.domain.model.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ScanManager {
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentResult = MutableStateFlow<ScanResult?>(null)
    val currentResult: StateFlow<ScanResult?> = _currentResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun updateProgress(value: Int) {
        _progress.value = value
    }

    fun setScanning(value: Boolean) {
        _isScanning.value = value
        if (value) {
            _error.value = null
            _progress.value = 0
            _currentResult.value = null
        }
    }

    fun setResult(result: ScanResult) {
        _currentResult.value = result
    }

    fun setError(message: String?) {
        _error.value = message
        _isScanning.value = false
    }
}
