/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageEmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageEmbedding(imageEmbedding: ImageEmbedding)

    @Query("SELECT * FROM image_embeddings WHERE id = :id LIMIT 1")
    suspend fun getRecord(id: Long): ImageEmbedding
}