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
import com.guichaguri.minimalftp.FTPServer
import com.guichaguri.minimalftp.impl.NativeFileSystem
import com.last.good.nest.ftpbridge.IPreferences
import com.last.good.nest.ftpbridge.MainActivity
import com.last.good.nest.ftpbridge.model.ServiceState
import com.last.good.nest.ftpbridge.utils.NetworkUtils
import com.last.good.nest.ftpbridge.utils.Notifications
import kotlinx.coroutines.flow.first
import java.io.File


class BridgeFtpServer : Service() {

    companion object {
        private const val TMP_SUB_FOLDER_NAME = "ftp_bridge_tmp"
        private const val SERVER_PORT_EXTRA = "ftp_port"
        private const val SERVER_USE_TMP_DIR_EXTRA = "ftp_use_tmp_dir"
        private const val SERVER_ROOT_DIR_EXTRA = "ftp_root_dir"
        private const val SERVER_ALLOW_ANONYMOUS_EXTRA = "ftp_allow_anonymous"
        private const val SERVER_USERNAME_EXTRA = "ftp_username"
        private const val SERVER_PASSWORD_EXTRA = "ftp_password"

        private const val SERVER_DEFAULT_PORT = 2121
        private const val SERVER_DEFAULT_USE_TMP_DIR = true
        private const val SERVER_DEFAULT_ALLOW_ANONYMOUS = true

        private var serviceState = mutableStateOf(ServiceState.NOT_RUNNING)

        fun getState(): ServiceState = serviceState.value
        fun getTempDir(context: Context): File = context.cacheDir.resolve(TMP_SUB_FOLDER_NAME)
        fun getDefaultIntent(context: Context) = Intent(context, BridgeFtpServer::class.java).apply {
            putExtra(SERVER_PORT_EXTRA, SERVER_DEFAULT_PORT)
            putExtra(SERVER_USE_TMP_DIR_EXTRA, SERVER_DEFAULT_USE_TMP_DIR)
            putExtra(SERVER_ALLOW_ANONYMOUS_EXTRA, SERVER_DEFAULT_ALLOW_ANONYMOUS)
        }
        suspend fun getIntentFromPrefs(context: Context, prefs: IPreferences) = Intent(context, BridgeFtpServer::class.java).apply {
            putExtra(SERVER_PORT_EXTRA, prefs.port.first())
            putExtra(SERVER_USE_TMP_DIR_EXTRA, prefs.useTmpDir.first())
            putExtra(SERVER_ROOT_DIR_EXTRA, prefs.rootDirectory.first().toString())
            putExtra(SERVER_ALLOW_ANONYMOUS_EXTRA, prefs.ftpAllowAnonymous.first())
            putExtra(SERVER_USERNAME_EXTRA, prefs.ftpUsername.first())
            putExtra(SERVER_PASSWORD_EXTRA, prefs.ftpPassword.first())
        }
    }

    private val ftpNotification = Notifications.Constant.FTP_FG_SERVICE
    private lateinit var ftpServer: FTPServer

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val useTmpDir =
            intent?.getBooleanExtra(SERVER_USE_TMP_DIR_EXTRA, SERVER_DEFAULT_USE_TMP_DIR)
                ?: SERVER_DEFAULT_USE_TMP_DIR
        val rootDirPath = intent?.getStringExtra(SERVER_ROOT_DIR_EXTRA)
        val rootDir = if (useTmpDir) {
            getTempDir(applicationContext)
        } else {
            if (rootDirPath != null) File(rootDirPath) else getTempDir(applicationContext)
        }

        val ftpPort = intent?.getIntExtra(SERVER_PORT_EXTRA, SERVER_DEFAULT_PORT)
            ?: SERVER_DEFAULT_PORT

        check(rootDir.exists())

        val allowAnonymous =
            intent?.getBooleanExtra(SERVER_ALLOW_ANONYMOUS_EXTRA, SERVER_DEFAULT_ALLOW_ANONYMOUS)
                ?: SERVER_DEFAULT_ALLOW_ANONYMOUS

        val username = intent?.getStringExtra(SERVER_USERNAME_EXTRA)
        val password = intent?.getStringExtra(SERVER_PASSWORD_EXTRA)

        val fs = NativeFileSystem(rootDir)
        val auth = FtpUserAuthenticator(fs)
        if (allowAnonymous) auth.allowAnonymous() else auth.requireAuthentication(username ?: "", password ?: "")
        ftpServer = FTPServer(auth)

        startForeground(ftpNotification.id, createNotification(ftpPort))
        ftpServer.listen(NetworkUtils.getLocalIPv4Address(), ftpPort)
        serviceState.value = ServiceState.RUNNING

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ftpServer.close()
        serviceState.value = ServiceState.STOPPED
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channelExisting = notificationManager.getNotificationChannel(ftpNotification.channelId)

        if (channelExisting == null) {
            val notificationChannel = NotificationChannel(
                ftpNotification.channelId,
                "FTP Server Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "FTP Server"
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(ftpPort: Int): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ftpNotification.channelId)
            .setContentTitle("FTP Server")
            .setContentText("Running on ${NetworkUtils.getLocalIPv4Address()?.hostAddress}:$ftpPort")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .build()
    }
}