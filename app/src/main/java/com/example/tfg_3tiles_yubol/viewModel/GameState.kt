package com.example.tfg_3tiles_yubol.viewModel

import com.example.tfg_3tiles_yubol.data.model.Tile

data class GameState(
    val tiles: List<Tile> = emptyList(), //cuando carta quedan
    val trayTiles: List<Tile> = emptyList(), // cuando carta estan en tary
    val score: Int = 0,  // no se si voy usar?
    val isGameOver: Boolean = false, // juego esta finado?
    val isWin: Boolean = false,
    val currentLevel: Int = 1
)