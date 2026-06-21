package com.mrcoder20.portx.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrcoder20.portx.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ToolsUIState(
    val pingResults: List<PingResult> = emptyList(),
    val dnsResults: List<String> = emptyList(),
    val whoisResult: String? = null,
    val localIp: LocalIpInfo? = null,
    val publicIp: String? = null,
    val isLoading: Boolean = false,
    val activeTool: String = "LOCAL",
    val target: String = "",
    val error: String? = null,
    val snackbarMessage: String? = null
)

class ToolsViewModel : ViewModel() {
    private val networkTools = getNetworkTools()
    private val clipboardManager = getClipboardManager()
    
    private val _uiState = MutableStateFlow(ToolsUIState())
    val uiState: StateFlow<ToolsUIState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    init {
        refreshLocalInfo()
    }

    fun onTargetChange(newTarget: String) {
        _uiState.update { it.copy(target = newTarget, error = null) }
    }

    fun selectTool(tool: String) {
        stopActiveTool()
        _uiState.update { it.copy(activeTool = tool, error = null, pingResults = emptyList(), dnsResults = emptyList(), whoisResult = null) }
        if (tool == "LOCAL") {
            if (_uiState.value.localIp == null || _uiState.value.publicIp == null) {
                refreshLocalInfo()
            }
        }
    }

    fun refreshLocalInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val info = networkTools.getLocalIpInfo()
                _uiState.update { it.copy(localIp = info) }
                
                val pubIp = networkTools.getPublicIp()
                _uiState.update { it.copy(publicIp = pubIp) }
            } catch (e: Exception) {
                // Keep existing info if refresh fails
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun runPing() {
        val rawHost = _uiState.value.target.trim()
        if (rawHost.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a target IP or Domain") }
            return
        }
        
        stopActiveTool()
        activeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pingResults = emptyList(), error = null) }
            try {
                networkTools.ping(rawHost).collect { res ->
                    _uiState.update { it.copy(pingResults = it.pingResults + res) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Engine failure: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun runDnsLookup() {
        val host = _uiState.value.target.trim()
        if (host.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a Domain to resolve") }
            return
        }

        stopActiveTool()
        activeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, dnsResults = emptyList(), error = null) }
            try {
                val results = networkTools.dnsLookup(host)
                if (results.isEmpty()) {
                    _uiState.update { it.copy(error = "No resolution found for $host. Check your internet.") }
                } else {
                    _uiState.update { it.copy(dnsResults = results) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Resolver error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun runWhois() {
        val host = _uiState.value.target.trim()
        if (host.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a domain (e.g. google.com)") }
            return
        }

        stopActiveTool()
        activeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, whoisResult = "Connecting to Authority Database...", error = null) }
            try {
                val result = networkTools.whois(host)
                _uiState.update { it.copy(whoisResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "WHOIS service unreachable.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun stopActiveTool() {
        activeJob?.cancel()
        activeJob = null
        _uiState.update { it.copy(isLoading = false) }
    }

    fun clearResults() {
        stopActiveTool()
        _uiState.update { it.copy(pingResults = emptyList(), dnsResults = emptyList(), whoisResult = null, error = null) }
    }

    fun copyResultsToClipboard() {
        val state = _uiState.value
        val text = when (state.activeTool) {
            "PING" -> state.pingResults.joinToString("\n") { it.message }
            "DNS" -> state.dnsResults.joinToString("\n")
            "WHOIS" -> state.whoisResult ?: ""
            "LOCAL" -> state.localIp?.let { "Internal IP: ${it.ipAddress}\nInterface: ${it.interfaceName}\nPublic IP: ${state.publicIp}" } ?: ""
            else -> ""
        }
        if (text.isNotBlank()) {
            clipboardManager.copyToClipboard(text)
            showSnackbar("Report copied to clipboard.")
        }
    }

    fun copyIndividualResult(text: String) {
        clipboardManager.copyToClipboard(text)
        showSnackbar("Copied: $text")
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = message) }
            kotlinx.coroutines.delay(2500)
            _uiState.update { it.copy(snackbarMessage = null) }
        }
    }
}
