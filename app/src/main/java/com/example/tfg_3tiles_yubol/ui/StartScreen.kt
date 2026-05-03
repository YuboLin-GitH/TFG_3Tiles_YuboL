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
        // 1. 全屏背景图
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo Marino",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. 主体内容（居中排列）
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🌟 装饰：放一个游戏里的螃蟹图标在标题上方点缀，增加可爱度
            Image(
                painter = painterResource(id = R.drawable.cangrejo),
                contentDescription = "Mascota",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // 🌟 标题：大字号 + 粗体 + 阴影（增加立体感，让白字在背景上清晰可见）
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

            // 🌟 开始按钮：珊瑚橙色，大圆角，带阴影高度（Elevation）
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .width(220.dp)
                    .height(65.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7F50) // Coral Orange 珊瑚橙
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

            // 🌟 退出按钮：深海蓝色，和开始按钮形成主次对比
            Button(
                onClick = onExitClick,
                modifier = Modifier
                    .width(220.dp)
                    .height(65.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00509E) // Deep Sea Blue 海蓝色
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