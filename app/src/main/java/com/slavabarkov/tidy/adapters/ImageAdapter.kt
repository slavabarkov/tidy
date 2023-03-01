/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.slavabarkov.tidy.fragments.ImageFragment
import com.slavabarkov.tidy.MainActivity
import com.slavabarkov.tidy.R


class ImageAdapter(private val context: Context, private val dataset: List<Long>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ImageViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = dataset[position]
        val imageUri = Uri.withAppendedPath(uri, item.toString())

        Glide.with(context).load(imageUri).thumbnail().into(holder.imageView)

        holder.imageView.setOnClickListener {
            val arguments = Bundle()
            arguments.putLong("image_id", item)
            arguments.putString("image_uri", imageUri.toString())

            val transaction: FragmentTransaction =
                (context as MainActivity).supportFragmentManager.beginTransaction()

            val fragment = ImageFragment()
            fragment.arguments = arguments
            transaction.addToBackStack("search_fragment")
            transaction.replace(R.id.fragmentContainerView, fragment)
            transaction.addToBackStack("image_fragment")
            transaction.commit()
        }
    }
}