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
import com.guichaguri.minimalftp.FTPServer
import com.guichaguri.minimalftp.impl.NativeFileSystem
import com.last.good.nest.ftpbridge.MainActivity
import com.last.good.nest.ftpbridge.model.ServiceState
import com.last.good.nest.ftpbridge.utils.NetworkUtils
import java.io.File


class BridgeFtpServer : Service() {

    companion object {
        const val TMP_SUB_FOLDER_NAME = "ftp_bridge_tmp"
        const val CHANNEL_ID = "BridgeFtpService"
        const val NOTIFICATION_ID = 2
        const val SERVER_PORT = 2121

        var serviceState = mutableStateOf(ServiceState.NOT_RUNNING);
        fun getState(): ServiceState {
            return serviceState.value;
        }
    }

    private val tempDir by lazy { File(applicationContext.cacheDir, TMP_SUB_FOLDER_NAME) }
    private val fs by lazy { NativeFileSystem(tempDir) }
    private val auth by lazy { FtpUserAuthenticator(fs) }
    private val ftpServer by lazy { FTPServer(auth) }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        tempDir.mkdirs()
        serviceState.value = ServiceState.RUNNING
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ftpServer.listen(NetworkUtils.getLocalIpAddress(), SERVER_PORT)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ftpServer.close()
        serviceState.value = ServiceState.STOPPED
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channelExisting = notificationManager.getNotificationChannel(CHANNEL_ID)

        if (channelExisting == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "FTP Server Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "FTP Server"
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
            .setContentTitle("FTP Server")
            .setContentText("Running on ${NetworkUtils.getLocalIpAddress()?.hostAddress}:$SERVER_PORT")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .build()
    }
}