package com.example.tfg_3tiles_yubol.viewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_3tiles_yubol.data.model.Level
import com.example.tfg_3tiles_yubol.data.model.Tile
import com.example.tfg_3tiles_yubol.data.local.TileIconMap
import com.example.tfg_3tiles_yubol.domain.CheckBlockUseCase
import com.example.tfg_3tiles_yubol.domain.CheckMatchUseCase
import com.example.tfg_3tiles_yubol.utils.SoundManager

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class GameViewModel() : ViewModel() {


     private val checkMatchUseCase = CheckMatchUseCase()
     private var checkBlockUseCase = CheckBlockUseCase(55f)

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

        if (tile.isBlocked || state.isGameOver || state.isAnimating) return

        val newTiles = state.tiles.filter { it.id != tile.id }

        _gameState.value = state.copy(
            tiles = updateBlockedState(newTiles),
            flyingTile = tile,
            isAnimating = true
        )
        soundManager?.playClick()

        viewModelScope.launch {
            delay(FLY_DURATION_MS)

            val s = _gameState.value
            val newTray = s.trayTiles + tile
            val matched = checkMatchUseCase.checkMatch(newTray)

            if (matched.isNotEmpty()) {
                _gameState.value = s.copy(
                    trayTiles = newTray,
                    flyingTile = null,
                    eliminatingTiles = matched,
                    isAnimating = true
                )
                soundManager?.playMatch()

                delay(ELIMINATE_DURATION_MS)

                val final = _gameState.value
                val afterMatchTray = final.trayTiles.filterNot { t -> matched.any { it.id == t.id } }
                val newScore = final.score + 10
                val finalTiles = updateBlockedState(final.tiles)
                _gameState.value = final.copy(
                    tiles = finalTiles,
                    trayTiles = afterMatchTray,
                    score = newScore,
                    eliminatingTiles = emptyList(),
                    isGameOver = afterMatchTray.size >= 7,
                    isWin = finalTiles.isEmpty() && afterMatchTray.isEmpty(),
                    isAnimating = false
                )
            } else {
                val finalTiles = updateBlockedState(s.tiles)
                _gameState.value = s.copy(
                    tiles = finalTiles,
                    trayTiles = newTray,
                    flyingTile = null,
                    isGameOver = newTray.size >= 7,
                    isWin = finalTiles.isEmpty() && newTray.isEmpty(),
                    isAnimating = false
                )
            }
        }
    }

    companion object {
        private const val FLY_DURATION_MS = 200L
        private const val ELIMINATE_DURATION_MS = 250L
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

    fun getSfxVolume(): Float = soundManager?.sfxVolume ?: 1f
    fun getBgmVolume(): Float = soundManager?.bgmVolume ?: 1f

    fun setSfxVolume(volume: Float) {
        soundManager?.setSfxVolume(volume)
    }

    fun setBgmVolume(volume: Float) {
        soundManager?.setBgmVolume(volume)
    }

    // Deshacer
    fun undoMove() {
        val state = _gameState.value
        if (state.isAnimating || state.trayTiles.isEmpty()) return
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
        if (state.isAnimating) return
        val currentTiles = state.tiles

        val shuffledTypes = currentTiles.map { it.type }.shuffled()

        val shuffledTiles = currentTiles.mapIndexed { index, tile ->
            val newType = shuffledTypes[index]
            tile.copy(
                type = newType,
                iconRes = TileIconMap.icons[newType]!!
            )
        }

        _gameState.value = state.copy(
            tiles = shuffledTiles
        )
    }


}

