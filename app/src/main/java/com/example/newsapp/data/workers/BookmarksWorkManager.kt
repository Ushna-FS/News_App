package com.example.newsapp.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.newsapp.data.local.BookmarkDao
import kotlinx.coroutines.tasks.await

@HiltWorker
class BookmarkSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bookmarkDao: BookmarkDao
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        Log.d("BookmarkWorker","Worker started")


        val userId = inputData.getString("USER_ID") ?: return Result.retry()

        val unsynced = bookmarkDao.getUnsyncedBookmarks()

        Log.d("BookmarkWorker","Unsynced bookmarks: ${unsynced.size}")

        unsynced.forEach { bookmark ->
            val docId = bookmark.url.hashCode().toString()
            firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(docId)
//                .document(bookmark.url)
                .set(bookmark)
                .await()

            bookmarkDao.markAsSynced(bookmark.url)
            Log.d("BookmarkWorker", "Uploading bookmark: ${bookmark.url}")
        }

        return Result.success()
    }
}