package com.example.tfg_3tiles_yubol.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.example.tfg_3tiles_yubol.ui.theme.OceanoMatchTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OceanoMatchTheme {

                    GameScreen()

            }
        }
    }
}