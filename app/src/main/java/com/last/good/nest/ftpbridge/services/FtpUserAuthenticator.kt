package com.last.good.nest.ftpbridge.services

import com.guichaguri.minimalftp.FTPConnection
import com.guichaguri.minimalftp.api.IFileSystem
import com.guichaguri.minimalftp.api.IUserAuthenticator
import com.guichaguri.minimalftp.api.IUserAuthenticator.AuthException
import java.net.InetAddress

class FtpUserAuthenticator(private val fs: IFileSystem<*>) : IUserAuthenticator {

    private var requireAuthentication = false
    private var username: String? = null
    private var password: String? = null

    fun allowAnonymous() {
        requireAuthentication = false
    }

    fun requireAuthentication(username: String, password: String) {
        requireAuthentication = true
        this.username = username
        this.password = password
    }

    override fun needsUsername(con: FTPConnection?): Boolean {
        return requireAuthentication
    }

    override fun needsPassword(
        con: FTPConnection?,
        username: String?,
        host: InetAddress?
    ): Boolean {
        return requireAuthentication
    }

    override fun authenticate(
        con: FTPConnection?,
        host: InetAddress?,
        username: String?,
        password: String?
    ): IFileSystem<*> {
        if (!requireAuthentication) {
            return fs
        }

        if (username == this.username && password == this.password) {
            return fs
        }

        throw AuthException()
    }

}