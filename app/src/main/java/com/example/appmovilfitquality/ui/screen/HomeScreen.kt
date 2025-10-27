package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
) {
    val gradientOverlay = Brush.verticalGradient(
        colors = listOf(Color.Black.copy(alpha = 0.6f), GreenEnergy.copy(alpha = 0.6f))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FitQuality", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //  Imagen de fondo borrosa
            Image(
                painter = painterResource(id = R.drawable.fondo_fitquality), // cambia el nombre a tu imagen
                contentDescription = "Fondo FitQuality",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(1.dp), //  Efecto de desenfoque suave
                contentScale = ContentScale.Crop
            )

            // Capa de gradiente semitransparente para contraste
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientOverlay)
            )

            // Tarjeta de bienvenida
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bienvenido a nuestra tienda",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenEnergy
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onGoLogin,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenEnergy,
                            contentColor = Color.White
                        )
                    ) { Text("Iniciar Sesión") }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Crear Cuenta") }

                    Spacer(Modifier.height(24.dp))


                }
            }
        }
    }
}