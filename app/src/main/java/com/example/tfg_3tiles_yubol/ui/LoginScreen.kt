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
    var email by remember { mutableStateOf(viewModel.getSavedEmail()) }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val authStatus by viewModel.loginStatus.collectAsState()

    fun validateFields(): Boolean {
        if (email.isBlank() && password.isBlank()) {
            localError = "El email y la contraseña son obligatorios"
            return false
        }
        if (email.isBlank()) {
            localError = "El email es obligatorio"
            return false
        }
        if (password.isBlank()) {
            localError = "La contraseña es obligatoria"
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            localError = "Introduce un email válido"
            return false
        }
        if (password.length < 6) {
            localError = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        localError = null
        return true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Océano Match", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Email") },
            enabled = authStatus != AuthStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localError = null },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            enabled = authStatus != AuthStatus.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authStatus == AuthStatus.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (validateFields()) {
                        viewModel.registerUser(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    if (validateFields()) {
                        viewModel.signIn(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar Sesión")
            }
        }

        val errorMessage = localError ?: (authStatus as? AuthStatus.Error)?.message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red
            )
        }
    }
}