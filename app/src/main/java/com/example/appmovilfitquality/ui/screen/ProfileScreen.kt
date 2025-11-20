package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    var name by remember(uiState.name) { mutableStateOf(uiState.name) }
    var phone by remember(uiState.phone) { mutableStateOf(uiState.phone) }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mi Perfil", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = GreenEnergy)
                        }
                    },
                    actions = {
                        Button(onClick = { isEditing = !isEditing }) {
                            Text(if (isEditing) "Cancelar" else "Editar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = GreenEnergy)
                    return@Box
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(if (isEditing) 1f else 0.8f) // Más estrecho en modo solo lectura
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Datos de la Cuenta", style = MaterialTheme.typography.headlineSmall, color = GreenEnergy, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))

                    // --- Sección de Visualización / Edición ---
                    ProfileField(label = "Nombre", value = name, isEditing = isEditing, onValueChange = { name = it }, error = uiState.error)
                    ProfileField(label = "Email", value = uiState.email, isEditing = false, onValueChange = {}) // Email no editable
                    ProfileField(label = "Teléfono", value = phone, isEditing = isEditing, onValueChange = { phone = it }, error = uiState.error)
                    ProfileField(label = "Rol", value = uiState.role, isEditing = false, onValueChange = {})

                    if (isEditing) {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.updateProfile(name, phone)
                                isEditing = false // Cierra la edición al intentar guardar
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenEnergy)
                        ) { Text("Guardar Cambios") }
                    }

                    Spacer(Modifier.height(32.dp))

                    // --- Sección de Credenciales ---
                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B01D))
                    ) { Text("Cambiar Contraseña") }

                    Spacer(Modifier.height(16.dp))

                    // Mostrar mensajes de UIState
                    uiState.message?.let {
                        Text(it, color = GreenEnergy, style = MaterialTheme.typography.bodySmall)
                    }
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            viewModel = viewModel,
            onDismiss = { showPasswordDialog = false }
        )
    }
}

// Componente para manejar la visualización/edición de un campo
@Composable
private fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = GreenEnergy)
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null
            )
        } else {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Divider(color = Color.DarkGray)
        }
    }
}

// Diálogo para el cambio de contraseña
@Composable
private fun ChangePasswordDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val validationState by viewModel.passwordValidationState.collectAsState()
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Cierra el diálogo si el cambio fue exitoso
    LaunchedEffect(validationState.success) {
        if (validationState.success) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambio de Contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Contraseña Actual
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña actual") },
                    isError = validationState.currentPasswordError != null,
                    supportingText = { validationState.currentPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation()
                )

                // Nueva Contraseña
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    isError = validationState.newPasswordError != null,
                    supportingText = { validationState.newPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation()
                )

                // Confirmar Nueva Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    isError = validationState.confirmPasswordError != null,
                    supportingText = { validationState.confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = PasswordVisualTransformation()
                )

                validationState.generalError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                    }
                },
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) { Text("Guardar Clave") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}