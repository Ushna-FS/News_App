package com.example.shared.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL(
            """
            CREATE TABLE bookmarked_articles_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                url TEXT NOT NULL,
                title TEXT,
                description TEXT,
                urlToImage TEXT,
                publishedAt TEXT,
                content TEXT,
                sourceName TEXT,
                sourceId TEXT,
                author TEXT,
                bookmarkedAt INTEGER NOT NULL
            )
        """
        )

        connection.execSQL(
            """
            INSERT INTO bookmarked_articles_new
            (url,title,description,urlToImage,publishedAt,content,sourceName,sourceId,author,bookmarkedAt)
            SELECT
            url,title,description,urlToImage,publishedAt,content,sourceName,sourceId,author,bookmarkedAt
            FROM bookmarked_articles
        """
        )

        connection.execSQL("DROP TABLE bookmarked_articles")

        connection.execSQL(
            """
            ALTER TABLE bookmarked_articles_new
            RENAME TO bookmarked_articles
        """
        )

        connection.execSQL(
            """
            CREATE UNIQUE INDEX index_bookmarked_articles_url
            ON bookmarked_articles(url)
        """
        )
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL(
            """
            ALTER TABLE bookmarked_articles
            ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0
            """
        )
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_bookmarked_articles_url
            ON bookmarked_articles(url)
            """
        )
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {

    override fun migrate(connection: SQLiteConnection) {

        connection.execSQL(
            """
            CREATE TABLE bookmarked_articles_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                url TEXT NOT NULL,
                userId TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                urlToImage TEXT,
                publishedAt TEXT,
                content TEXT,
                sourceName TEXT NOT NULL,
                sourceId TEXT,
                author TEXT,
                bookmarkedAt INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
            """
        )

        connection.execSQL(
            """
            INSERT INTO bookmarked_articles_new (
                id, url, userId, title, description, urlToImage, 
                publishedAt, content, sourceName, sourceId, author, 
                bookmarkedAt, isSynced
            )
            SELECT 
                id, url, '' AS userId, title, description, urlToImage,
                publishedAt, content, sourceName, sourceId, author,
                bookmarkedAt, isSynced
            FROM bookmarked_articles
            """
        )

        connection.execSQL("DROP TABLE bookmarked_articles")

        connection.execSQL(
            "ALTER TABLE bookmarked_articles_new RENAME TO bookmarked_articles"
        )

        connection.execSQL(
            """
            CREATE UNIQUE INDEX index_bookmarked_articles_url_userId
            ON bookmarked_articles(url, userId)
            """
        )
    }
}