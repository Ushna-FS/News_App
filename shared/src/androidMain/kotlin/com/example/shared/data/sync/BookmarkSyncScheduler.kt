package com.example.shared.data.sync

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.shared.data.worker.BookmarkSyncWorker

actual class BookmarkSyncScheduler(
    private val workManager: WorkManager
) {

    actual fun scheduleSync(userId: String) {
        Log.d("BookmarkWorker", "Scheduling worker for user: $userId")


        val data = workDataOf("USER_ID" to userId)

        val request = OneTimeWorkRequestBuilder<BookmarkSyncWorker>()
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "bookmark_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}