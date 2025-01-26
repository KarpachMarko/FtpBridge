package com.last.good.nest.ftpbridge

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

class Preferences(context: Context) {

    companion object {
        private const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

        private fun createDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.createWithPath {
                context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath()
            }
        }
    }

    private val dataStore = createDataStore(context)

    private val ftpServerPortKey = intPreferencesKey("ftp_server_port")


    val port get() = dataStore.data.map { it[ftpServerPortKey] ?: 21 }

    suspend fun setPort(port: Int?) = dataStore.edit {
        if (port == null) {
          it.remove(ftpServerPortKey)
          return@edit
        }
        it[ftpServerPortKey] = port
    }
}
