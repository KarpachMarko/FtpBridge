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

    private val _ftpPortFlow = prefs?.port ?: flowOf(2121)
    private val _deleteAfterSyncedFlow = prefs?.deleteAfterSynced ?: flowOf(true)
    private val _useTempDirectoryFlow = prefs?.useTmpDir ?: flowOf(true)
    private val _rootDirectoryFlow = prefs?.rootDirectory ?: flowOf(null)
    private val _ftpAllowAnonymousFlow = prefs?.ftpAllowAnonymous ?: flowOf(false)
    private val _ftpUsernameFlow = prefs?.ftpUsername ?: flowOf(null)
    private val _ftpPasswordFlow = prefs?.ftpPassword ?: flowOf(null)
    private val _smbServerAddressFlow = prefs?.smbServerAddress ?: flowOf(null)
    private val _smbServerPortFlow = prefs?.smbServerPort ?: flowOf(null)
    private val _smbShareNameFlow = prefs?.smbShareName ?: flowOf(null)
    private val _smbUsernameFlow = prefs?.smbUsername ?: flowOf(null)
    private val _smbPasswordFlow = prefs?.smbPassword ?: flowOf(null)
    private val _smbRemoteDestinationDirectoryFlow = prefs?.smbRemoteDestinationDirectory ?: flowOf(null)

    private var _ftpPortNumber = MutableStateFlow("")
    val ftpServerPort = _ftpPortNumber.asStateFlow()
    fun setPortNumber(value: String) {
        viewModelScope.launch {
            value.toIntOrNull()?.let { port -> prefs?.setFtpPort(port) }
            if (value.isBlank()) {
                prefs?.setFtpPort(null)
            }
        }
        _ftpPortNumber.value = value
    }

    val deleteAfterSynced = _deleteAfterSyncedFlow
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

    val ftpAllowAnonymous = _ftpAllowAnonymousFlow
    fun setFtpAllowAnonymous(value: Boolean) {
        viewModelScope.launch {
            prefs?.setAllowAnonymous(value)
        }
    }

    private var _ftpUsername = MutableStateFlow("")
    val ftpUsername = _ftpUsername.asStateFlow()
    fun setFtpUsername(value: String) {
        viewModelScope.launch {
            prefs?.setFtpUsername(value)
        }
        _ftpUsername.value = value
    }

    private var _ftpPassword = MutableStateFlow("")
    val ftpPassword = _ftpPassword.asStateFlow()
    fun setFtpPassword(value: String) {
        viewModelScope.launch {
            prefs?.setFtpPassword(value)
        }
        _ftpPassword.value = value
    }

    private var _smbServerAddress = MutableStateFlow("")
    val smbServerAddress = _smbServerAddress.asStateFlow()
    fun setSmbServerAddress(value: String) {
        viewModelScope.launch {
            if (value.isBlank()) {
                prefs?.setSmbServerAddress(null)
            } else {
                prefs?.setSmbServerAddress(value)
            }
        }
        _smbServerAddress.value = value
    }

    private var _smbServerPort = MutableStateFlow("")
    val smbServerPort = _smbServerPort.asStateFlow()
    fun setSmbServerPort(value: String) {
        viewModelScope.launch {
            value.toIntOrNull()?.let { port -> prefs?.setSmbServerPort(port) }
            if (value.isBlank()) {
                prefs?.setSmbServerPort(null)
            }
        }
        _smbServerPort.value = value
    }

    private var _smbShareName = MutableStateFlow("")
    val smbShareName = _smbShareName.asStateFlow()
    fun setSmbShareName(value: String) {
        viewModelScope.launch {
            prefs?.setSmbShareName(value)
        }
        _smbShareName.value = value
    }

    private var _smbUsername = MutableStateFlow("")
    val smbUsername = _smbUsername.asStateFlow()
    fun setSmbUsername(value: String) {
        viewModelScope.launch {
            prefs?.setSmbUsername(value)
        }
        _smbUsername.value = value
    }

    private var _smbPassword = MutableStateFlow("")
    val smbPassword = _smbPassword.asStateFlow()
    fun setSmbPassword(values: String) {
        viewModelScope.launch {
            prefs?.setSmbPassword(values)
        }
        _smbPassword.value = values
    }

    private var _smbRemoteDestinationDirectory = MutableStateFlow("")
    val smbRemoteDir = _smbRemoteDestinationDirectory.asStateFlow()
    fun setSmbRemoteDir(value: String) {
        viewModelScope.launch {
            prefs?.setSmbRemoteDestinationDirectory(value)
        }
        _smbRemoteDestinationDirectory.value = value
    }

    init {
        viewModelScope.launch {
            _ftpPortNumber.value = _ftpPortFlow.firstOrNull()?.toString() ?: ""
            _ftpUsername.value = _ftpUsernameFlow.firstOrNull()?.toString() ?: ""
            _ftpPassword.value = _ftpPasswordFlow.firstOrNull()?.toString() ?: ""
            _smbServerAddress.value = _smbServerAddressFlow.firstOrNull()?.toString() ?: ""
            _smbServerPort.value = _smbServerPortFlow.firstOrNull()?.toString() ?: ""
            _smbShareName.value = _smbShareNameFlow.firstOrNull()?.toString() ?: ""
            _smbUsername.value = _smbUsernameFlow.firstOrNull()?.toString() ?: ""
            _smbPassword.value = _smbPasswordFlow.firstOrNull()?.toString() ?: ""
            _smbRemoteDestinationDirectory.value =
                _smbRemoteDestinationDirectoryFlow.firstOrNull()?.toString() ?: ""
        }
    }
}