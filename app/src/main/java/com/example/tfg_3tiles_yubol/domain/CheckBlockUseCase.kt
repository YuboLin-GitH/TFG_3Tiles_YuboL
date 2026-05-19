package com.example.tfg_3tiles_yubol.domain

import com.example.tfg_3tiles_yubol.data.model.Tile

class CheckBlockUseCase(private val tamanoFicha: Float) {

    // Verifica si una carta está bloqueada por otras cartas en niveles superiores (Z-index mayor).
    fun estaBloqueado(tile: Tile, todasLasFichas: List<Tile>): Boolean {
        val fichasEncima = todasLasFichas.filter { otro ->
            otro.id != tile.id && otro.z > tile.z
        }
        // si carta arriba hay una carta sale bloqueado(true)
        return fichasEncima.any { otro -> superponen(tile, otro) }
    }


    // prueba 2 carta esta superpuestos una al otro
    // si el lado izquierdo de carta b está a la izquierda del lado derecho de carta a
    // ademas el lado derecho de carta b está a la derecha del lado izquierdo de carta a
    private fun superponen(a: Tile, b: Tile): Boolean {
        //b.x            -> Esquina superior izquierda
        //b.x + tamanoFicha -> Esquina superior derecha
        val xOverlap = b.x < a.x + tamanoFicha && b.x + tamanoFicha > a.x
        val yOverlap = b.y < a.y + tamanoFicha && b.y + tamanoFicha > a.y
        return xOverlap && yOverlap
    }
}