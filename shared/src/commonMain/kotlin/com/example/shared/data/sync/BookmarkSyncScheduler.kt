package com.example.shared.data.sync

expect class BookmarkSyncScheduler {
    fun scheduleSync(userId: String)
}