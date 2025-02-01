package com.last.good.nest.ftpbridge.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

object FileUtils {

    fun String.toFile(): File {
        return File(this)
    }

    fun String.toPath(): Path {
        return Paths.get(this)
    }

    fun File.isLocked(): Boolean {
        return try {
            val raf = RandomAccessFile(this, "rw")
            val channel = raf.channel
            val lock = channel.tryLock()
            lock?.release()
            channel.close()
            raf.close()
            false // File is not locked
        } catch (e: Exception) {
            true // File is locked
        }
    }

    fun File.sha256Checksum(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        this.inputStream().use { fis ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

}