package com.example.tfg_3tiles_yubol.data.local

import com.example.tfg_3tiles_yubol.data.model.Level
import com.example.tfg_3tiles_yubol.data.model.Tile

object LevelData {

    fun getLevel1(): Level {
        val tileSize = 55f
        val spacing = 18f
        val layerOffset = 35f
        val startX = 45f
        val startY = 130f

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

        //
        val layers = listOf(
            // Cuerpo principal
            LayerConfig(z = 1, startX = 50f, startY = 150f, cols = 5, rows = 5), //  25
            LayerConfig(z = 2, startX = 80f, startY = 180f, cols = 4, rows = 4), //  16
            LayerConfig(z = 3, startX = 110f, startY = 210f, cols = 3, rows = 3), // 9
            LayerConfig(z = 4, startX = 140f, startY = 240f, cols = 2, rows = 2), // 4
            LayerConfig(z = 5, startX = 170f, startY = 270f, cols = 1, rows = 1), // 1

            // Cabeza
            LayerConfig(z = 1, startX = 50f, startY = 80f, cols = 5, rows = 2),  // 10
            LayerConfig(z = 2, startX = 110f, startY = 80f, cols = 3, rows = 2), // 6


            // pierna izquierda
            LayerConfig(z = 1, startX = 50f, startY = 450f, cols = 2, rows = 3), // 6
            LayerConfig(z = 2, startX = 50f, startY = 450f, cols = 2, rows = 2), // 4
            // Pierna derecha
            LayerConfig(z = 1, startX = 230f, startY = 450f, cols = 2, rows = 3), // 6
            LayerConfig(z = 2, startX = 230f, startY = 450f, cols = 2, rows = 2), // 4


            // Hombros prominentes a ambos lados
            LayerConfig(z = 2, startX = 20f, startY = 150f, cols = 1, rows = 3), // 3
            LayerConfig(z = 2, startX = 320f, startY = 150f, cols = 1, rows = 3), // 3
            // La carta en los piernas
            LayerConfig(z = 3, startX = 80f, startY = 480f, cols = 1, rows = 2), // 2
            LayerConfig(z = 3, startX = 260f, startY = 480f, cols = 1, rows = 2), // 2
            // oculta en lo profundo del centro.
            LayerConfig(z = 6, startX = 170f, startY = 150f, cols = 1, rows = 4), // 4
        )


        for (layer in layers) {
            // Llamaremos al bucle exterior "outer" para poder salir de él fácilmente más adelante.
            outer@ for (row in 0 until layer.rows) {
                for (col in 0 until layer.cols) {
                    if (typeIndex >= allTypes.size) break@outer  // Salir de dos bucles anidados

                    // significa desplazarse una posición a la derecha después de tomar el siguiente tipo.
                    val type = allTypes[typeIndex++]
                    // si falla coge cangrejo
                    val fallbackIcon = com.example.tfg_3tiles_yubol.R.drawable.cangrejo
                    // buscar icono de cartas
                    val iconResId = TileIconMap.icons[type] ?: fallbackIcon
                    // Cuanto más alta sea la capa, más se desplazan ligeramente las cartas.
                    val zOffset = (layer.z - 1) * 4f
                    tiles.add(
                        Tile(
                            id = id++,
                            type = type,
                            x = layer.startX + col * tileSize + zOffset,
                            y = layer.startY + row * tileSize + zOffset,
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