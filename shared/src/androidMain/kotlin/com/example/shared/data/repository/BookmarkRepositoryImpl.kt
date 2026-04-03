package com.example.shared.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.shared.data.local.BookmarkDao
import com.example.shared.data.local.BookmarkedArticle
import com.example.shared.data.local.toBookmarkedArticle
import com.example.shared.data.models.Article
import com.example.shared.data.worker.BookmarkSyncWorker
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao,
    private val workManager: WorkManager

) : BookmarkRepository {
    private val _bookmarkUpdates = MutableSharedFlow<String>()
    override val bookmarkUpdates: SharedFlow<String> = _bookmarkUpdates.asSharedFlow()
    private val firestore = Firebase.firestore

    override suspend fun removeBookmark(url: String, userId: String) {
        bookmarkDao.deleteBookmarkByUrl(url)

        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(url.hashCode().toString())
            .delete()

        _bookmarkUpdates.emit(url)
    }

    override suspend fun addBookmark(article: Article, userId: String) {
        val bookmarked = article.toBookmarkedArticle().copy(isSynced = false)
        bookmarkDao.insertBookmark(bookmarked)
        _bookmarkUpdates.emit(article.url)
        enqueueBookmarkSyncWorker(userId)
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    override fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }

    override suspend fun fetchBookmarksFromFirebase(userId: String) {
        // Firestore instance
        val firestore = Firebase.firestore

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .get() // suspend function

        // Convert snapshot to list
        val bookmarks = snapshot.documents.map {
            it.data<BookmarkedArticle>()
        }
        // Clear local db and insert
        bookmarkDao.clearBookmarks()
        bookmarkDao.insertBookmarks(bookmarks.map { it.copy(isSynced = true) })
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


    override fun startRealtimeSync(userId: String) {
        val firestore = Firebase.firestore

        // snapshotFlow gives a Flow<QuerySnapshot>
        val flow = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .snapshots()

        // Launch coroutine to collect updates
        CoroutineScope(Dispatchers.IO).launch {
            flow.collectLatest { snapshot ->
                val bookmarks = snapshot.documents.map {
                    it.data<BookmarkedArticle>()
                }
                val local = bookmarkDao.getAllBookmarksOnce()

                val remoteUrls = bookmarks.map { it.url }.toSet()
                val localUrls = local.map { it.url }.toSet()

                if (remoteUrls != localUrls && bookmarks.isNotEmpty()) {
                    bookmarkDao.clearBookmarks()
                    bookmarkDao.insertBookmarks(bookmarks.map { it.copy(isSynced = true) })
                }
            }
        }
    }
}