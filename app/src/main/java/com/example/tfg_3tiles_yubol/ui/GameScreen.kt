package com.example.tfg_3tiles_yubol.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel
import com.example.tfg_3tiles_yubol.R
import com.example.tfg_3tiles_yubol.data.model.Tile

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
// prueba
    val state by viewModel.gameState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadTiles(listOf(
            Tile(id = 1, type = 1, x = 80f,  y = 200f, z = 1, iconRes = R.drawable.smiling),
            Tile(id = 2, type = 1, x = 160f, y = 200f, z = 1, iconRes = R.drawable.smiling),
            Tile(id = 3, type = 2, x = 120f, y = 320f, z = 2, iconRes = R.drawable.crab),
            //para pureba superpuestas
            Tile(id = 4, type = 2, x = 60f, y = 200f, z = 2, iconRes = R.drawable.crab),
        ))
    }





    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        state.tiles.forEach { tile ->
            TileComponent(
                tile = tile,
                onClick = { viewModel.onTileClick(tile) }
            )
        }


        // parte tray
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.tray_carta),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            //
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
}


@Composable
fun TileComponent(tile: Tile, onClick: () -> Unit) {
    // posicion
    Box(
        modifier = Modifier
            .offset(x = tile.x.dp, y = tile.y.dp)
            .size(64.dp) // tamaño
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