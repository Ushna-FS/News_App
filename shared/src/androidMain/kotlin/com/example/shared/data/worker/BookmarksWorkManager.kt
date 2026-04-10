package com.example.shared.data.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.shared.data.local.BookmarkDao
import dev.gitlive.firebase.*
import dev.gitlive.firebase.firestore.*

class BookmarkSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val bookmarkDao: BookmarkDao
) : CoroutineWorker(context, params) {

    private val firestore = Firebase.firestore

    override suspend fun doWork(): Result {
        Log.d("BookmarkWorker", "Worker started")


        val userId = inputData.getString("USER_ID") ?: return Result.retry()

        val unsynced = bookmarkDao.getUnsyncedBookmarks()

        Log.d("BookmarkWorker", "Unsynced bookmarks: ${unsynced.size}")

        unsynced.forEach { bookmark ->
            val docId = bookmark.url.hashCode().toString()
            firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(docId)
                .set(bookmark)
//                .await()

            bookmarkDao.markAsSynced(bookmark.url)
            Log.d("BookmarkWorker", "Uploading bookmark: ${bookmark.url}")
        }

        return Result.success()
    }
}