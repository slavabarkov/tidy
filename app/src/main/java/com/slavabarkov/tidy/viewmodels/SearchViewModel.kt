/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.slavabarkov.tidy.dot

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    var searchResults: List<Long>? = null
    var fromImg2ImgFlag: Boolean = false

    fun sortByCosineDistance(searchEmbedding: FloatArray,
                          imageEmbeddingsList: List<FloatArray>,
                          imageIdxList: List<Long>) {
        val distances = LinkedHashMap<Long, Float>()
        for (i in imageEmbeddingsList.indices) {
            val dist = searchEmbedding.dot(imageEmbeddingsList[i])
            distances[imageIdxList[i]] = dist
        }
        searchResults = distances.toList().sortedBy { (k, v) -> v }.map { (k, v) -> k }.reversed()
    }


}