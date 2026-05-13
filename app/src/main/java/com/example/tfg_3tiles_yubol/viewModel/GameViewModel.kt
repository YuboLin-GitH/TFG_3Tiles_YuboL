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
import com.example.tfg_3tiles_yubol.utils.supabase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

sealed class AuthStatus {
    data object Idle : AuthStatus()
    data object Loading : AuthStatus()
    data object Success : AuthStatus()
    data class Error(val message: String) : AuthStatus()
}

@Serializable
data class RankingRequest(
    val user_id: String,
    val email: String,
    val score: Int
)

class GameViewModel() : ViewModel() {


     private val checkMatchUseCase = CheckMatchUseCase()
     private var checkBlockUseCase = CheckBlockUseCase(55f)

    private var currentLevel = 1
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _loginStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val loginStatus: StateFlow<AuthStatus> = _loginStatus.asStateFlow()


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
                val won = finalTiles.isEmpty() && afterMatchTray.isEmpty()
                _gameState.value = final.copy(
                    tiles = finalTiles,
                    trayTiles = afterMatchTray,
                    score = newScore,
                    eliminatingTiles = emptyList(),
                    isGameOver = afterMatchTray.size >= 7,
                    isWin = won && currentLevel != 1,
                    isAnimating = false
                )
                if (won) {
                    if (currentLevel == 1) {
                        _gameState.value = _gameState.value.copy(showLevelUp = true)
                        delay(1500)
                        goToNextLevel()
                    } else {
                        guardarPuntuacionEnSupabase(newScore)
                    }
                }
            } else {
                val finalTiles = updateBlockedState(s.tiles)
                val won = finalTiles.isEmpty() && newTray.isEmpty()
                _gameState.value = s.copy(
                    tiles = finalTiles,
                    trayTiles = newTray,
                    flyingTile = null,
                    isGameOver = newTray.size >= 7,
                    isWin = won && currentLevel != 1,
                    isAnimating = false
                )
                if (won) {
                    if (currentLevel == 1) {
                        _gameState.value = _gameState.value.copy(showLevelUp = true)
                        delay(1500)
                        goToNextLevel()
                    } else {
                        guardarPuntuacionEnSupabase(s.score)
                    }
                }
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
        val previousScore = _gameState.value.score
        currentLevel++
        _gameState.value = GameState(currentLevel = currentLevel, score = previousScore)
        loadCurrentLevel()
    }
    fun resetGame() {
        currentLevel = 1
        _gameState.value = GameState(currentLevel = 1)
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

    fun registrarUsuario(correo: String, contrasena: String) {
        viewModelScope.launch {
            _loginStatus.value = AuthStatus.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    email = correo
                    password = contrasena
                }
                _loginStatus.value = AuthStatus.Success
            } catch (e: Exception) {
                _loginStatus.value = AuthStatus.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        viewModelScope.launch {
            _loginStatus.value = AuthStatus.Loading
            try {
                supabase.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }
                _loginStatus.value = AuthStatus.Success
            } catch (e: Exception) {
                _loginStatus.value = AuthStatus.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetAuthStatus() {
        _loginStatus.value = AuthStatus.Idle
    }

    @Serializable
    data class RankingRecord(
        val id: Int = 0,
        val user_id: String = "",
        val email: String = "",
        val score: Int = 0
    )

    private val _rankings = MutableStateFlow<List<RankingRecord>>(emptyList())
    val rankings: StateFlow<List<RankingRecord>> = _rankings.asStateFlow()

    fun fetchRankings() {
        viewModelScope.launch {
            try {
                val data = supabase.from("rankings").select()
                    .decodeList<RankingRecord>()
                _rankings.value = data.sortedByDescending { it.score }
            } catch (e: Exception) {
                println("Error fetch rankings: ${e.message}")
            }
        }
    }

    fun guardarPuntuacionEnSupabase(puntos: Int) {
        viewModelScope.launch {
            try {
                val usuarioActual = supabase.auth.currentUserOrNull()
                if (usuarioActual != null) {
                    val registro = RankingRequest(
                        user_id = usuarioActual.id,
                        email = usuarioActual.email ?: "Anónimo",
                        score = puntos
                    )
                    supabase.from("rankings").insert(registro)
                    println("Puntuacion guardada: $puntos")
                } else {
                    println("No hay usuario autenticado")
                }
            } catch (e: Exception) {
                println("Error al guardar puntuacion: ${e.message}")
            }
        }
    }

}

