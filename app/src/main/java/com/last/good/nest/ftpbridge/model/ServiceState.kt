package com.last.good.nest.ftpbridge.model

enum class ServiceState {
    NOT_RUNNING,
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING;

    fun isRunning(): Boolean {
        return this == RUNNING
    }
}