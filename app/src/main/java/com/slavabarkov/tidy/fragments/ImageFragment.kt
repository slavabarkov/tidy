/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.slavabarkov.tidy.viewmodels.ORTImageViewModel
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.viewmodels.SearchViewModel


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
        val singleImageView: ImageView = view.findViewById(R.id.singeImageView)
        Glide.with(view).load(imageUri).into(singleImageView)

        val buttonImage2Image: Button = view.findViewById(R.id.buttonImage2Image)
        buttonImage2Image.setOnClickListener {
            imageId?.let{
                val imageIndex = mORTImageViewModel.idxList.indexOf(it)
                val imageEmbedding = mORTImageViewModel.embeddingsList[imageIndex]
                mSearchViewModel.sortByCosineDistance(imageEmbedding, mORTImageViewModel.embeddingsList, mORTImageViewModel.idxList)
            }
            mSearchViewModel.fromImg2ImgFlag = true
            parentFragmentManager.popBackStack()
        }
        return view
    }
}