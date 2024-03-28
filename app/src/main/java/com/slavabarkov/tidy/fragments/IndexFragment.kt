/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.slavabarkov.tidy.viewmodels.ORTImageViewModel
import com.slavabarkov.tidy.R

class IndexFragment : Fragment() {
    private var progressBarView: ProgressBar? = null
    private var progressBarTextView: TextView? = null
    private val mORTImageViewModel: ORTImageViewModel by activityViewModels()

    private val permissionsRequest: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mORTImageViewModel.generateIndex()
        } else {
            Toast.makeText(context, "The app requires storage permissions!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_index, container, false)
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Request required permissions depending on the Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsRequest.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        else {
            permissionsRequest.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        progressBarView = view.findViewById(R.id.progressBar)
        progressBarTextView = view.findViewById(R.id.progressBarText)

        mORTImageViewModel.progress.observe(viewLifecycleOwner) { progress ->
            var progressPercent: Int = (progress * 100).toInt()
            progressBarView?.progress = progressPercent

            //Reference progress_bar_text in strings.xml for better localisation.
            val myString = getString(R.string.progress_bar_text)
            "${myString}: ${progressPercent}%".also { progressBarTextView?.text = it }

            if (progress == 1.0) {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                findNavController().navigate(R.id.action_indexFragment_to_searchFragment)
            }
        }
        return view
    }
}
