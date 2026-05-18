package com.example.tfg_3tiles_yubol.viewModel

import com.example.tfg_3tiles_yubol.data.model.Tile

enum class Difficulty(val label: String, val timeSeconds: Int, val maxUndos: Int, val maxShuffles: Int) {
    FACIL("Fácil", 600, maxUndos = 5, maxShuffles = 5),
    NORMAL("Normal", 300, maxUndos = 3, maxShuffles = 1),
    DIFICIL("Difícil", 180, maxUndos = 0, maxShuffles = 0)
}

data class GameState(
    val tiles: List<Tile> = emptyList(),
    val trayTiles: List<Tile> = emptyList(),
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isWin: Boolean = false,
    val currentLevel: Int = 1,
    val tileSize: Float = 55f,
    val eliminatingTiles: List<Tile> = emptyList(),
    val isAnimating: Boolean = false,  // Bloquea interacción durante animaciones
    val showLevelUp: Boolean = false,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val remainingTimeSeconds: Int = Difficulty.NORMAL.timeSeconds,
    val isTimeUp: Boolean = false,
    val remainingShuffles: Int = 1,
    val remainingUndos: Int = 3
)