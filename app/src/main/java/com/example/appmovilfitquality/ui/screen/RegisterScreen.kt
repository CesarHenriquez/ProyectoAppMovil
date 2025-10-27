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
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisteredSuccess: () -> Unit,
    onGoLogin: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: AuthViewModel
) {
    val gradientBrush = Brush.linearGradient(colors = listOf(Color.Black, GreenEnergy))

    val validationErrors by viewModel.registerValidationState.collectAsState()
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Puerta de carga para registro exitoso
    var showGate by remember { mutableStateOf(false) }

    // Al registrarse OK, muestra overlay 3s y luego va al login
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            showGate = true
            delay(3000)
            onRegisteredSuccess()
            viewModel.consumeRegistrationSuccess()
            showGate = false
        }
    }

    fun submitRegister() {
        if (isSubmitting || showGate) return
        isSubmitting = true
        viewModel.validateAndRegister(
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            password = password,
            confirm = confirm
        )
        isSubmitting = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta", color = Color.White) },
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
                        "Regístrate en FitQuality",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GreenEnergy
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        isError = validationErrors.nameError != null,
                        supportingText = { validationErrors.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        isError = validationErrors.emailError != null,
                        supportingText = { validationErrors.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        isError = validationErrors.phoneError != null,
                        supportingText = { validationErrors.phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        isError = validationErrors.passwordError != null,
                        supportingText = { validationErrors.passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        isError = validationErrors.confirmError != null,
                        supportingText = { validationErrors.confirmError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    )

                    validationErrors.generalError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { submitRegister() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenEnergy,
                            contentColor = Color.White
                        )
                    ) { Text(if (isSubmitting || showGate) "Procesando…" else "Registrar") }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoLogin,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(isSubmitting || showGate)
                    ) { Text("Ya tienes cuenta? Inicia sesión") }
                }
            }

            // Overlay bloqueante elegante
            if (showGate) {
                BlockingLoadingOverlay("Creando tu cuenta…")
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