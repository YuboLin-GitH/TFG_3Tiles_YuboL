package com.example.tfg_3tiles_yubol.domain

import com.example.tfg_3tiles_yubol.data.model.Tile

class CheckMatchUseCase {

    fun checkMatch(tray: MutableList<Tile>): List<Tile> {
        return tray.groupBy { it.id }
            .values
            .filter { it.size >= 3 }
            .flatten()
    }
}