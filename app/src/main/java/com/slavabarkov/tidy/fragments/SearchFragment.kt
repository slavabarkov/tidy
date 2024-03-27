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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
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

        // If Edge-to-edge display is enabled,
        // tweak the top margin so that content will not overlap with status bar
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            // Handle window insets here
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Update the layout parameters of the view's parent
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = insets.top
            }

            // Use WindowInsets to check the software keyboard visibility.
            // val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            // val imeHeight = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        // New keyboard animation using WindowInsets
        ViewCompat.setWindowInsetsAnimationCallback(
            view,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                var startBottom = 0f
                override fun onPrepare(
                    animation: WindowInsetsAnimationCompat
                ) {
                    startBottom = view.bottom.toFloat()
                }

                var endBottom = 0f
                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    // Record the position of the view after the IME transition.
                    endBottom = view.bottom.toFloat()

                    return bounds
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    // Find an IME animation.
                    val imeAnimation = runningAnimations.find {
                        it.typeMask and WindowInsetsCompat.Type.ime() != 0
                    } ?: return insets

                    // Offset the view based on the interpolated fraction of the IME animation.
                    view.translationY =
                        (startBottom - endBottom) * (1 - imeAnimation.interpolatedFraction)

                    return insets
                }

            }
        )


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