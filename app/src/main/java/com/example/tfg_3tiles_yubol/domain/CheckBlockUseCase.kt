package com.example.tfg_3tiles_yubol.domain

import com.example.tfg_3tiles_yubol.data.model.Tile

class CheckBlockUseCase {

    private val tileSize = 64f


    // funcion para prueba esta bloqueado
    // sale V o F
    fun isBlocked(tile: Tile, allTiles: List<Tile>): Boolean {

        // buscar carta esta arriba
        // filtrar carta misma y z(altura) mas que él
        val tilesAbove = allTiles.filter { otro ->
            otro.id != tile.id && otro.z > tile.z
        }

        // si carta arriba hay una carta sale bloqueado(true)
        return tilesAbove.any { otro ->
            overlaps(tile, otro)
        }
    }



    // prueba 2 carta esta superpuestos una al otro

    // si el lado izquierdo de carta b está a la izquierda del lado derecho de carta a
    // ademas el lado derecho de carta b está a la derecha del lado izquierdo de carta a
    // return true
    private fun overlaps (a: Tile , b: Tile): Boolean{

        //b.x            -> Esquina superior izquierda
        //b.x + tileSize -> Esquina superior derecha
        val xOverlap = b.x < a.x + tileSize && b.x + tileSize > a.x

        val yOverlap = b.y < a.y + tileSize && b.y + tileSize > a.y
        return  xOverlap && yOverlap
    }



}