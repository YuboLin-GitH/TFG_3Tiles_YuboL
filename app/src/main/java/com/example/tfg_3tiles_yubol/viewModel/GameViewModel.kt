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

     private val comprobarCoincidencia = CheckMatchUseCase()
     private var comprobarBloqueo = CheckBlockUseCase(55f)

    private var nivelActual = 1
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _loginStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val loginStatus: StateFlow<AuthStatus> = _loginStatus.asStateFlow()

    private var temporizador: Job? = null


    fun cargarFichas(fichas: List<Tile>) {
        _gameState.value = _gameState.value.copy(
            fichas =actualizarBloqueos(fichas))
    }


    private var gestorSonido: SoundManager? = null

    fun iniciarSonido(context: Context) {
        appContext = context.applicationContext
        gestorSonido = SoundManager(context)
        gestorSonido?.iniciarMusicaFondo()
    }

    fun pausarMusica() {
        gestorSonido?.pausarMusicaFondo()
    }

    fun reanudarMusica() {
        gestorSonido?.iniciarMusicaFondo()
    }

    fun liberarMusica() {
        gestorSonido?.liberar()
        gestorSonido = null
    }

    fun cambiarDificultad(dificultad: Difficulty) {
        cancelarTemporizador()
        _gameState.value = _gameState.value.copy(
            dificultad = dificultad,
            tiempoRestante = dificultad.tiempoSegundos,
            deshacerRestantes = dificultad.maxDeshacer,
            mezclasRestantes = dificultad.maxMezclas,
            tiempoAgotado = false
        )
    }

    private fun iniciarTemporizador() {
        cancelarTemporizador()
        temporizador = viewModelScope.launch {
            while (_gameState.value.tiempoRestante > 0) {
                delay(1000L)
                val state = _gameState.value
                // Pausar el temporizador durante game over, victoria o transición de nivel
                if (state.juegoTerminado || state.victoria || state.mostrarSubidaNivel) break
                val nuevoTiempo = state.tiempoRestante - 1
                if (nuevoTiempo <= 0) {
                    _gameState.value = state.copy(
                        tiempoRestante = 0,
                        tiempoAgotado = true,
                        juegoTerminado = true
                    )
                    guardarPuntuacion()
                    break
                }
                _gameState.value = state.copy(tiempoRestante = nuevoTiempo)
            }
        }
    }

    private fun cancelarTemporizador() {
        temporizador?.cancel()
        temporizador = null
    }

    // Lógica principal: clic → quitar del tablero → insertar al frente del tray →
    // detectar trío → animar eliminación (250ms) → comprobar victoria/derrota
    fun pulsarFicha(tile: Tile) {
        val state = _gameState.value

        if (tile.estaBloqueada || state.juegoTerminado || state.animando || state.mostrarSubidaNivel) return

        val nuevasFichas = state.fichas.filter { it.id != tile.id }
        val nuevaBandeja = listOf(tile) + state.fichasBandeja
        gestorSonido?.reproducirClick()

        val coincidencias = comprobarCoincidencia.buscarTrios(nuevaBandeja)

        if (coincidencias.isNotEmpty()) {
            _gameState.value = state.copy(
                fichas =actualizarBloqueos(nuevasFichas),
                fichasBandeja = nuevaBandeja,
                fichasEliminando = coincidencias,
                animando = true
            )
            gestorSonido?.reproducirCoincidencia()

            viewModelScope.launch {
                delay(ELIMINATE_DURATION_MS)

                val final = _gameState.value
                val bandejaTrasCoincidencia = final.fichasBandeja.filterNot { t -> coincidencias.any { it.id == t.id } }
                val nuevaPuntuacion = final.puntuacion + 10
                val fichasFinales = actualizarBloqueos(final.fichas)
                val ganado = fichasFinales.isEmpty() && bandejaTrasCoincidencia.isEmpty()
                _gameState.value = final.copy(
                    fichas =fichasFinales,
                    fichasBandeja = bandejaTrasCoincidencia,
                    puntuacion = nuevaPuntuacion,
                    fichasEliminando = emptyList(),
                    juegoTerminado = bandejaTrasCoincidencia.size >= 7,
                    victoria = ganado && nivelActual != 1,
                    animando = false
                )
                if (ganado) {
                    if (nivelActual == 1) {
                        _gameState.value = _gameState.value.copy(mostrarSubidaNivel = true)
                        delay(1500)
                        siguienteNivel()
                    } else {
                        guardarPuntuacion()
                    }
                } else if (bandejaTrasCoincidencia.size >= 7 && nivelActual == 2) {
                    guardarPuntuacion()
                }
            }
        } else {
            val fichasFinales = actualizarBloqueos(nuevasFichas)
            val ganado = fichasFinales.isEmpty() && nuevaBandeja.isEmpty()
            _gameState.value = state.copy(
                fichas =fichasFinales,
                fichasBandeja = nuevaBandeja,
                juegoTerminado = nuevaBandeja.size >= 7,
                victoria = ganado && nivelActual != 1,
                animando = false
            )
            if (ganado) {
                if (nivelActual == 1) {
                    _gameState.value = _gameState.value.copy(mostrarSubidaNivel = true)
                    viewModelScope.launch {
                        delay(1500)
                        siguienteNivel()
                    }
                } else {
                    guardarPuntuacion()
                }
            } else if (nuevaBandeja.size >= 7 && nivelActual == 2) {
                guardarPuntuacion()
            }
        }
    }

    companion object {
        private const val ELIMINATE_DURATION_MS = 250L
    }


    // Recalcula estaBloqueada para todas las cartas tras cada movimiento
    private fun actualizarBloqueos(fichas: List<Tile>): List<Tile> {
        return fichas.map { tile ->
            tile.copy(estaBloqueada = comprobarBloqueo.estaBloqueado(tile, fichas))
        }
    }




    // Avanza al siguiente nivel conservando puntuación, dificultad, tiempo y ayudas restantes
    fun siguienteNivel() {
        val state = _gameState.value
        nivelActual++
        _gameState.value = GameState(
            nivelActual = nivelActual,
            puntuacion = state.puntuacion,
            dificultad = state.dificultad,
            tiempoRestante = state.tiempoRestante,
            deshacerRestantes = state.deshacerRestantes,
            mezclasRestantes = state.mezclasRestantes
        )
        cargarNivelActual()
    }
    fun reiniciarJuego() {
        cancelarTemporizador()
        nivelActual = 1
        val dificultadLocal= _gameState.value.dificultad
        _gameState.value = GameState(
            nivelActual = 1,
            dificultad = dificultadLocal,
            tiempoRestante = dificultadLocal.tiempoSegundos,
            deshacerRestantes = dificultadLocal.maxDeshacer,
            mezclasRestantes = dificultadLocal.maxMezclas
        )
        cargarNivelActual()
    }


    fun cargarNivelActual() {
        val nivel = when (nivelActual) {
            1 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
            2 -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel2()
            else -> com.example.tfg_3tiles_yubol.data.local.LevelData.getLevel1()
        }
        cargarNivel(nivel)
        iniciarTemporizador()
    }

    fun cargarNivel(nivel: Level) {
        cargarFichas(nivel.fichas)
    }

    fun obtenerVolumenEfectos(): Float = gestorSonido?.volumenEfectos ?: 1f
    fun obtenerVolumenMusica(): Float = gestorSonido?.volumenMusica ?: 1f

    fun cambiarVolumenEfectos(volume: Float) {
        gestorSonido?.cambiarVolumenEfectos(volume)
    }

    fun cambiarVolumenMusica(volume: Float) {
        gestorSonido?.cambiarVolumenMusica(volume)
    }

    fun deshacerMovimiento() {
        val state = _gameState.value
        if (state.animando || state.juegoTerminado || state.mostrarSubidaNivel || state.fichasBandeja.isEmpty()) return
        if (state.deshacerRestantes <= 0) return
        val ultimaFicha = state.fichasBandeja.first()
        val nuevaBandeja = state.fichasBandeja.drop(1)
        val nuevasFichas = state.fichas + ultimaFicha

        _gameState.value = state.copy(
            fichas =actualizarBloqueos(nuevasFichas),
            fichasBandeja = nuevaBandeja,
            juegoTerminado = false,
            deshacerRestantes = state.deshacerRestantes - 1
        )
    }

    fun mezclarFichas() {
        val state = _gameState.value
        if (state.animando || state.juegoTerminado || state.mostrarSubidaNivel || state.mezclasRestantes <= 0) return
        val fichasActuales = state.fichas

        val tiposMezclados = fichasActuales.map { it.tipo }.shuffled()

        val fichasMezcladas = fichasActuales.mapIndexed { index, tile ->
            val nuevoTipo = tiposMezclados[index]
            tile.copy(
                tipo = nuevoTipo,
                iconoRecurso = TileIconMap.icons[nuevoTipo]!!
            )
        }

        _gameState.value = state.copy(
            fichas =fichasMezcladas,
            mezclasRestantes = state.mezclasRestantes - 1
        )
    }

    fun intentarAutoLogin() {
        val estabaLogueado = loginPrefs.getBoolean("was_logged_in", false)
        if (!estabaLogueado) return

        viewModelScope.launch {
            try {
                var usuario= supabase.auth.currentUserOrNull()
                if (usuario != null) {
                    _loginStatus.value = AuthStatus.Success
                    return@launch
                }
                // Reintentar: la sesión de Supabase puede tardar en cargar desde SharedPreferences
                repeat(5) {
                    delay(500)
                    usuario = supabase.auth.currentUserOrNull()
                    if (usuario != null) {
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

    fun obtenerEmailGuardado(): String = loginPrefs.getString("email", "") ?: ""

    fun registrarUsuario(email: String, password: String) {
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
                _loginStatus.value = AuthStatus.Error(traducirError(e.message))
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
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
                _loginStatus.value = AuthStatus.Error(traducirError(e.message))
            }
        }
    }

    private fun traducirError(message: String?): String {
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

    fun limpiarEstadoAuth() {
        _loginStatus.value = AuthStatus.Idle
    }

    fun cerrarSesion() {
        cancelarTemporizador()
        loginPrefs.edit { clear() }
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _loginStatus.value = AuthStatus.Idle
                reiniciarJuego()
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

    fun cargarRankings() {
        viewModelScope.launch {
            try {
                val datos = supabase.from("rankings").select()
                    .decodeList<RankingRecord>()
                // Cada usuario tiene una entrada por dificultad (mejor puntuación en cada una)
                _rankings.value = datos
                    .groupBy { it.user_id to it.difficulty }
                    .map { (_, records) -> records.maxBy { it.score } }
                    .sortedWith(compareByDescending<RankingRecord> { it.score }
                        .thenByDescending { it.time_left })
            } catch (e: Exception) {
                println("Error fetch rankings: ${e.message}")
            }
        }
    }

    fun guardarPuntuacion() {
        val state = _gameState.value
        viewModelScope.launch {
            try {
                val usuarioActual = supabase.auth.currentUserOrNull()
                if (usuarioActual != null) {
                    val registro = RankingRequest(
                        user_id = usuarioActual.id,
                        email = usuarioActual.email ?: "Anónimo",
                        score = state.puntuacion,
                        difficulty = state.dificultad.etiqueta,
                        time_left = state.tiempoRestante
                    )
                    supabase.from("rankings").insert(registro)
                    println("Puntuacion guardada: ${state.puntuacion}, dificultad: ${state.dificultad.etiqueta}")
                } else {
                    println("No hay usuario autenticado")
                }
            } catch (e: Exception) {
                println("Error al guardar puntuacion: ${e.message}")
            }
        }
    }

}
