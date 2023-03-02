/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.viewmodels

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.centerCrop
import com.slavabarkov.tidy.data.ImageEmbedding
import com.slavabarkov.tidy.data.ImageEmbeddingDatabase
import com.slavabarkov.tidy.data.ImageEmbeddingRepository
import com.slavabarkov.tidy.normalizeL2
import com.slavabarkov.tidy.preProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ORTImageViewModel(application: Application) : AndroidViewModel(application) {
    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var repository: ImageEmbeddingRepository
    var idxList: ArrayList<Long> = arrayListOf()
    var embeddingsList: ArrayList<FloatArray> = arrayListOf()
    var progress: MutableLiveData<Double> = MutableLiveData(0.0)

    init {
        val imageEmbeddingDao = ImageEmbeddingDatabase.getDatabase(application).imageEmbeddingDao()
        repository = ImageEmbeddingRepository(imageEmbeddingDao)
    }

    fun generateIndex() {
        val modelID = R.raw.visual_quant
        val resources = getApplication<Application>().resources
        val model = resources.openRawResource(modelID).readBytes()
        val session = ortEnv.createSession(model)

        viewModelScope.launch(Dispatchers.Main) {
            progress.value = 0.0
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
            val sortOrder = "${MediaStore.Images.Media._ID} ASC"
            val contentResolver: ContentResolver = getApplication<Application>().contentResolver
            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, sortOrder)
            val totalImages = cursor?.count ?: 0
            cursor?.use {
                val idColumn: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn: Int =
                    it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val bucketColumn: Int =
                    it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (it.moveToNext()) {
                    val id: Long = it.getLong(idColumn)
                    val date: Long = it.getLong(dateColumn)
                    val bucket: String = it.getString(bucketColumn)
                    if (bucket == "Screenshots") continue

                    Log.d("ORTImageViewModel", "ID: $id, Date: $date, Bucket: $bucket")
                    val record = repository.getRecord(id) as ImageEmbedding?
                    if (record != null) {
                        Log.d("ORTImageViewModel", "Record exists")
                        idxList.add(record.id)
                        embeddingsList.add(record.embedding)
                    } else {
                        Log.d("ORTImageViewModel", "Record does not exist")

                        val imageUri: Uri = Uri.withAppendedPath(uri, id.toString())
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        println("STREAM EMPTY: ${bytes == null}")

                        // Can fail to create the image decoder if its not implemented for the image type
                        val bitmap: Bitmap? =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
                        println("BITMAP EMPTY: ${bitmap == null}")
                        bitmap?.let {
                            val rawBitmap = centerCrop(bitmap, 224)
                            val inputShape = longArrayOf(1, 3, 224, 224)
                            val inputName = "pixel_values"
                            val imgData = preProcess(rawBitmap)
                            val inputTensor = OnnxTensor.createTensor(ortEnv, imgData, inputShape)

                            inputTensor.use {
                                val output =
                                    session?.run(Collections.singletonMap(inputName, inputTensor))
                                output.use {
                                    @Suppress("UNCHECKED_CAST") var rawOutput =
                                        ((output?.get(0)?.value) as Array<FloatArray>)[0]
                                    rawOutput = normalizeL2(rawOutput)

                                    repository.addImageEmbedding(
                                        ImageEmbedding(
                                            id, date, rawOutput
                                        )
                                    )
                                    idxList.add(id)
                                    embeddingsList.add(rawOutput)

                                }
                            }
                        }
                    }
                    // record created/loaded
                    // update progress
                    progress.value = it.position.toDouble() / totalImages.toDouble()

                }
            }
            cursor?.close()
            session.close()
            progress.setValue(1.0)
        }

    }
}



