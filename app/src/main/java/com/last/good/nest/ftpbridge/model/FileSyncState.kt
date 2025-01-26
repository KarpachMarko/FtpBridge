package com.last.good.nest.ftpbridge.model

import java.util.Date

class FileSyncState(
    private var state: State,
    private var updateDate: Date,
) {
    enum class State {
        NEW,
        LOCKED,
        READY_TO_SYNC,
        SYNC_IN_PROGRESS,
        SYNCED,
        REMOVED,
        ERROR
    }

    fun getState() = state
    fun getUpdateDate() = updateDate

    companion object {
        fun new() = fromState(State.NEW)

        fun fromState(state: State): FileSyncState {
            return FileSyncState(state, Date())
        }
    }

    fun updateState(newState: State) {
        state = newState
        updateDate = Date()
    }

    override fun toString(): String {
        return "<$state at: $updateDate)>"
    }
}