package com.last.good.nest.ftpbridge.repository

import com.last.good.nest.ftpbridge.model.FileSyncState
import com.last.good.nest.ftpbridge.model.FileSyncState.State.ERROR
import com.last.good.nest.ftpbridge.model.FileSyncState.State.LOCKED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.READY_TO_SYNC
import com.last.good.nest.ftpbridge.model.FileSyncState.State.REMOVED
import com.last.good.nest.ftpbridge.model.FileSyncState.State.SYNCED
import com.last.good.nest.ftpbridge.utils.FileUtils.isLocked
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import java.time.Duration as JDuration

class FileSyncStateRepository {

    companion object {
        private val finalStates = listOf(REMOVED, SYNCED, ERROR)
    }

    private val fileStates = ConcurrentHashMap<String, FileSyncState>()

    fun newFile(filePath: String) {
        fileStates[filePath] = FileSyncState.new()
    }

    fun setFileState(filePath: String, state: FileSyncState.State) {
        fileStates[filePath]?.updateState(state)
    }

    fun getFileState(filePath: String): FileSyncState? {
        return fileStates[filePath]
    }

    fun fileExists(filePath: String): Boolean {
        return fileStates.contains(filePath)
    }

    fun updateStates(readyToSync: Duration) {
        fileStates.entries
            .filter { it.value.getState() !in finalStates }
            .forEach {
                val file = File(it.key)
                val state = it.value

                if (!file.exists()) {
                    state.updateState(REMOVED)
                    return@forEach
                }

                if (file.isLocked()) {
                    state.updateState(LOCKED)
                    return@forEach
                }

                val stableFor =
                    JDuration.between(state.getUpdateDate().toInstant(), Instant.now())
                if (readyToSync.inWholeMilliseconds <= stableFor.toMillis()) {
                    state.updateState(READY_TO_SYNC)
                }
            }
    }

    fun getFilesWithState(state: FileSyncState.State): Map<String, FileSyncState> {
        return fileStates.filter { it.value.getState() == state }
    }

    fun getAllFiles(): Map<String, FileSyncState> {
        return fileStates.toMap()
    }

    fun removeFile(filePath: String) {
        fileStates.remove(filePath)
    }

    fun clearRemoved() {
        fileStates.values.removeIf { it.getState() == FileSyncState.State.REMOVED }
    }

    fun clearAll() {
        fileStates.clear()
    }
}