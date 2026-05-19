package com.example.tfg_3tiles_yubol.viewModel

import com.example.tfg_3tiles_yubol.data.model.Tile

enum class Difficulty(val etiqueta: String, val tiempoSegundos: Int, val maxDeshacer: Int, val maxMezclas: Int) {
    FACIL("Fácil", 600, maxDeshacer = 5, maxMezclas = 5),
    NORMAL("Normal", 300, maxDeshacer = 3, maxMezclas = 1),
    DIFICIL("Difícil", 180, maxDeshacer = 0, maxMezclas = 0)
}

data class GameState(
    val fichas: List<Tile> = emptyList(),
    val fichasBandeja: List<Tile> = emptyList(),
    val puntuacion: Int = 0,
    val juegoTerminado: Boolean = false,
    val victoria: Boolean = false,
    val nivelActual: Int = 1,
    val tamanoFicha: Float = 55f,
    val fichasEliminando: List<Tile> = emptyList(),
    val animando: Boolean = false,  // Bloquea interacción durante animaciones
    val mostrarSubidaNivel: Boolean = false,
    val dificultad: Difficulty = Difficulty.NORMAL,
    val tiempoRestante: Int = Difficulty.NORMAL.tiempoSegundos,
    val tiempoAgotado: Boolean = false,
    val mezclasRestantes: Int = 1,
    val deshacerRestantes: Int = 3
)