package com.example.tfg_3tiles_yubol.domain

import com.example.tfg_3tiles_yubol.data.model.Tile

class CheckMatchUseCase {

    fun buscarTrios(tray: List<Tile>): List<Tile> {
        return tray.groupBy { it.tipo }
            .values
            .filter { it.size >= 3 }
            .map { it.take(3) }
            .flatten()
    }
}