package com.example.tfg_3tiles_yubol.data.local

import com.example.tfg_3tiles_yubol.data.model.Level
import com.example.tfg_3tiles_yubol.data.model.Tile

object LevelData {

    fun getLevel1(): Level {
        val tileSize = 55f
        val spacing = 18f
        val layerOffset = 27.5f  // medio tile, consistente con nivel 2
        val startX = 62f
        val startY = 220f

        val allTypes = mutableListOf<Int>().apply {
            for (type in 1..3) {
                repeat(9) { add(type) }
            }
        }.shuffled()

        var id = 1
        var typeIndex = 0
        val tiles = mutableListOf<Tile>()

        for (z in 1..3) {
            val offset = (z - 1) * layerOffset
            for (row in 0..2) {
                for (col in 0..2) {
                    val type = allTypes[typeIndex++]
                    tiles.add(
                        Tile(
                            id = id++,
                            type = type,
                            x = startX + col * (tileSize + spacing) + offset,
                            y = startY + row * (tileSize + spacing) + offset,
                            z = z,
                            iconRes = TileIconMap.icons[type]!!
                        )
                    )
                }
            }
        }

        return Level(id = 1, tiles = tiles)
    }

    fun getLevel2(): Level {

        val tileSize = 55f

        // Un total de 105 cartas (garantizando exactamente 35 conjuntos de tres cartas iguales)
        val allTypes = mutableListOf<Int>().apply {
            listOf(1, 2, 3, 4, 5, 6, 7).forEach { type -> repeat(9) { add(type) } }
            listOf(8, 9, 10, 11, 12, 13, 14).forEach { type -> repeat(6) { add(type) } }
        }.shuffled()

        var id = 200
        var typeIndex = 0
        val tiles = mutableListOf<Tile>()

        // Diseño simétrico de 105 cartas centrado en X = 197.5f
        val layers = listOf(
            // --- CUERPO (pirámide central) Z: 3-7 (55 cartas) ---
            LayerConfig(z = 3, startX = 60f,    startY = 200f,   cols = 5, rows = 5),
            LayerConfig(z = 4, startX = 87.5f,  startY = 227.5f, cols = 4, rows = 4),
            LayerConfig(z = 5, startX = 115f,   startY = 255f,   cols = 3, rows = 3),
            LayerConfig(z = 6, startX = 142.5f, startY = 282.5f, cols = 2, rows = 2),
            LayerConfig(z = 7, startX = 170f,   startY = 310f,   cols = 1, rows = 1),

            // --- CABEZA Z: 8-10 (14 cartas) ---
            LayerConfig(z = 8, startX = 115f,   startY = 90f,    cols = 3, rows = 3),
            LayerConfig(z = 9, startX = 142.5f, startY = 117.5f, cols = 2, rows = 2),
            LayerConfig(z = 10, startX = 170f,  startY = 145f,   cols = 1, rows = 1),

            // --- PIERNA IZQUIERDA Z: 1-2 (10 cartas) ---
            LayerConfig(z = 1, startX = 60f,  startY = 470f,   cols = 2, rows = 3),
            LayerConfig(z = 2, startX = 60f,  startY = 497.5f, cols = 2, rows = 2),
            // --- PIERNA DERECHA Z: 1-2 (10 cartas) ---
            LayerConfig(z = 1, startX = 225f, startY = 470f,   cols = 2, rows = 3),
            LayerConfig(z = 2, startX = 225f, startY = 497.5f, cols = 2, rows = 2),

            // --- COLA (relleno entre piernas) Z: 1-2 (6 cartas) ---
            LayerConfig(z = 1, startX = 170f, startY = 470f, cols = 1, rows = 3),
            LayerConfig(z = 2, startX = 170f, startY = 470f, cols = 1, rows = 3),

            // --- HOMBROS Z: 8-9 (10 cartas) ---
            LayerConfig(z = 8,  startX = 32.5f,  startY = 255f,   cols = 1, rows = 3),
            LayerConfig(z = 8,  startX = 307.5f, startY = 255f,   cols = 1, rows = 3),
            LayerConfig(z = 9,  startX = 32.5f,  startY = 282.5f, cols = 1, rows = 2),
            LayerConfig(z = 9,  startX = 307.5f, startY = 282.5f, cols = 1, rows = 2)
        )

        for (layer in layers) {
            outer@ for (row in 0 until layer.rows) {
                for (col in 0 until layer.cols) {
                    if (typeIndex >= allTypes.size) break@outer

                    val type = allTypes[typeIndex++]
                    val fallbackIcon = com.example.tfg_3tiles_yubol.R.drawable.cangrejo
                    val iconResId = TileIconMap.icons[type] ?: fallbackIcon
                    tiles.add(
                        Tile(
                            id = id++,
                            type = type,
                            x = layer.startX + col * tileSize,
                            y = layer.startY + row * tileSize,
                            z = layer.z,
                            iconRes = iconResId
                        )
                    )
                }
            }
        }
        return Level(id = 2, tiles = tiles)
    }
}

data class LayerConfig(
    val z: Int,
    val startX: Float,
    val startY: Float,
    val cols: Int,
    val rows: Int
)