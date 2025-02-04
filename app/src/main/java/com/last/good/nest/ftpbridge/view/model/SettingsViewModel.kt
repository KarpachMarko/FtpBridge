package com.last.good.nest.ftpbridge.view.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.last.good.nest.ftpbridge.IPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: IPreferences? = null
) : ViewModel() {

    private val _portFlow = prefs?.port ?: flowOf(2121)
    private val _deleteAfterSyncedFlow = prefs?.deleteAfterSynced ?: flowOf(true)
    private val _useTempDirectoryFlow = prefs?.useTmpDir ?: flowOf(true)
    private val _rootDirectoryFlow = prefs?.rootDirectory ?: flowOf(null)
    private val _serverAddressFlow = prefs?.serverAddress ?: flowOf(null)
    private val _serverPortFlow = prefs?.serverPort ?: flowOf(null)
    private val _shareNameFlow = prefs?.shareName ?: flowOf(null)
    private val _userNameFlow = prefs?.userName ?: flowOf(null)
    private val _passwordFlow = prefs?.password ?: flowOf(null)
    private val _remoteDestinationDirectoryFlow = prefs?.remoteDestinationDirectory ?: flowOf(null)

    private var _portNumber = MutableStateFlow("")
    val portNumber = _portNumber.asStateFlow()
    fun setPortNumber(value: String) {
        viewModelScope.launch {
            value.toIntOrNull()?.let { port -> prefs?.setPort(port) }
            if (value.isBlank()) {
                prefs?.setPort(null)
            }
        }
        _portNumber.value = value
    }

    var deleteAfterSynced = _deleteAfterSyncedFlow
    fun setDeleteAfterSynced(value: Boolean) {
        viewModelScope.launch {
            prefs?.setDeleteAfterSynced(value)
        }
    }

    val useTmpDir = _useTempDirectoryFlow
    fun setUseTmpDir(value: Boolean) {
        viewModelScope.launch {
            prefs?.setUseTmpDir(value)
        }
    }

    val rootDir = _rootDirectoryFlow

    private var _serverAddress = MutableStateFlow("")
    val serverAddress = _serverAddress.asStateFlow()
    fun setServerAddress(value: String) {
        viewModelScope.launch {
            if (value.isBlank()) {
                prefs?.setServerAddress(null)
            } else {
                prefs?.setServerAddress(value)
            }
        }
        _serverAddress.value = value
    }

    private var _serverPort = MutableStateFlow("")
    val serverPort = _serverPort.asStateFlow()
    fun setServerPort(value: String) {
        viewModelScope.launch {
            value.toIntOrNull()?.let { port -> prefs?.setServerPort(port) }
            if (value.isBlank()) {
                prefs?.setServerPort(null)
            }
        }
        _serverPort.value = value
    }

    private var _shareName = MutableStateFlow("")
    val shareName = _shareName.asStateFlow()
    fun setShareName(value: String) {
        viewModelScope.launch {
            prefs?.setShareName(value)
        }
        _shareName.value = value
    }

    private var _username = MutableStateFlow("")
    val username = _username.asStateFlow()
    fun setUsername(value: String) {
        viewModelScope.launch {
            prefs?.setUsername(value)
        }
        _username.value = value
    }

    private var _password = MutableStateFlow("")
    val password = _password.asStateFlow()
    fun setPassword(values: String) {
        viewModelScope.launch {
            prefs?.setPassword(values)
        }
        _password.value = values
    }

    private var _remoteDestinationDirectory = MutableStateFlow("")
    val remoteDestinationDirectory = _remoteDestinationDirectory.asStateFlow()
    fun setRemoteDestinationDirectory(value: String) {
        viewModelScope.launch {
            prefs?.setRemoteDestinationDirectory(value)
        }
        _remoteDestinationDirectory.value = value
    }

    init {
        viewModelScope.launch {
            _portNumber.value = _portFlow.firstOrNull()?.toString() ?: ""
            _serverAddress.value = _serverAddressFlow.firstOrNull()?.toString() ?: ""
            _serverPort.value = _serverPortFlow.firstOrNull()?.toString() ?: ""
            _shareName.value = _shareNameFlow.firstOrNull()?.toString() ?: ""
            _username.value = _userNameFlow.firstOrNull()?.toString() ?: ""
            _password.value = _passwordFlow.firstOrNull()?.toString() ?: ""
            _remoteDestinationDirectory.value =
                _remoteDestinationDirectoryFlow.firstOrNull()?.toString() ?: ""
        }
    }
}