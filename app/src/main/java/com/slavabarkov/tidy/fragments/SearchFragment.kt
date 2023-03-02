/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.slavabarkov.tidy.viewmodels.ORTImageViewModel
import com.slavabarkov.tidy.viewmodels.ORTTextViewModel
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.viewmodels.SearchViewModel
import com.slavabarkov.tidy.adapters.ImageAdapter


class SearchFragment : Fragment() {
    private var searchText: TextView? = null
    private var searchButton: Button? = null
    private var clearButton: Button? = null
    private val mORTImageViewModel: ORTImageViewModel by activityViewModels()
    private val mORTTextViewModel: ORTTextViewModel by activityViewModels()
    private val mSearchViewModel: SearchViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        searchText = view?.findViewById(R.id.searchText)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view)

        if (mSearchViewModel.fromImg2ImgFlag) {
            searchText?.text = null
            recyclerView?.scrollToPosition(0)
            mSearchViewModel.fromImg2ImgFlag = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        if (mSearchViewModel.searchResults == null) {
            mSearchViewModel.searchResults = mORTImageViewModel.idxList.reversed()
        }
        recyclerView.adapter = ImageAdapter(requireContext(), mSearchViewModel.searchResults!!)
        recyclerView.scrollToPosition(0)

        mORTTextViewModel.init()

        searchText = view?.findViewById(R.id.searchText)
        searchButton = view?.findViewById(R.id.searchButton)

        searchButton?.setOnClickListener {
            val textEmbedding: FloatArray =
                mORTTextViewModel.getTextEmbedding(searchText?.text.toString())
            mSearchViewModel.sortByCosineDistance(textEmbedding, mORTImageViewModel.embeddingsList, mORTImageViewModel.idxList)
            recyclerView.adapter = ImageAdapter(requireContext(), mSearchViewModel.searchResults!!)
        }

        clearButton = view?.findViewById(R.id.clearButton)
        clearButton?.setOnClickListener{
            searchText?.text = null
            mSearchViewModel.searchResults = mORTImageViewModel.idxList.reversed()
            recyclerView.adapter = ImageAdapter(requireContext(), mSearchViewModel.searchResults!!)
        }
        return view
    }

}