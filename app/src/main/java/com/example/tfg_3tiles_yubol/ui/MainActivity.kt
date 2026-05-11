package com.example.tfg_3tiles_yubol.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.tfg_3tiles_yubol.navegation.NavGraph
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initSound(this)
        setContent {
            MaterialTheme {

                NavGraph(viewModel = viewModel)

            }
        }

    }
    override fun onResume() {
        super.onResume()
        viewModel.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releaseMusic()
    }
}