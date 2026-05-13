package com.example.tfg_3tiles_yubol.ui


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel
import com.example.tfg_3tiles_yubol.R
import com.example.tfg_3tiles_yubol.data.model.Tile
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel, onViewRanking: () -> Unit = {}, onBackToMenu: () -> Unit = {}) {
    val state by viewModel.gameState.collectAsState()
    val density = LocalDensity.current

    var boxHeightPx by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var sfxVolume by remember { mutableStateOf(1f) }
    var bgmVolume by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentLevel()
    }

    // Tray area from bottom: 16dp padding + ~48dp buttons + 8dp gap + 135dp tray
    val trayCenterFromBottomDp = 16 + 48 + 8 + 135 / 2
    val trayStartXDp = 16

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { boxHeightPx = it.size.height }
    ) {

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
            Text(
                text = "⚙",
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.clickable { showSettings = true }
            )
            Text(
                text = "Nivel ${state.currentLevel}",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${state.score}",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        state.tiles.sortedBy { it.z }.forEach { tile ->
            androidx.compose.runtime.key(tile.id) {
                TileComponent(
                    tile = tile,
                    tileSize = state.tileSize,
                    onClick = { viewModel.onTileClick(tile) }
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
                Button(onClick = { viewModel.undoMove() }) {
                    Text("Deshacer ")
                }
                Button(onClick = { viewModel.shuffleTiles() }) {
                    Text("Mezclar ")
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
                        items = state.trayTiles,
                        key = { it.id }
                    ) { tile ->
                        val isEliminating = state.eliminatingTiles.any { it.id == tile.id }
                        TileComponent(
                            tile = tile.copy(x = 0f, y = 0f),
                            tileSize = state.tileSize,
                            onClick = {},
                            modifier = Modifier.animateItem(),
                            isEliminating = isEliminating
                        )
                    }
                }
            }
        }

        // Flying tile overlay
        if (state.flyingTile != null) {
            val flyTile = state.flyingTile!!
            val tilePx = (state.tileSize * density.density).roundToInt()
            val startX = (flyTile.x * density.density).roundToInt()
            val startY = (flyTile.y * density.density).roundToInt()

            val trayStartX = with(density) { trayStartXDp.dp.roundToPx() }
            val trayY = boxHeightPx - with(density) { trayCenterFromBottomDp.dp.roundToPx() }

            val targetX = trayStartX + state.trayTiles.size *
                    (tilePx + with(density) { 4.dp.roundToPx() })

            val offsetAnim = remember {
                Animatable(
                    initialValue = IntOffset(startX, startY),
                    typeConverter = IntOffset.VectorConverter
                )
            }

            LaunchedEffect(flyTile.id) {
                offsetAnim.animateTo(
                    targetValue = IntOffset(targetX, trayY),
                    animationSpec = tween(durationMillis = 200)
                )
            }

            Box(
                modifier = Modifier
                    .offset { offsetAnim.value }
                    .size(state.tileSize.dp)
            ) {
                TileComponent(
                    tile = flyTile.copy(x = 0f, y = 0f),
                    tileSize = state.tileSize,
                    onClick = {}
                )
            }
        }

        if (state.isGameOver) {
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
                        text = " Game Over",
                        fontSize = 36.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Puntuación: ${state.score}",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Button(onClick = { viewModel.resetGame() }) {
                        Text("Reintentar")
                    }
                }
            }
        }
        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Ajustes") },
                text = {
                    Column {
                        Text("Efectos de sonido")
                        Slider(
                            value = sfxVolume,
                            onValueChange = {
                                sfxVolume = it
                                viewModel.setSfxVolume(it)
                            }
                        )
                        Text("Música de fondo")
                        Slider(
                            value = bgmVolume,
                            onValueChange = {
                                bgmVolume = it
                                viewModel.setBgmVolume(it)
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showSettings = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (state.showLevelUp) {
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

        if (state.isWin) {
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
                        text = "Puntuación: ${state.score}",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    if (state.currentLevel == 1) {
                        Button(onClick = { viewModel.goToNextLevel() }) {
                            Text("Siguiente nivel →")
                        }
                    } else {
                        Button(onClick = onViewRanking) {
                            Text("Ver Ranking")
                        }
                    }
                    Button(onClick = {
                        viewModel.resetGame()
                        onBackToMenu()
                    }) {
                        Text("Jugar de nuevo")
                    }
                }
            }
        }
    }
}


@Composable
fun TileComponent(
    tile: Tile,
    tileSize: Float,
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
            .size(tileSize.dp)
            .scale(scale.value)
            .alpha(alpha.value)
            .clickable(enabled = !tile.isBlocked && !isEliminating) { onClick() }
    ) {
        // fondo de carta
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // imagen de carta
            Image(
                painter = painterResource(id = tile.iconRes),
                contentDescription = "Tile Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }

        // si es ocultado
        if (tile.isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
            )
        }
    }
}