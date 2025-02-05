package com.last.good.nest.ftpbridge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.last.good.nest.ftpbridge.utils.FileUtils.toFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import java.io.File

interface IPreferences {

    companion object {
        private const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

        private var instance: DataStorePreferences? = null

        fun of(context: Context): DataStorePreferences {
            return instance ?: synchronized(this) {
                instance ?: DataStorePreferences(createDataStore(context)).also { instance = it }
            }
        }

        private fun createDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.createWithPath {
                context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath()
            }
        }
    }

    val port: Flow<Int>
    suspend fun setFtpPort(port: Int?)

    val deleteAfterSynced: Flow<Boolean>
    suspend fun setDeleteAfterSynced(deleteAfterSynced: Boolean)

    val useTmpDir: Flow<Boolean>
    suspend fun setUseTmpDir(useTmpDir: Boolean)

    val rootDirectory: Flow<File?>
    suspend fun setRootDirectory(rootDir: File?)

    val ftpAllowAnonymous: Flow<Boolean>
    suspend fun setAllowAnonymous(allowAnonymous: Boolean)

    val ftpUsername: Flow<String?>
    suspend fun setFtpUsername(userName: String?)

    val ftpPassword: Flow<String?>
    suspend fun setFtpPassword(password: String?)

    val smbServerAddress: Flow<String?>
    suspend fun setSmbServerAddress(address: String?)

    val smbServerPort: Flow<Int?>
    suspend fun setSmbServerPort(port: Int?)

    val smbShareName: Flow<String?>
    suspend fun setSmbShareName(name: String?)

    val smbUsername: Flow<String?>
    suspend fun setSmbUsername(userName: String?)

    val smbPassword: Flow<String?>
    suspend fun setSmbPassword(password: String?)

    val smbRemoteDestinationDirectory: Flow<String?>
    suspend fun setSmbRemoteDestinationDirectory(dir: String?)
}

class DataStorePreferences(
    private val prefDataStore: DataStore<Preferences>
) : IPreferences {
    private val ftpServerPortKey get() = intPreferencesKey("ftp_server_port")
    private val deleteAfterSyncedKey get() = booleanPreferencesKey("delete_after_synced")
    private val useTmpDirKey get() = booleanPreferencesKey("use_tmp_dir")
    private val rootDirectoryKey get() = stringPreferencesKey("root_directory")
    private val ftpAllowAnonymousKey get() = booleanPreferencesKey("ftp_allow_anonymous")
    private val ftpUsernameKey get() = stringPreferencesKey("ftp_username")
    private val ftpPasswordKey get() = stringPreferencesKey("ftp_password")
    private val serverAddressKey get() = stringPreferencesKey("serverAddress")
    private val serverPortKey get() = intPreferencesKey("serverPort")
    private val shareNameKey get() = stringPreferencesKey("shareName")
    private val usernameKey get() = stringPreferencesKey("username")
    private val passwordKey get() = stringPreferencesKey("password")
    private val remoteDestinationDirKey get() = stringPreferencesKey("remoteDestinationDir")

    override val port get() = getVal(ftpServerPortKey, 2121)
    override suspend fun setFtpPort(port: Int?) = setVal(ftpServerPortKey, port)

    override val deleteAfterSynced get() = getVal(deleteAfterSyncedKey, true)
    override suspend fun setDeleteAfterSynced(deleteAfterSynced: Boolean) =
        setVal(deleteAfterSyncedKey, deleteAfterSynced)

    override val useTmpDir: Flow<Boolean> get() = getVal(useTmpDirKey, true)
    override suspend fun setUseTmpDir(useTmpDir: Boolean) = setVal(useTmpDirKey, useTmpDir)

    override val rootDirectory: Flow<File?> = getVal(rootDirectoryKey).map { it?.toFile() }
    override suspend fun setRootDirectory(rootDir: File?) =
        setVal(rootDirectoryKey, rootDir.toString())

    override val ftpAllowAnonymous: Flow<Boolean> get() = getVal(ftpAllowAnonymousKey, true)
    override suspend fun setAllowAnonymous(allowAnonymous: Boolean) =
        setVal(ftpAllowAnonymousKey, allowAnonymous)

    override val ftpUsername: Flow<String?> get() = getVal(ftpUsernameKey)
    override suspend fun setFtpUsername(userName: String?) = setVal(ftpUsernameKey, userName)

    override val ftpPassword: Flow<String?> get() = getVal(ftpPasswordKey)
    override suspend fun setFtpPassword(password: String?) = setVal(ftpPasswordKey, password)

    override val smbServerAddress: Flow<String?> get() = getVal(serverAddressKey)
    override suspend fun setSmbServerAddress(address: String?) = setVal(serverAddressKey, address)

    override val smbServerPort: Flow<Int?> get() = getVal(serverPortKey)
    override suspend fun setSmbServerPort(port: Int?) = setVal(serverPortKey, port)

    override val smbShareName: Flow<String?> get() = getVal(shareNameKey)
    override suspend fun setSmbShareName(name: String?) = setVal(shareNameKey, name)

    override val smbUsername: Flow<String?> get() = getVal(usernameKey)
    override suspend fun setSmbUsername(userName: String?) = setVal(usernameKey, userName)

    override val smbPassword: Flow<String?> get() = getVal(passwordKey)
    override suspend fun setSmbPassword(password: String?) = setVal(passwordKey, password)

    override val smbRemoteDestinationDirectory: Flow<String?> get() = getVal(remoteDestinationDirKey)
    override suspend fun setSmbRemoteDestinationDirectory(dir: String?) =
        setVal(remoteDestinationDirKey, dir)

    private fun <T> getVal(key: Preferences.Key<T>): Flow<T?> =
        prefDataStore.data.map { it[key] }

    private fun <T> getVal(key: Preferences.Key<T>, default: T): Flow<T> =
        prefDataStore.data.map { it[key] ?: default }

    private suspend fun <T> setVal(key: Preferences.Key<T>, value: T?) {
        prefDataStore.edit {
            if (value == null) {
                it.remove(key)
                return@edit
            }
            it[key] = value
        }
    }
}
