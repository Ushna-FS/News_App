package com.example.shared.data.repository

import com.example.shared.data.local.BookmarkDao
import com.example.shared.data.local.BookmarkedArticle
import com.example.shared.data.local.toBookmarkedArticle
import com.example.shared.data.models.Article
import com.example.shared.data.sync.BookmarkSyncScheduler
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao,
    private val scheduler: BookmarkSyncScheduler
) : BookmarkRepository {

    private val _bookmarkUpdates = MutableSharedFlow<String>()
    override val bookmarkUpdates = _bookmarkUpdates.asSharedFlow()

    private val firestore = Firebase.firestore
    private var syncJob: Job? = null

    override suspend fun addBookmark(article: Article, userId: String) {

        val bookmarked = article.toBookmarkedArticle().copy(isSynced = false)

        bookmarkDao.insertBookmark(bookmarked)

        _bookmarkUpdates.emit(article.url)

        scheduler.scheduleSync(userId)
    }

    override suspend fun removeBookmark(url: String, userId: String) {

        bookmarkDao.deleteBookmarkByUrl(url)

        firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .document(url.hashCode().toString())
            .delete()

        _bookmarkUpdates.emit(url)
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return bookmarkDao.isArticleBookmarked(url) > 0
    }

    override fun getAllBookmarks(): Flow<List<BookmarkedArticle>> {
        return bookmarkDao.getAllBookmarks()
    }

    // 🔹 Fetch bookmarks from Firebase once
    override suspend fun fetchBookmarksFromFirebase(userId: String) {

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .get()

        val bookmarks = snapshot.documents.map {
            it.data<BookmarkedArticle>()
        }

        bookmarkDao.clearBookmarks()

        bookmarkDao.insertBookmarks(
            bookmarks.map { it.copy(isSynced = true) }
        )
    }

    // 🔹 Realtime Firebase sync
    override fun startRealtimeSync(userId: String) {
        syncJob?.cancel()

        val flow = firestore.collection("users")
            .document(userId)
            .collection("bookmarks")
            .snapshots()

        syncJob = CoroutineScope(Dispatchers.Default).launch {

            flow.collectLatest { snapshot ->

                val bookmarks = snapshot.documents.map {
                    it.data<BookmarkedArticle>()
                }

                val local = bookmarkDao.getAllBookmarksOnce()

                val remoteUrls = bookmarks.map { it.url }.toSet()
                val localUrls = local.map { it.url }.toSet()

                if (remoteUrls != localUrls && bookmarks.isNotEmpty()) {

                    bookmarkDao.clearBookmarks()

                    bookmarkDao.insertBookmarks(
                        bookmarks.map { it.copy(isSynced = true) }
                    )
                }
            }
        }
    }

     override fun stopRealtimeSync() {
        syncJob?.cancel()
        syncJob = null
    }
}