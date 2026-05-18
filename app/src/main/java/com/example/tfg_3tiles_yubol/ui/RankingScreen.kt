package com.example.tfg_3tiles_yubol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel

@Composable
fun RankingScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val rankings by viewModel.rankings.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Fácil", "Normal", "Difícil")

    LaunchedEffect(Unit) {
        viewModel.fetchRankings()
    }

    val filteredRankings = if (selectedFilter == "Todos") {
        rankings
    } else {
        rankings.filter { it.difficulty == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1F3F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ranking",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Yellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Filtros de dificultad ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Text(
                    text = filter,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Yellow else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { selectedFilter = filter }
                        .background(
                            if (isSelected) Color.Yellow.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.width(22.dp))
            Text("Jugador", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("Dif.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.Center)
            Text("Tiempo", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
            Text("Pts", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
        }

        HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.2f))

        if (filteredRankings.isEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("No hay puntuaciones aún", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(filteredRankings) { index, record ->
                RankingRow(
                    position = index + 1,
                    email = record.email,
                    difficulty = record.difficulty,
                    timeLeft = record.time_left,
                    score = record.score
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth(0.5f)) {
            Text("Volver")
        }
    }
}

private fun difficultyColor(label: String): Color = when (label) {
    "Fácil" -> Color(0xFF4CAF50)
    "Normal" -> Color(0xFFFF9800)
    "Difícil" -> Color(0xFFF44336)
    else -> Color.Gray
}

@Composable
private fun RankingRow(position: Int, email: String, difficulty: String, timeLeft: Int, score: Int) {
    val displayEmail = email.substringBefore("@")
    val isTop3 = position <= 3
    val bgColor = when (position) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.15f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.05f)
    }
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeText = "%d:%02d".format(minutes, seconds)
    val diffColor = difficultyColor(difficulty)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$position",
            color = if (isTop3) Color.Yellow else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp)
        )
        Text(
            text = displayEmail,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = difficulty.take(3),
            color = diffColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = timeText,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = "$score",
            color = Color.Yellow,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(48.dp)
        )
    }
}
