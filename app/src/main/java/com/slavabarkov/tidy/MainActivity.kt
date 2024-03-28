/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy

import android.os.Bundle
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

val apiLevel = Build.VERSION.SDK_INT

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (apiLevel >= 30 ) {
            // Code to handle API level 30 (Android 11)
            // When Android >= 11, enable “Display content edge-to-edge”
            // See more in https://developer.android.com/develop/ui/views/layout/edge-to-edge
            enableEdgeToEdge()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}


