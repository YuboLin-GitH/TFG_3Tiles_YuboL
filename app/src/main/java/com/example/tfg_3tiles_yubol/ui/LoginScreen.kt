package com.example.tfg_3tiles_yubol.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_3tiles_yubol.viewModel.AuthStatus
import com.example.tfg_3tiles_yubol.viewModel.GameViewModel

@Composable
fun LoginScreen(viewModel: GameViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authStatus by viewModel.loginStatus.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Océano Match", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = authStatus != AuthStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = authStatus != AuthStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authStatus == AuthStatus.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.registrarUsuario(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.iniciarSesion(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar Sesión")
            }
        }

        if (authStatus is AuthStatus.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authStatus as AuthStatus.Error).message,
                color = Color.Red
            )
        }
    }
}