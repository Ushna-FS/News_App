package com.example.shared.data.di


import com.example.shared.data.worker.BookmarkSyncWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {

    worker {
        BookmarkSyncWorker(
            context = get(),
            params = get(),
            bookmarkDao = get()
        )
    }

}