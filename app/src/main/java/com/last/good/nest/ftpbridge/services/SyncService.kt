package com.last.good.nest.ftpbridge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.last.good.nest.ftpbridge.MainActivity
import com.last.good.nest.ftpbridge.model.FileSyncState.State.ERROR
import com.last.good.nest.ftpbridge.model.FileSyncState.State.LOCKED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.READY_TO_SYNC
import com.last.good.nest.ftpbridge.model.FileSyncState.State.REMOVED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.SYNCED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.SYNC_IN_PROGRESS
import com.last.good.nest.ftpbridge.model.ServiceState
import com.last.good.nest.ftpbridge.repository.FileSyncStateRepository
import com.last.good.nest.ftpbridge.services.BridgeFtpServer.Companion.TMP_SUB_FOLDER_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SyncService : Service() {

    companion object {
        const val CHANNEL_ID = "SyncService"
        const val NOTIFICATION_ID = 1

        var serviceState = mutableStateOf(ServiceState.NOT_RUNNING);
        fun getState(): ServiceState {
            return serviceState.value;
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val fileTransferManager = FileTransferManager()
    private val syncRepository = FileSyncStateRepository()

    private val rootDir by lazy { File(applicationContext.cacheDir, TMP_SUB_FOLDER_NAME) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        check(rootDir.exists())
        serviceState.value = ServiceState.RUNNING
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            // Add already existing files to sync repository
            rootDir.listFiles()
                ?.filter { it.isFile }
                ?.filter { !syncRepository.fileExists(it.absolutePath) }
                ?.forEach { syncRepository.newFile(it.absolutePath) }


            val watchService = FileSystems.getDefault().newWatchService()
            rootDir.toPath().register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )

            launch { // Looks for file changes and adds to repository accordingly
                while (isActive) {
                    val watchKey = watchService.take()
                    watchKey.pollEvents()
                        .forEach {
                            val file = File(rootDir, it.context().toString())
                            if (it.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                syncRepository.newFile(file.absolutePath)
                            } else if (it.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                syncRepository.setFileState(
                                    file.absolutePath,
                                    LOCKED
                                )
                            }
                        }

                    watchKey.reset();
                }
            }

            launch { // Sync files that are ready to sync
                while (isActive) {
                    syncRepository.updateStates(readyToSync = 1.toDuration(DurationUnit.SECONDS))
                    delay(200.toDuration(DurationUnit.MILLISECONDS))

                    println("""READY_TO_SYNC = ${syncRepository.getFilesWithState(READY_TO_SYNC)}                        
                    SYNCING = ${syncRepository.getFilesWithState(SYNC_IN_PROGRESS)}""".trimIndent())

                    syncRepository.getFilesWithState(READY_TO_SYNC).keys.forEach {
                        syncRepository.setFileState(it, SYNC_IN_PROGRESS)
                        val syncSuccess = trySyncFile(File(it))
                        syncRepository.setFileState(it, if (syncSuccess) SYNCED else ERROR)
                    }

                    syncRepository.getFilesWithState(SYNCED).keys
                        .forEach {
                            Path(it).deleteExisting()
                            syncRepository.setFileState(it, REMOVED)
                        }

                    syncRepository.clearRemoved()
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        serviceState.value = ServiceState.STOPPED
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channelExisting = notificationManager.getNotificationChannel(CHANNEL_ID)

        if (channelExisting == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Sync Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Sync To Remote Server"
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sync Bridge")
            .setContentText("Sync process is running")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .build()
    }

    private suspend fun trySyncFile(file: File): Boolean {
        return fileTransferManager.uploadFile(file)
    }
}

