package com.example.tfg_3tiles_yubol.data.local

import com.example.tfg_3tiles_yubol.R
import com.example.tfg_3tiles_yubol.data.model.Level
import com.example.tfg_3tiles_yubol.data.model.Tile

object LevelData {

    fun getLevel1(): Level {
        val tamanoFicha = 55f
        val espaciado = 18f
        val desplazamientoCapa = 27.5f  // medio tile, consistente con nivel 2
        val inicioX = 62f
        val inicioY = 220f

        val todosLosTipos = mutableListOf<Int>().apply {
            for (tipo in 1..3) {
                repeat(9) { add(tipo) }
            }
        }.shuffled()

        var id = 1
        var indiceTipo = 0
        val fichasLista = mutableListOf<Tile>()

        for (z in 1..3) {
            val desplazamiento = (z - 1) * desplazamientoCapa
            for (fila in 0..2) {
                for (columna in 0..2) {
                    val tipo = todosLosTipos[indiceTipo++]
                    fichasLista.add(
                        Tile(
                            id = id++,
                            tipo =tipo,
                            x = inicioX + columna * (tamanoFicha + espaciado) + desplazamiento,
                            y = inicioY + fila * (tamanoFicha + espaciado) + desplazamiento,
                            z = z,
                            iconoRecurso =TileIconMap.icons[tipo]!!
                        )
                    )
                }
            }
        }

        return Level(id = 1, fichas = fichasLista)
    }

    fun getLevel2(): Level {

        val tamanoFicha = 55f

        // Un total de 105 cartas (garantizando exactamente 35 conjuntos de tres cartas iguales)
        val todosLosTipos = mutableListOf<Int>().apply {
            listOf(1, 2, 3, 4, 5, 6, 7).forEach { tipo -> repeat(9) { add(tipo) } }
            listOf(8, 9, 10, 11, 12, 13, 14).forEach { tipo -> repeat(6) { add(tipo) } }
        }.shuffled()

        var id = 200
        var indiceTipo = 0
        val fichasLista = mutableListOf<Tile>()

        // Diseño simétrico de 105 cartas centrado en X = 197.5f
        val capas = listOf(
            // --- CUERPO (pirámide central) Z: 3-7 (55 cartas) ---
            LayerConfig(z = 3, inicioX = 60f,    inicioY = 200f,   columnas = 5, filas = 5),
            LayerConfig(z = 4, inicioX = 87.5f,  inicioY = 227.5f, columnas = 4, filas = 4),
            LayerConfig(z = 5, inicioX = 115f,   inicioY = 255f,   columnas = 3, filas = 3),
            LayerConfig(z = 6, inicioX = 142.5f, inicioY = 282.5f, columnas = 2, filas = 2),
            LayerConfig(z = 7, inicioX = 170f,   inicioY = 310f,   columnas = 1, filas = 1),

            // --- CABEZA Z: 8-10 (14 cartas) ---
            LayerConfig(z = 8, inicioX = 115f,   inicioY = 90f,    columnas = 3, filas = 3),
            LayerConfig(z = 9, inicioX = 142.5f, inicioY = 117.5f, columnas = 2, filas = 2),
            LayerConfig(z = 10, inicioX = 170f,  inicioY = 145f,   columnas = 1, filas = 1),

            // --- PIERNA IZQUIERDA Z: 1-2 (10 cartas) ---
            LayerConfig(z = 1, inicioX = 60f,  inicioY = 470f,   columnas = 2, filas = 3),
            LayerConfig(z = 2, inicioX = 60f,  inicioY = 497.5f, columnas = 2, filas = 2),
            // --- PIERNA DERECHA Z: 1-2 (10 cartas) ---
            LayerConfig(z = 1, inicioX = 225f, inicioY = 470f,   columnas = 2, filas = 3),
            LayerConfig(z = 2, inicioX = 225f, inicioY = 497.5f, columnas = 2, filas = 2),

            // --- COLA (relleno entre piernas) Z: 1-2 (6 cartas) ---
            LayerConfig(z = 1, inicioX = 170f, inicioY = 470f, columnas = 1, filas = 3),
            LayerConfig(z = 2, inicioX = 170f, inicioY = 470f, columnas = 1, filas = 3),

            // --- HOMBROS Z: 8-9 (10 cartas) ---
            LayerConfig(z = 8,  inicioX = 32.5f,  inicioY = 255f,   columnas = 1, filas = 3),
            LayerConfig(z = 8,  inicioX = 307.5f, inicioY = 255f,   columnas = 1, filas = 3),
            LayerConfig(z = 9,  inicioX = 32.5f,  inicioY = 282.5f, columnas = 1, filas = 2),
            LayerConfig(z = 9,  inicioX = 307.5f, inicioY = 282.5f, columnas = 1, filas = 2)
        )

        for (capa in capas) {
            outer@ for (fila in 0 until capa.filas) {
                for (columna in 0 until capa.columnas) {
                    if (indiceTipo >= todosLosTipos.size) break@outer

                    val tipo = todosLosTipos[indiceTipo++]
                    val iconoRespaldo = R.drawable.cangrejo
                    val idIconoRecurso = TileIconMap.icons[tipo] ?: iconoRespaldo
                    fichasLista.add(
                        Tile(
                            id = id++,
                            tipo =tipo,
                            x = capa.inicioX + columna * tamanoFicha,
                            y = capa.inicioY + fila * tamanoFicha,
                            z = capa.z,
                            iconoRecurso =idIconoRecurso
                        )
                    )
                }
            }
        }
        return Level(id = 2, fichas = fichasLista)
    }
}

data class LayerConfig(
    val z: Int,
    val inicioX: Float,
    val inicioY: Float,
    val columnas: Int,
    val filas: Int
)