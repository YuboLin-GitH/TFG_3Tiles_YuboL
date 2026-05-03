package com.example.tfg_3tiles_yubol.navegation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg_3tiles_yubol.ui.GameScreen
import com.example.tfg_3tiles_yubol.ui.StartScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MenuInicio, // inicio
        modifier = Modifier.fillMaxSize()
    ) {

        composable<MenuInicio> {
            val context = LocalContext.current
            StartScreen(
                onPlayClick = {

                    navController.navigate(NivelJuego)
                },
                onExitClick = {

                    (context as? android.app.Activity)?.finish()
                }
            )
        }


        composable<NivelJuego> {
            GameScreen()
        }
    }
}