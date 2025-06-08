package com.example.my_blog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Blog(
    val id: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val isDraft: Boolean,
    val mediaUri: String?,
    val mediaType: String?  // "image" or "video"
)

class BlogDbHelper(context: Context) : SQLiteOpenHelper(context, "blogs.db", null, 2) {

    companion object {
        const val TABLE_NAME = "blogs"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_AUTHOR = "authorId"
        const val COLUMN_IS_DRAFT = "isDraft"
        const val COLUMN_MEDIA_URI = "mediaUri"
        const val COLUMN_MEDIA_TYPE = "mediaType"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val create = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_AUTHOR INTEGER NOT NULL,
                $COLUMN_IS_DRAFT INTEGER NOT NULL,
                $COLUMN_MEDIA_URI TEXT,
                $COLUMN_MEDIA_TYPE TEXT
            )
        """.trimIndent()
        db.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_MEDIA_URI TEXT")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_MEDIA_TYPE TEXT")
        }
    }

    fun insertBlog(
        title: String,
        content: String,
        authorId: Int,
        isDraft: Boolean,
        mediaUri: String?,
        mediaType: String?
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
            put(COLUMN_AUTHOR, authorId)
            put(COLUMN_IS_DRAFT, if (isDraft) 1 else 0)
            put(COLUMN_MEDIA_URI, mediaUri)
            put(COLUMN_MEDIA_TYPE, mediaType)
        }
        return db.insert(TABLE_NAME, null, values)
    }
    fun getLatestBlogId(): Int? {
        val cursor = readableDatabase.rawQuery("SELECT $COLUMN_ID FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC LIMIT 1", null)
        var latestId: Int? = null
        if (cursor.moveToFirst()) {
            latestId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        return latestId
    }



    fun updateBlog(
        id: Int,
        title: String,
        content: String,
        isDraft: Boolean,
        mediaUri: String?,
        mediaType: String?
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
            put(COLUMN_IS_DRAFT, if (isDraft) 1 else 0)
            put(COLUMN_MEDIA_URI, mediaUri)
            put(COLUMN_MEDIA_TYPE, mediaType)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getBlogById(id: Int): Blog? {
        val cursor = readableDatabase.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        var blog: Blog? = null
        if (cursor.moveToFirst()) {
            blog = Blog(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DRAFT)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_URI)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE))
            )
        }
        cursor.close()
        return blog
    }
}
