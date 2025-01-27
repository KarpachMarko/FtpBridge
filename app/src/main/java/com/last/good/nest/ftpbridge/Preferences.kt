package com.last.good.nest.ftpbridge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

interface IPreferences {

    companion object {
        private const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

        fun of(context: Context) = DataStorePreferences(createDataStore(context))

        private fun createDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.createWithPath {
                context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath()
            }
        }

    }

    val port: Flow<Int>
    suspend fun setPort(port: Int?)
}

class DataStorePreferences(private val prefDataStore: DataStore<Preferences>) : IPreferences {
    private val ftpServerPortKey get() = intPreferencesKey("ftp_server_port")

    override val port get() = prefDataStore.data.map { it[ftpServerPortKey] ?: 21 }

    override suspend fun setPort(port: Int?) {
        prefDataStore.edit {
            if (port == null) {
                it.remove(ftpServerPortKey)
                return@edit
            }
            it[ftpServerPortKey] = port
        }
    }}
