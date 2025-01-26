package com.last.good.nest.ftpbridge.services

import android.util.Log
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.io.path.Path
import com.hierynomus.smbj.auth.AuthenticationContext as SmbAuthenticationContext

class FileTransferManager {

    companion object {
        private const val TAG = "SmbTransferService"

        private const val SMB_SERVER = "nas.last-good-nest.com"
        private const val SMB_SERVER_PORT = 445
        private const val SMB_SHARE_NAME = "nest"
        private const val SMB_USERNAME = "mkarpats"
        private const val SMB_PASSWORD = "retriever-overpower-condiment-myself-turf"
        private const val SMB_REMOTE_DIR = "personal/mkarpats/sync/tmp"
    }

    suspend fun uploadFile(localFile: File): Boolean = withContext(Dispatchers.IO) {
        val smbClient = SMBClient()
        var session: Session? = null
        var diskShare: DiskShare? = null

        try {
            // Connect to the SMB server
            val connection = smbClient.connect(SMB_SERVER, SMB_SERVER_PORT)
            session = connection.authenticate(
                SmbAuthenticationContext(SMB_USERNAME, SMB_PASSWORD.toCharArray(), "")
            )
            Log.d(TAG, "SMB session connected")

            // Access the shared folder
            diskShare = session.connectShare(SMB_SHARE_NAME) as DiskShare
            Log.d(TAG, "Connected to SMB share: $SMB_SHARE_NAME")

            // File information
            val fileName = localFile.name
            val remoteFilePath = Path(SMB_REMOTE_DIR, fileName)

            // Open the file for writing
            diskShare.openFile(
                remoteFilePath.toString(),
                setOf(AccessMask.GENERIC_WRITE),
                setOf(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                setOf(SMB2ShareAccess.FILE_SHARE_WRITE),
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                null
            ).use { smbFile ->
                smbFile.outputStream.use { outputStream ->
                    // Optimize buffer size for large file uploads
                    val bufferSize = 1024 * 1024 // 1 MB buffer
                    val buffer = ByteArray(bufferSize)

                    FileInputStream(localFile).use { inputStream ->
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        Log.d(TAG, "File transferred successfully to $remoteFilePath")
                    }
                    true
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during SMB file transfer", e)
            false
        } finally {
            try {
                diskShare?.close()
                session?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing SMB connection", e)
            }
            smbClient.close()
            Log.d(TAG, "SMB client stopped")
        }
    }
}
