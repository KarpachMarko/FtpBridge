package com.last.good.nest.ftpbridge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.last.good.nest.ftpbridge.IPreferences
import com.last.good.nest.ftpbridge.MainActivity
import com.last.good.nest.ftpbridge.model.FileSyncState.State.ERROR
import com.last.good.nest.ftpbridge.model.FileSyncState.State.LOCKED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.READY_TO_SYNC
import com.last.good.nest.ftpbridge.model.FileSyncState.State.REMOVED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.SYNCED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.SYNC_IN_PROGRESS
import com.last.good.nest.ftpbridge.model.ServiceState
import com.last.good.nest.ftpbridge.repository.FileSyncStateRepository
import com.last.good.nest.ftpbridge.utils.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
        private const val SYNC_DELETE_AFTER_SYNCED_EXTRA = "sync_delete_after_synced"
        private const val SYNC_USE_TMP_DIR_EXTRA = "sync_use_tmp_dir"
        private const val SYNC_ROOT_DIR_EXTRA = "sync_root_dir"
        private const val SYNC_SMB_SERVER_ADDRESS_EXTRA = "sync_smb_server_address"
        private const val SYNC_SMB_SERVER_PORT_EXTRA = "sync_smb_server_port"
        private const val SYNC_SMB_SHARE_NAME_EXTRA = "sync_smb_share_name"
        private const val SYNC_SMB_USERNAME_EXTRA = "sync_smb_username"
        private const val SYNC_SMB_PASSWORD_EXTRA = "sync_smb_password"
        private const val SYNC_SMB_REMOTE_DIR_EXTRA = "sync_smb_remote_dir"

        private const val SYNC_DEFAULT_DELETE_AFTER_SYNCED = true
        private const val SYNC_DEFAULT_USE_TMP_DIR = true

        var serviceState = mutableStateOf(ServiceState.NOT_RUNNING)
        fun getState(): ServiceState = serviceState.value
        fun getTempDir(context: Context) = BridgeFtpServer.getTempDir(context)
        fun getDefaultIntent(context: Context) = Intent(context, SyncService::class.java).apply {
            putExtra(SYNC_DELETE_AFTER_SYNCED_EXTRA, SYNC_DEFAULT_DELETE_AFTER_SYNCED)
            putExtra(SYNC_USE_TMP_DIR_EXTRA, SYNC_DEFAULT_USE_TMP_DIR)
        }

        suspend fun getIntentFromPrefs(context: Context, prefs: IPreferences) =
            Intent(context, SyncService::class.java).apply {
                putExtra(SYNC_DELETE_AFTER_SYNCED_EXTRA, prefs.deleteAfterSynced.first())
                putExtra(SYNC_USE_TMP_DIR_EXTRA, prefs.useTmpDir.first())
                putExtra(SYNC_ROOT_DIR_EXTRA, prefs.rootDirectory.first().toString())
                putExtra(SYNC_SMB_SERVER_ADDRESS_EXTRA, prefs.smbServerAddress.first())
                putExtra(SYNC_SMB_SERVER_PORT_EXTRA, prefs.smbServerPort.first())
                putExtra(SYNC_SMB_SHARE_NAME_EXTRA, prefs.smbShareName.first())
                putExtra(SYNC_SMB_USERNAME_EXTRA, prefs.smbUsername.first())
                putExtra(SYNC_SMB_PASSWORD_EXTRA, prefs.smbPassword.first())
                putExtra(SYNC_SMB_REMOTE_DIR_EXTRA, prefs.smbRemoteDestinationDirectory.first())
            }
    }

    private val syncNotification = Notifications.Constant.SYNC_FG_SERVICE

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private val syncRepository = FileSyncStateRepository()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(syncNotification.id, createNotification())
        serviceState.value = ServiceState.RUNNING
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requireNotNull(intent)
        val useTmpDir =
            intent.getBooleanExtra(
                SYNC_USE_TMP_DIR_EXTRA,
                SYNC_DEFAULT_USE_TMP_DIR
            )
        val deleteAfterSync = intent.getBooleanExtra(
            SYNC_DELETE_AFTER_SYNCED_EXTRA,
            SYNC_DEFAULT_DELETE_AFTER_SYNCED
        )
        val rootDirPath = intent.getStringExtra(SYNC_ROOT_DIR_EXTRA)
        val rootDir = if (useTmpDir) {
            getTempDir(applicationContext)
        } else {
            if (rootDirPath != null) File(rootDirPath) else getTempDir(applicationContext)
        }

        check(rootDir.exists())

        val smbServerAddress = intent.getStringExtra(SYNC_SMB_SERVER_ADDRESS_EXTRA)
        checkNotNull(smbServerAddress)
        val smbServerPort = intent.getIntExtra(SYNC_SMB_SERVER_PORT_EXTRA, 0)
        check(smbServerPort > 0)
        val smbShareName = intent.getStringExtra(SYNC_SMB_SHARE_NAME_EXTRA)
        checkNotNull(smbShareName)
        val smbUsername = intent.getStringExtra(SYNC_SMB_USERNAME_EXTRA)
        checkNotNull(smbUsername)
        val smbPassword = intent.getStringExtra(SYNC_SMB_PASSWORD_EXTRA)
        checkNotNull(smbPassword)
        val smbRemoteDir = intent.getStringExtra(SYNC_SMB_REMOTE_DIR_EXTRA)
        checkNotNull(smbRemoteDir)

        val fileTransferManager = FileTransferManager(
            smbServerAddress,
            smbServerPort,
            smbShareName,
            smbUsername,
            smbPassword,
            smbRemoteDir
        )

        serviceScope.launch {
            // Add already existing files to sync repository2
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

                    watchKey.reset()
                }
            }

            launch { // Sync files that are ready to sync
                while (isActive) {
                    syncRepository.updateStates(readyToSync = 1.toDuration(DurationUnit.SECONDS))
                    delay(200.toDuration(DurationUnit.MILLISECONDS))

                    println(
                        """READY_TO_SYNC = ${syncRepository.getFilesWithState(READY_TO_SYNC)}                        
                    SYNCING = ${syncRepository.getFilesWithState(SYNC_IN_PROGRESS)}""".trimIndent()
                    )

                    syncRepository.getFilesWithState(READY_TO_SYNC).keys.forEach {
                        syncRepository.setFileState(it, SYNC_IN_PROGRESS)
                        val syncSuccess = fileTransferManager.uploadFile(File(it))
                        syncRepository.setFileState(it, if (syncSuccess) SYNCED else ERROR)
                    }

                    if (deleteAfterSync) {
                        syncRepository.getFilesWithState(SYNCED).keys
                            .forEach {
                                Path(it).deleteExisting()
                                syncRepository.setFileState(it, REMOVED)
                            }

                        syncRepository.clearRemoved()
                    }
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
        val channelExisting = notificationManager.getNotificationChannel(syncNotification.channelId)

        if (channelExisting == null) {
            val notificationChannel = NotificationChannel(
                syncNotification.channelId,
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

        return NotificationCompat.Builder(this, syncNotification.channelId)
            .setContentTitle("Sync Bridge")
            .setContentText("Sync process is running")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .build()
    }
}

