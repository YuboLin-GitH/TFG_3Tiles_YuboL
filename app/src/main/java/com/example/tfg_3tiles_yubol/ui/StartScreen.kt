package com.example.tfg_3tiles_yubol.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_3tiles_yubol.R

@Composable
fun StartScreen(onPlayClick: () -> Unit, onExitClick: () -> Unit) {

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo a pantalla completa
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo Marino",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido principal
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cangrejo),
                contentDescription = "Mascota",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            //  título
            Text(
                text = "Océano Match",
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0xFF003366), // 深海蓝色阴影
                        offset = Offset(6f, 6f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(70.dp))

            // Botón de inicio
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .width(220.dp)
                    .height(65.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7F50) // Coral Orange
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Text(
                    text = "JUGAR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de salida
            Button(
                onClick = onExitClick,
                modifier = Modifier
                    .width(220.dp)
                    .height(65.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00509E) // Deep Sea Blue
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "SALIR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}