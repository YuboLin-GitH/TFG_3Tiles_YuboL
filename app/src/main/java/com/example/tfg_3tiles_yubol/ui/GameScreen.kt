package com.example.tfg_3tiles_yubol.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel
import com.example.tfg_3tiles_yubol.R
import com.example.tfg_3tiles_yubol.data.local.LevelData
import com.example.tfg_3tiles_yubol.data.model.Tile

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
// prueba
    val state by viewModel.gameState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadLevel(LevelData.getLevel1())
    }




    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        state.tiles.sortedBy { it.z }.forEach { tile ->
            TileComponent(
                tile = tile,
                onClick = { viewModel.onTileClick(tile) }
            )
        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //  boton deshacer y mezclar
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

            // 2. parte Tray
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tray_carta),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.trayTiles.forEach { tile ->
                        TileComponent(
                            tile = tile.copy(x = 0f, y = 0f),
                            onClick = {}
                        )
                    }
                }
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
                    //  solo mostrar si hay siguiente nivel
                    if (state.currentLevel == 1) {
                        Button(onClick = { viewModel.goToNextLevel() }) {
                            Text("Siguiente nivel →")
                        }
                    }
                    Button(onClick = { viewModel.resetGame() }) {
                        Text("Jugar de nuevo")
                    }
                }
            }
        }
    }
}


@Composable
fun TileComponent(tile: Tile, onClick: () -> Unit) {
    // posicion
    Box(
        modifier = Modifier
            .offset(x = tile.x.dp, y = tile.y.dp)
            .size(60.dp) // tamaño
            .clickable(enabled = !tile.isBlocked) { onClick() }
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