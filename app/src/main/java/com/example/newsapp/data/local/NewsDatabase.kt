package com.example.newsapp.data.local


import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BookmarkedArticle::class],
    version = 4,
    exportSchema = true
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val DATABASE_NAME = "news_database"
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
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

                db.execSQL(
                    """
            INSERT INTO bookmarked_articles_new
            (url,title,description,urlToImage,publishedAt,content,sourceName,sourceId,author,bookmarkedAt)
            SELECT
            url,title,description,urlToImage,publishedAt,content,sourceName,sourceId,author,bookmarkedAt
            FROM bookmarked_articles
        """
                )

                db.execSQL("DROP TABLE bookmarked_articles")

                db.execSQL(
                    """
            ALTER TABLE bookmarked_articles_new
            RENAME TO bookmarked_articles
        """
                )

                db.execSQL(
                    """
            CREATE UNIQUE INDEX index_bookmarked_articles_url
            ON bookmarked_articles(url)
        """
                )
            }
        }


        // 2 → 3 (added isSynced column)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                ALTER TABLE bookmarked_articles
                ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0
            """
                )
            }
        }

        // 3 → 4 (example schema update)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                CREATE INDEX IF NOT EXISTS index_bookmarked_articles_url
                ON bookmarked_articles(url)
            """
                )
            }
        }
    }
}