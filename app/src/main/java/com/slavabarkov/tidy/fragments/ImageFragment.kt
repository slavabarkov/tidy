/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.viewmodels.ORTImageViewModel
import com.slavabarkov.tidy.viewmodels.SearchViewModel
import java.text.DateFormat


class ImageFragment : Fragment() {
    private var imageUri: Uri? = null
    private var imageId: Long? = null
    private val mORTImageViewModel: ORTImageViewModel by activityViewModels()
    private val mSearchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        val bundle = this.arguments
        bundle?.let {
            imageId = it.getLong("image_id")
            imageUri = it.getString("image_uri")?.toUri()
        }

        //Get image date from image URI
        val cursor: Cursor =
            requireContext().contentResolver.query(imageUri!!, null, null, null, null)!!
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED)
        val date: Long = cursor.getLong(idx) * 1000
        cursor.close()

        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        dateTextView.text = DateFormat.getDateInstance().format(date)

        val singleImageView: PhotoView = view.findViewById(R.id.singeImageView)
        Glide.with(view).load(imageUri).into(singleImageView)

        val buttonImage2Image: Button = view.findViewById(R.id.buttonImage2Image)
        buttonImage2Image.setOnClickListener {
            imageId?.let {
                val imageIndex = mORTImageViewModel.idxList.indexOf(it)
                val imageEmbedding = mORTImageViewModel.embeddingsList[imageIndex]
                mSearchViewModel.sortByCosineDistance(
                    imageEmbedding,
                    mORTImageViewModel.embeddingsList,
                    mORTImageViewModel.idxList
                )
            }
            mSearchViewModel.fromImg2ImgFlag = true
            parentFragmentManager.popBackStack()
        }

        val buttonShare: Button = view.findViewById(R.id.buttonShare)
        buttonShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/*"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        return view
    }
}