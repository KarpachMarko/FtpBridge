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

class FileTransferManager(
    private val smbServerAddress: String,
    private val smbServerPort: Int,
    private val smbShareName: String,
    private val smbUsername: String,
    private val smbPassword: String,
    private val smbRemoteDir: String,
) {

    companion object {
        private const val TAG = "SmbTransferService"
    }

    suspend fun uploadFile(localFile: File): Boolean = withContext(Dispatchers.IO) {
        val smbClient = SMBClient()
        var session: Session? = null
        var diskShare: DiskShare? = null

        try {
            // Connect to the SMB server
            val connection = smbClient.connect(smbServerAddress, smbServerPort)
            session = connection.authenticate(
                SmbAuthenticationContext(smbUsername, smbPassword.toCharArray(), "")
            )
            Log.d(TAG, "SMB session connected")

            // Access the shared folder
            diskShare = session.connectShare(smbShareName) as DiskShare
            Log.d(TAG, "Connected to SMB share: $smbShareName")

            // File information
            val fileName = localFile.name
            val remoteFilePath = Path(smbRemoteDir, fileName)

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
