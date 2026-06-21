package com.mrcoder20.portx.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrcoder20.portx.domain.model.ScanResult
import com.mrcoder20.portx.domain.repository.ScanRepository
import com.mrcoder20.portx.domain.getFileSharer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportsUIState(
    val scans: List<ScanResult> = emptyList(),
    val exportFormat: String = "JSON",
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

class ReportsViewModel(
    private val scanRepository: ScanRepository,
    private val exportReportUseCase: com.mrcoder20.portx.domain.usecase.ExportReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUIState())
    val uiState: StateFlow<ReportsUIState> = _uiState.asStateFlow()

    private val fileSharer = getFileSharer()

    fun onFormatChange(format: String) {
        _uiState.update { it.copy(exportFormat = format) }
    }

    fun shareScan(scan: ScanResult) {
        val (_, content, fileName, mimeType) = prepareExport(scan)
        fileSharer.shareFile(content, fileName, mimeType)
    }

    fun downloadScan(scan: ScanResult) {
        val (_, content, fileName, mimeType) = prepareExport(scan)
        val path = fileSharer.downloadFile(content, fileName, mimeType)
        if (path != null) {
            showSnackbar("Saved to: $path")
        } else {
            showSnackbar("Download failed")
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = message) }
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun prepareExport(scan: ScanResult): ExportData {
        val format = _uiState.value.exportFormat
        val content = exportReportUseCase(scan, format)
        val extension = if (format == "MD") "md" else format.lowercase()
        val fileName = "PortX_Report_${scan.target}_${scan.timestamp}.$extension"
        val mimeType = when (format) {
            "CSV" -> "text/csv"
            "MD" -> "text/markdown"
            else -> "application/json"
        }
        return ExportData(format, content, fileName, mimeType)
    }

    private data class ExportData(val format: String, val content: String, val fileName: String, val mimeType: String)

    init {
        loadScans()
    }

    fun loadScans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            scanRepository.getAllScans().collect { scans ->
                _uiState.update { it.copy(scans = scans, isLoading = false) }
            }
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            scanRepository.deleteScan(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            scanRepository.deleteAllScans()
        }
    }
}
