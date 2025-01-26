package com.last.good.nest.ftpbridge.services

import com.guichaguri.minimalftp.FTPConnection
import com.guichaguri.minimalftp.api.IFileSystem
import com.guichaguri.minimalftp.api.IUserAuthenticator
import com.guichaguri.minimalftp.api.IUserAuthenticator.AuthException
import java.net.InetAddress

class FtpUserAuthenticator(private val fs: IFileSystem<*>) : IUserAuthenticator {

    override fun needsUsername(con: FTPConnection?): Boolean {
        return true
    }

    override fun needsPassword(
        con: FTPConnection?,
        username: String?,
        host: InetAddress?
    ): Boolean {
        return true
    }

    override fun authenticate(
        con: FTPConnection?,
        host: InetAddress?,
        username: String?,
        password: String?
    ): IFileSystem<*> {
        if (username == "usr" && password == "pwd") {
            return fs
        }

        throw AuthException()
    }

}