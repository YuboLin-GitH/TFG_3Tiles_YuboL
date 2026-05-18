package com.example.tfg_3tiles_yubol.navegation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg_3tiles_yubol.ui.GameScreen
import com.example.tfg_3tiles_yubol.ui.LoginScreen
import com.example.tfg_3tiles_yubol.ui.RankingScreen
import com.example.tfg_3tiles_yubol.ui.StartScreen
import com.example.tfg_3tiles_yubol.viewModel.AuthStatus
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel

@Composable
fun NavGraph(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val authStatus by viewModel.loginStatus.collectAsState()

    LaunchedEffect(authStatus) {
        if (authStatus == AuthStatus.Success) {
            // Eliminar Login de la pila: el usuario no puede volver atrás tras autenticarse
            navController.navigate(MenuInicio) {
                popUpTo(Login) { inclusive = true }
            }
            viewModel.resetAuthStatus()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Login> {
            LoginScreen(viewModel = viewModel)
        }

        composable<MenuInicio> {
            val context = LocalContext.current
            StartScreen(
                onPlayClick = {
                    viewModel.resetGame()
                    navController.navigate(NivelJuego)
                },
                onExitClick = {
                    (context as? android.app.Activity)?.finish()
                },
                onRankingClick = {
                    navController.navigate(Ranking)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Login) {
                        popUpTo(MenuInicio) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable<NivelJuego> {
            GameScreen(
                viewModel = viewModel,
                onViewRanking = { navController.navigate(Ranking) },
                onBackToMenu = {
                    navController.navigate(MenuInicio) {
                        popUpTo(NivelJuego) { inclusive = true }
                    }
                }
            )
        }

        composable<Ranking> {
            RankingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}