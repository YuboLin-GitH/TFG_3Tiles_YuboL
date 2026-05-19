package com.example.tfg_3tiles_yubol.ui


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel
import com.example.tfg_3tiles_yubol.R
import com.example.tfg_3tiles_yubol.data.model.Tile

@Composable
fun GameScreen(viewModel: GameViewModel, onViewRanking: () -> Unit = {}, onBackToMenu: () -> Unit = {}) {
    val state by viewModel.gameState.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var sfxVolume by remember { mutableFloatStateOf(viewModel.obtenerVolumenEfectos()) }
    var bgmVolume by remember { mutableFloatStateOf(viewModel.obtenerVolumenMusica()) }

    LaunchedEffect(Unit) {
        viewModel.cargarNivelActual()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = Color.White,
                modifier = Modifier.size(28.dp).clickable { showSettings = true }
            )
            Text(
                text = "Nivel ${state.nivelActual}",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            val minutes = state.tiempoRestante / 60
            val seconds = state.tiempoRestante % 60
            val timeText = "%02d:%02d".format(minutes, seconds)
            val isLowTime = state.tiempoRestante < 60
            Text(
                text = timeText,
                fontSize = 20.sp,
                color = if (isLowTime) Color(0xFFFF4444) else Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Renderizar por profundidad: z bajo primero (atrás), z alto encima (delante)
        state.fichas.sortedBy { it.z }.forEach { tile ->
            androidx.compose.runtime.key(tile.id) {
                TileComponent(
                    tile = tile,
                    tamanoFicha = state.tamanoFicha,
                    onClick = { viewModel.pulsarFicha(tile) }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.deshacerMovimiento() },
                    enabled = state.deshacerRestantes > 0
                ) {
                    Text("Deshacer (${state.deshacerRestantes})")
                }
                Button(
                    onClick = { viewModel.mezclarFichas() },
                    enabled = state.mezclasRestantes > 0
                ) {
                    Text("Mezclar (${state.mezclasRestantes})")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tray_carta),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(
                        items = state.fichasBandeja,
                        key = { it.id }
                    ) { tile ->
                        val isEliminating = state.fichasEliminando.any { it.id == tile.id }
                        // En el tray las cartas no tienen posición absoluta, se ordenan en LazyRow
                        TileComponent(
                            tile = tile.copy(x = 0f, y = 0f),
                            tamanoFicha = state.tamanoFicha,
                            onClick = {},
                            modifier = Modifier.animateItem(),
                            isEliminating = isEliminating
                        )
                    }
                }
            }
        }

        if (state.juegoTerminado) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.tiempoAgotado) {
                        Text(
                            text = "⏰ ¡Tiempo agotado!",
                            fontSize = 36.sp,
                            color = Color(0xFFFF4444),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Fin del juego",
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Puntuación: ${state.puntuacion}",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { viewModel.reiniciarJuego() }) {
                            Text("Reintentar")
                        }
                        Button(onClick = {
                            viewModel.reiniciarJuego()
                            onBackToMenu()
                        }) {
                            Text("Inicio")
                        }
                    }
                }
            }
        }
        if (showSettings) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSettings = false }
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D1B2A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Ajustes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Efectos: ${(sfxVolume * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = sfxVolume,
                        onValueChange = {
                            sfxVolume = it
                            viewModel.cambiarVolumenEfectos(it)
                        }
                    )
                    Text(
                        text = "Música: ${(bgmVolume * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = bgmVolume,
                        onValueChange = {
                            bgmVolume = it
                            viewModel.cambiarVolumenMusica(it)
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSettings = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E8B57)
                            )
                        ) {
                            Text("Cerrar", fontSize = 14.sp, color = Color.White)
                        }
                        Button(
                            onClick = {
                                showSettings = false
                                viewModel.reiniciarJuego()
                                onBackToMenu()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Inicio", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (state.mostrarSubidaNivel) {
            val scale = remember { Animatable(0.5f) }
            LaunchedEffect(Unit) {
                scale.animateTo(1f, tween(400))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚡",
                        fontSize = 64.sp,
                        modifier = Modifier.scale(scale.value)
                    )
                    Text(
                        text = "¡Dificultad aumentada!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nivel 2",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }

        if (state.victoria) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = " ¡Ganaste!",
                        fontSize = 36.sp,
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Puntuación: ${state.puntuacion}",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Button(onClick = onViewRanking) {
                        Text("Ver Ranking")
                    }
                    Button(onClick = {
                        viewModel.reiniciarJuego()
                        onBackToMenu()
                    }) {
                        Text("Inicio")
                    }
                }
            }
        }
    }
}


@Composable
fun TileComponent(
    tile: Tile,
    tamanoFicha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEliminating: Boolean = false
) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(isEliminating) {
        if (isEliminating) {
            scale.animateTo(1.3f, tween(200))
            alpha.animateTo(0f, tween(250))
        } else {
            scale.snapTo(1f)
            alpha.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .offset(x = tile.x.dp, y = tile.y.dp)
            .size(tamanoFicha.dp)
            .scale(scale.value)
            .alpha(alpha.value)
            .clickable(enabled = !tile.estaBloqueada && !isEliminating) { onClick() }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = tile.iconoRecurso),
                contentDescription = "Tile Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }

        if (tile.estaBloqueada) {
            // Capa semitransparente sobre cartas bloqueadas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
            )
        }
    }
}