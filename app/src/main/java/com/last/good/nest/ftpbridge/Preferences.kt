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
    suspend fun setPort(port: Int?)

    val deleteAfterSynced: Flow<Boolean>
    suspend fun setDeleteAfterSynced(deleteAfterSynced: Boolean)

    val useTmpDir: Flow<Boolean>
    suspend fun setUseTmpDir(useTmpDir: Boolean)

    val rootDirectory: Flow<File?>
    suspend fun setRootDirectory(rootDir: File?)

    val serverAddress: Flow<String?>
    suspend fun setServerAddress(address: String?)

    val serverPort: Flow<Int?>
    suspend fun setServerPort(port: Int?)

    val shareName: Flow<String?>
    suspend fun setShareName(name: String?)

    val userName: Flow<String?>
    suspend fun setUsername(userName: String?)

    val password: Flow<String?>
    suspend fun setPassword(password: String?)

    val remoteDestinationDirectory: Flow<String?>
    suspend fun setRemoteDestinationDirectory(dir: String?)
}

class DataStorePreferences(
    private val prefDataStore: DataStore<Preferences>
) : IPreferences {
    private val ftpServerPortKey get() = intPreferencesKey("ftp_server_port")
    private val deleteAfterSyncedKey get() = booleanPreferencesKey("delete_after_synced")
    private val useTmpDirKey get() = booleanPreferencesKey("use_tmp_dir")
    private val rootDirectoryKey get() = stringPreferencesKey("root_directory")
    private val serverAddressKey get() = stringPreferencesKey("serverAddress")
    private val serverPortKey get() = intPreferencesKey("serverPort")
    private val shareNameKey get() = stringPreferencesKey("shareName")
    private val usernameKey get() = stringPreferencesKey("username")
    private val passwordKey get() = stringPreferencesKey("password")
    private val remoteDestinationDirKey get() = stringPreferencesKey("remoteDestinationDir")

    override val port get() = getVal(ftpServerPortKey, 2121)
    override suspend fun setPort(port: Int?) = setVal(ftpServerPortKey, port)

    override val deleteAfterSynced get() = getVal(deleteAfterSyncedKey, true)
    override suspend fun setDeleteAfterSynced(deleteAfterSynced: Boolean) =
        setVal(deleteAfterSyncedKey, deleteAfterSynced)

    override val useTmpDir: Flow<Boolean> get() = getVal(useTmpDirKey, true)
    override suspend fun setUseTmpDir(useTmpDir: Boolean) = setVal(useTmpDirKey, useTmpDir)

    override val rootDirectory: Flow<File?> = getVal(rootDirectoryKey).map { it?.toFile() }
    override suspend fun setRootDirectory(rootDir: File?) =
        setVal(rootDirectoryKey, rootDir.toString())

    override val serverAddress: Flow<String?> get() = getVal(serverAddressKey)
    override suspend fun setServerAddress(address: String?) = setVal(serverAddressKey, address)

    override val serverPort: Flow<Int?> get() = getVal(serverPortKey)
    override suspend fun setServerPort(port: Int?) = setVal(serverPortKey, port)

    override val shareName: Flow<String?> get() = getVal(shareNameKey)
    override suspend fun setShareName(name: String?) = setVal(shareNameKey, name)

    override val userName: Flow<String?> get() = getVal(usernameKey)
    override suspend fun setUsername(userName: String?) = setVal(usernameKey, userName)

    override val password: Flow<String?> get() = getVal(passwordKey)
    override suspend fun setPassword(password: String?) = setVal(passwordKey, password)

    override val remoteDestinationDirectory: Flow<String?> get() = getVal(remoteDestinationDirKey)
    override suspend fun setRemoteDestinationDirectory(dir: String?) = setVal(remoteDestinationDirKey, dir)

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
