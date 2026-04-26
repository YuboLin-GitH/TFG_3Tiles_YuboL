package com.example.tfg_3tiles_yubol.viewModel

import androidx.lifecycle.ViewModel
import com.example.tfg_3tiles_yubol.data.model.Tile
import com.example.tfg_3tiles_yubol.domain.CheckBlockUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class GameViewModel() : ViewModel() {


    // private val checkMatchUseCase = CheckMatchUseCase()
     private val checkBlockUseCase = CheckBlockUseCase()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()


    fun loadTiles(tiles: List<Tile>) {
        _gameState.value = _gameState.value.copy(
            tiles = updateBlockedState(tiles))
    }




    //Haz clic -> Comprueba si está bloqueado -> Si no está bloqueado
    // mueve la tarjeta de tiles a trayTiles
    fun onTileClick(tile: Tile) {
        val state = _gameState.value

        // si esta bloqueado, no hacer nada
        if (tile.isBlocked) return


        val newTiles = state.tiles.filter { it.id != tile.id }
        val trayAfterMatch  = state.trayTiles + tile

        _gameState.value = state.copy(
            tiles = updateBlockedState(newTiles),
            trayTiles = trayAfterMatch
        )

    }


    // para cambiar color si esta activa
    private fun updateBlockedState(tiles: List<Tile>): List<Tile> {
        return tiles.map { tile ->
            tile.copy(isBlocked = checkBlockUseCase.isBlocked(tile, tiles))
        }
    }

}