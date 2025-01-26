package com.last.good.nest.ftpbridge.utils

object Notifications {

    enum class Constant(val channelId: String, val id: Int) {
        FTP_FG_SERVICE("BridgeFtpService", 100),
        SYNC_FG_SERVICE("SyncService", 110);
    }

}