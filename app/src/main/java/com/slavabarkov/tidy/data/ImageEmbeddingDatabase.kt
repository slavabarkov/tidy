/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageEmbedding::class], version = 1, exportSchema = false)
abstract class ImageEmbeddingDatabase : RoomDatabase() {
    abstract fun imageEmbeddingDao(): ImageEmbeddingDao

    companion object {
        @Volatile
        private var INSTANCE: ImageEmbeddingDatabase? = null

        fun getDatabase(context: Context): ImageEmbeddingDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageEmbeddingDatabase::class.java,
                    "image_embedding_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}