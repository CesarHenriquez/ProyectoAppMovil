package com.example.appmovilfitquality.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.viewmodel.AuthViewModel
import com.example.appmovilfitquality.viewmodel.LoginResult
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: AuthViewModel
) {
    val gradientBrush = Brush.linearGradient(colors = listOf(Color.Black, GreenEnergy))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Puerta de carga antes de navegar (3s)
    var showGate by remember { mutableStateOf(false) }

    // Cuando showGate es true, espera 3s y navega
    LaunchedEffect(showGate) {
        if (showGate) {
            delay(3000)
            onLoginSuccess()
            showGate = false
        }
    }

    fun submitLogin() {
        if (isSubmitting || showGate) return
        isSubmitting = true
        errorMsg = null
        viewModel.login(email.trim(), password) { result ->
            when (result) {
                is LoginResult.Success -> {
                    // Muestra overlay, la navegación ocurre tras 3s
                    showGate = true
                }
                is LoginResult.UserNotFound -> errorMsg = "El usuario no existe."
                is LoginResult.WrongPassword -> errorMsg = "Contraseña incorrecta."
            }
            isSubmitting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onGoHome) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = GreenEnergy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Bienvenido a FitQuality",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GreenEnergy
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )

                    Spacer(Modifier.height(16.dp))
                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { submitLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenEnergy,
                            contentColor = Color.White
                        )
                    ) { Text(if (isSubmitting || showGate) "Procesando…" else "Iniciar Sesión") }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoRegister,
                        enabled = !(isSubmitting || showGate),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Crear nueva cuenta") }
                }
            }

            // Overlay bloqueante elegante
            if (showGate) {
                BlockingLoadingOverlay("Entrando a tu cuenta…")
            }
        }
    }
}

/* ---------- Overlay reutilizable: fade + spinner + mensaje ---------- */
@Composable
private fun BlockingLoadingOverlay(message: String) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = GreenEnergy)
                Spacer(Modifier.height(12.dp))
                Text(message, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}