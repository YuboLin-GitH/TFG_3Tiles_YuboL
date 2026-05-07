package com.example.tfg_3tiles_yubol.viewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.tfg_3tiles_yubol.data.model.Level
import com.example.tfg_3tiles_yubol.data.model.Tile
import com.example.tfg_3tiles_yubol.domain.CheckBlockUseCase
import com.example.tfg_3tiles_yubol.domain.CheckMatchUseCase
import com.example.tfg_3tiles_yubol.utils.SoundManager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class GameViewModel() : ViewModel() {


     private val checkMatchUseCase = CheckMatchUseCase()
     private val checkBlockUseCase = CheckBlockUseCase()

    private var currentLevel = 1
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()


    fun loadTiles(tiles: List<Tile>) {
        _gameState.value = _gameState.value.copy(
            tiles = updateBlockedState(tiles))
    }


    //musica
    private var soundManager: SoundManager? = null

    fun initSound(context: Context) {
        soundManager = SoundManager(context)
        soundManager?.startBGM()
    }

    fun pauseMusic() {
        soundManager?.pauseBGM()
    }

    fun resumeMusic() {
        soundManager?.startBGM()
    }

    fun releaseMusic() {
        soundManager?.release()
        soundManager = null
    }

    //Haz clic -> Comprueba si está bloqueado -> Si no está bloqueado
    // mueve la tarjeta de tiles a trayTiles
    fun onTileClick(tile: Tile) {
        val state = _gameState.value

        // si esta bloqueado o terminado, no hacer nada
        if (tile.isBlocked || state.isGameOver) return


        val newTiles = state.tiles.filter { it.id != tile.id }

        var newTray = state.trayTiles + tile

        val matchedTiles = checkMatchUseCase.checkMatch(newTray)
        var newScore = state.score

        soundManager?.playClick()

        if (matchedTiles.isNotEmpty()) {
            newTray = newTray.filterNot { matchedTiles.contains(it) }
            newScore += 10
        }

        val isGameOver = newTray.size >= 7
        val isWin = newTiles.isEmpty() && newTray.isEmpty()

        _gameState.value = state.copy(
            tiles = updateBlockedState(newTiles),
            trayTiles = newTray,
            score = newScore,
            isGameOver = isGameOver,
            isWin = isWin
        )

    }


    // para actualizar el estado de ocultación de la tarjeta
    private fun updateBlockedState(tiles: List<Tile>): List<Tile> {
        return tiles.map { tile ->
            tile.copy(isBlocked = checkBlockUseCase.isBlocked(tile, tiles))
        }
    }




    fun goToNextLevel() {
        currentLevel++
        _gameState.value = GameState(currentLevel = currentLevel)
        loadCurrentLevel()
    }
    fun resetGame() {
        _gameState.value = GameState(currentLevel = currentLevel)
        loadCurrentLevel()
    }


    fun loadCurrentLevel() {
        val level = when (currentLevel) {
            1 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
            2 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel2()
            else -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
        }
        loadLevel(level)
    }

    fun loadLevel(level: Level) {
        loadTiles(level.tiles)
    }

    // Deshacer
    fun undoMove() {
        val state = _gameState.value
        // si hay carta es vuelve atras
        if (state.trayTiles.isNotEmpty()) {
            val lastTile = state.trayTiles.last() // ultimo carta
            val newTray = state.trayTiles.dropLast(1) // quitar ultimo carta
            val newTiles = state.tiles + lastTile // este carta vuelve al pantalla de juego

            _gameState.value = state.copy(
                tiles = updateBlockedState(newTiles),
                trayTiles = newTray,
                isGameOver = false
            )
        }
    }

    // Mezclar
    fun shuffleTiles() {
        val state = _gameState.value
        val currentTiles = state.tiles

        // Extrae todos los iconos del escritorio y cambia su orden.
        val shuffledIcons = currentTiles.map { it.iconRes }.shuffled()

        // Solo se reenvían los iconos a las cartas.
        val shuffledTiles = currentTiles.mapIndexed { index, tile ->
            tile.copy(iconRes = shuffledIcons[index])
        }

        // Actualizar
        _gameState.value = state.copy(
            tiles = shuffledTiles
        )
    }


}

