package com.example.tfg_3tiles_yubol.domain

import com.example.tfg_3tiles_yubol.data.model.Tile

class CheckMatchUseCase {

    fun checkMatch(tray: List<Tile>): List<Tile> {
        return tray.groupBy { it.type }
            .values
            .filter { it.size >= 3 }
            .flatten()
    }
}