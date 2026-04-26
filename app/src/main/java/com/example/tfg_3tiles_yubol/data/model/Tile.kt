package com.example.tfg_3tiles_yubol.data.model

data class Tile(
    val id: Int,
    val type: Int,          // tipo de carta
    val x: Float,
    val y: Float,
    val z: Int,
    val iconRes: Int,         // para imagen de cada carta
    var isBlocked: Boolean = false
)