package com.example.tfg_3tiles_yubol.data.model

data class Tile(
    val id: Int,
    val tipo: Int,          // tipo de carta
    val x: Float,
    val y: Float,
    val z: Int,
    val iconoRecurso: Int,         // para imagen de cada carta
    val estaBloqueada: Boolean = false
)