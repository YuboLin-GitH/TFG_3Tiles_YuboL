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

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import androidx.core.content.edit

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
    val score: Int,
    val difficulty: String = "Normal",
    val time_left: Int = 0
)

class GameViewModel() : ViewModel() {

    private var appContext: Context? = null
    private val loginPrefs by lazy {
        appContext!!.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    }

     private val checkMatchUseCase = CheckMatchUseCase()
     private var checkBlockUseCase = CheckBlockUseCase(55f)

    private var currentLevel = 1
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _loginStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val loginStatus: StateFlow<AuthStatus> = _loginStatus.asStateFlow()

    private var timerJob: Job? = null


    fun loadTiles(tiles: List<Tile>) {
        _gameState.value = _gameState.value.copy(
            tiles = updateBlockedState(tiles))
    }


    private var soundManager: SoundManager? = null

    fun initSound(context: Context) {
        appContext = context.applicationContext
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

    fun setDifficulty(difficulty: Difficulty) {
        cancelTimer()
        _gameState.value = _gameState.value.copy(
            difficulty = difficulty,
            remainingTimeSeconds = difficulty.timeSeconds,
            remainingUndos = difficulty.maxUndos,
            remainingShuffles = difficulty.maxShuffles,
            isTimeUp = false
        )
    }

    private fun startTimer() {
        cancelTimer()
        timerJob = viewModelScope.launch {
            while (_gameState.value.remainingTimeSeconds > 0) {
                delay(1000L)
                val state = _gameState.value
                // Pausar el temporizador durante game over, victoria o transición de nivel
                if (state.isGameOver || state.isWin || state.showLevelUp) break
                val newTime = state.remainingTimeSeconds - 1
                if (newTime <= 0) {
                    _gameState.value = state.copy(
                        remainingTimeSeconds = 0,
                        isTimeUp = true,
                        isGameOver = true
                    )
                    saveScoreToSupabase()
                    break
                }
                _gameState.value = state.copy(remainingTimeSeconds = newTime)
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Lógica principal: clic → quitar del tablero → insertar al frente del tray →
    // detectar trío → animar eliminación (250ms) → comprobar victoria/derrota
    fun onTileClick(tile: Tile) {
        val state = _gameState.value

        if (tile.isBlocked || state.isGameOver || state.isAnimating || state.showLevelUp) return

        val newTiles = state.tiles.filter { it.id != tile.id }
        val newTray = listOf(tile) + state.trayTiles
        soundManager?.playClick()

        val matched = checkMatchUseCase.checkMatch(newTray)

        if (matched.isNotEmpty()) {
            _gameState.value = state.copy(
                tiles = updateBlockedState(newTiles),
                trayTiles = newTray,
                eliminatingTiles = matched,
                isAnimating = true
            )
            soundManager?.playMatch()

            viewModelScope.launch {
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
                        saveScoreToSupabase()
                    }
                } else if (afterMatchTray.size >= 7 && currentLevel == 2) {
                    saveScoreToSupabase()
                }
            }
        } else {
            val finalTiles = updateBlockedState(newTiles)
            val won = finalTiles.isEmpty() && newTray.isEmpty()
            _gameState.value = state.copy(
                tiles = finalTiles,
                trayTiles = newTray,
                isGameOver = newTray.size >= 7,
                isWin = won && currentLevel != 1,
                isAnimating = false
            )
            if (won) {
                if (currentLevel == 1) {
                    _gameState.value = _gameState.value.copy(showLevelUp = true)
                    viewModelScope.launch {
                        delay(1500)
                        goToNextLevel()
                    }
                } else {
                    saveScoreToSupabase()
                }
            } else if (newTray.size >= 7 && currentLevel == 2) {
                saveScoreToSupabase()
            }
        }
    }

    companion object {
        private const val ELIMINATE_DURATION_MS = 250L
    }


    // Recalcula isBlocked para todas las cartas tras cada movimiento
    private fun updateBlockedState(tiles: List<Tile>): List<Tile> {
        return tiles.map { tile ->
            tile.copy(isBlocked = checkBlockUseCase.isBlocked(tile, tiles))
        }
    }




    // Avanza al siguiente nivel conservando puntuación, dificultad, tiempo y ayudas restantes
    fun goToNextLevel() {
        val state = _gameState.value
        currentLevel++
        _gameState.value = GameState(
            currentLevel = currentLevel,
            score = state.score,
            difficulty = state.difficulty,
            remainingTimeSeconds = state.remainingTimeSeconds,
            remainingUndos = state.remainingUndos,
            remainingShuffles = state.remainingShuffles
        )
        loadCurrentLevel()
    }
    fun resetGame() {
        cancelTimer()
        currentLevel = 1
        val diff = _gameState.value.difficulty
        _gameState.value = GameState(
            currentLevel = 1,
            difficulty = diff,
            remainingTimeSeconds = diff.timeSeconds,
            remainingUndos = diff.maxUndos,
            remainingShuffles = diff.maxShuffles
        )
        loadCurrentLevel()
    }


    fun loadCurrentLevel() {
        val level = when (currentLevel) {
            1 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
            2 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel2()
            else -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
        }
        loadLevel(level)
        startTimer()
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

    fun undoMove() {
        val state = _gameState.value
        if (state.isAnimating || state.isGameOver || state.showLevelUp || state.trayTiles.isEmpty()) return
        if (state.remainingUndos <= 0) return
        val lastTile = state.trayTiles.first()
        val newTray = state.trayTiles.drop(1)
        val newTiles = state.tiles + lastTile

        _gameState.value = state.copy(
            tiles = updateBlockedState(newTiles),
            trayTiles = newTray,
            isGameOver = false,
            remainingUndos = state.remainingUndos - 1
        )
    }

    fun shuffleTiles() {
        val state = _gameState.value
        if (state.isAnimating || state.isGameOver || state.showLevelUp || state.remainingShuffles <= 0) return
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
            tiles = shuffledTiles,
            remainingShuffles = state.remainingShuffles - 1
        )
    }

    fun tryAutoLogin() {
        val wasLoggedIn = loginPrefs.getBoolean("was_logged_in", false)
        if (!wasLoggedIn) return

        viewModelScope.launch {
            try {
                var user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    _loginStatus.value = AuthStatus.Success
                    return@launch
                }
                // Reintentar: la sesión de Supabase puede tardar en cargar desde SharedPreferences
                repeat(5) {
                    delay(500)
                    user = supabase.auth.currentUserOrNull()
                    if (user != null) {
                        _loginStatus.value = AuthStatus.Success
                        return@launch
                    }
                }
                loginPrefs.edit { putBoolean("was_logged_in", false) }
            } catch (_: Exception) {
                loginPrefs.edit { putBoolean("was_logged_in", false) }
            }
        }
    }

    fun getSavedEmail(): String = loginPrefs.getString("email", "") ?: ""

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = AuthStatus.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                _loginStatus.value = AuthStatus.Success
                loginPrefs.edit {
                    putBoolean("was_logged_in", true)
                    putString("email", email)
                }
            } catch (e: Exception) {
                _loginStatus.value = AuthStatus.Error(translateError(e.message))
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = AuthStatus.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _loginStatus.value = AuthStatus.Success
                loginPrefs.edit {
                    putBoolean("was_logged_in", true)
                    putString("email", email)
                }
            } catch (e: Exception) {
                _loginStatus.value = AuthStatus.Error(translateError(e.message))
            }
        }
    }

    private fun translateError(message: String?): String {
        val texto = message ?: return "Error desconocido"
        return when {
            texto.contains("Invalid login credentials", ignoreCase = true) ->
                "Credenciales inválidas"
            texto.contains("User already registered", ignoreCase = true) ||
            texto.contains("already exists", ignoreCase = true) ||
            texto.contains("already been registered", ignoreCase = true) ||
            texto.contains("already registered", ignoreCase = true) ->
                "El usuario ya está registrado"
            texto.contains("Email not confirmed", ignoreCase = true) ||
            texto.contains("not confirmed", ignoreCase = true) ->
                "Email no confirmado. Revisa tu bandeja de entrada"
            texto.contains("invalid format", ignoreCase = true) ||
            texto.contains("Unable to validate email", ignoreCase = true) ||
            texto.contains("valid email", ignoreCase = true) ||
            texto.contains("invalid email", ignoreCase = true) ->
                "Formato de email inválido"
            texto.contains("Password should be at least", ignoreCase = true) ||
            texto.contains("Password is too short", ignoreCase = true) ||
            texto.contains("at least 6 characters", ignoreCase = true) ||
            texto.contains("too short", ignoreCase = true) ->
                "La contraseña debe tener al menos 6 caracteres"
            texto.contains("Email rate limit", ignoreCase = true) ||
            texto.contains("rate limit", ignoreCase = true) ||
            texto.contains("too many requests", ignoreCase = true) ->
                "Demasiados intentos. Espera un momento"
            texto.contains("User not found", ignoreCase = true) ||
            texto.contains("not found", ignoreCase = true) ->
                "Usuario no encontrado"
            texto.contains("Password is required", ignoreCase = true) ||
            texto.contains("password is required", ignoreCase = true) ||
            texto.contains("requires a valid password", ignoreCase = true) ||
            texto.contains("Signup requires a valid password", ignoreCase = true) ||
            texto.contains("valid password", ignoreCase = true) ->
                "La contraseña es obligatoria"
            texto.contains("Email is required", ignoreCase = true) ||
            texto.contains("email is required", ignoreCase = true) ||
            texto.contains("An email", ignoreCase = true) ||
            texto.contains("email address", ignoreCase = true) ->
                "El email es obligatorio"
            texto.contains("Password should be", ignoreCase = true) ||
            texto.contains("password should", ignoreCase = true) ||
            texto.contains("weak password", ignoreCase = true) ||
            texto.contains("not strong enough", ignoreCase = true) ->
                "La contraseña no es lo suficientemente segura"
            else -> texto
        }
    }

    fun resetAuthStatus() {
        _loginStatus.value = AuthStatus.Idle
    }

    fun logout() {
        cancelTimer()
        loginPrefs.edit { clear() }
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _loginStatus.value = AuthStatus.Idle
                resetGame()
            } catch (e: Exception) {
                println("Error al cerrar sesión: ${e.message}")
            }
        }
    }

    @Serializable
    data class RankingRecord(
        val id: Int = 0,
        val user_id: String = "",
        val email: String = "",
        val score: Int = 0,
        val difficulty: String = "Normal",
        val time_left: Int = 0
    )

    private val _rankings = MutableStateFlow<List<RankingRecord>>(emptyList())
    val rankings: StateFlow<List<RankingRecord>> = _rankings.asStateFlow()

    fun fetchRankings() {
        viewModelScope.launch {
            try {
                val data = supabase.from("rankings").select()
                    .decodeList<RankingRecord>()
                // Cada usuario tiene una entrada por dificultad (mejor puntuación en cada una)
                _rankings.value = data
                    .groupBy { it.user_id to it.difficulty }
                    .map { (_, records) -> records.maxBy { it.score } }
                    .sortedWith(compareByDescending<RankingRecord> { it.score }
                        .thenByDescending { it.time_left })
            } catch (e: Exception) {
                println("Error fetch rankings: ${e.message}")
            }
        }
    }

    fun saveScoreToSupabase() {
        val state = _gameState.value
        viewModelScope.launch {
            try {
                val usuarioActual = supabase.auth.currentUserOrNull()
                if (usuarioActual != null) {
                    val registro = RankingRequest(
                        user_id = usuarioActual.id,
                        email = usuarioActual.email ?: "Anónimo",
                        score = state.score,
                        difficulty = state.difficulty.label,
                        time_left = state.remainingTimeSeconds
                    )
                    supabase.from("rankings").insert(registro)
                    println("Puntuacion guardada: ${state.score}, dificultad: ${state.difficulty.label}")
                } else {
                    println("No hay usuario autenticado")
                }
            } catch (e: Exception) {
                println("Error al guardar puntuacion: ${e.message}")
            }
        }
    }

}
