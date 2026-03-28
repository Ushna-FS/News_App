package com.example.newsapp.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.newsapp.data.local.BookmarkDao
import com.example.newsapp.data.local.BookmarkedArticle
import com.example.newsapp.data.local.toBookmarkedArticle
import com.example.newsapp.data.models.Article
import com.example.newsapp.data.workers.BookmarkSyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val workManager: WorkManager

) {
    private val _bookmarkUpdates = MutableSharedFlow<String>()
    val bookmarkUpdates: SharedFlow<String> = _bookmarkUpdates.asSharedFlow()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun removeBookmark(url: String, userId: String) {
        bookmarkDao.deleteBookmarkByUrl(url)

        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(url.hashCode().toString())
            .delete()

        _bookmarkUpdates.emit(url)
    }

    suspend fun addBookmark(article: Article, userId: String) {
        val bookmarked = article.toBookmarkedArticle().copy(isSynced = false)
        bookmarkDao.insertBookmark(bookmarked)
        _bookmarkUpdates.emit(article.url)
        enqueueBookmarkSyncWorker(userId)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }

    suspend fun fetchBookmarksFromFirebase(userId: String) {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .get()
            .await()

        val bookmarks = snapshot.toObjects(BookmarkedArticle::class.java)

        bookmarkDao.clearBookmarks()

        bookmarkDao.insertBookmarks(
            bookmarks.map { it.copy(isSynced = true) }
        )
    }

    private fun enqueueBookmarkSyncWorker(userId: String) {


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

    private var listener: ListenerRegistration? = null

    fun startRealtimeSync(userId: String) {

        listener?.remove()

        listener = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val bookmarks = snapshot.toObjects(BookmarkedArticle::class.java)

                CoroutineScope(Dispatchers.IO).launch {

                    val local = bookmarkDao.getAllBookmarksOnce()

                    val remoteUrls = bookmarks.map { it.url }.toSet()
                    val localUrls = local.map { it.url }.toSet()

                    if (remoteUrls != localUrls) {

                        if (bookmarks.isNotEmpty()) {
                            bookmarkDao.clearBookmarks()
                            bookmarkDao.insertBookmarks(
                                bookmarks.map { it.copy(isSynced = true) }
                            )
                        }
                    }
                }
            }
    }
}