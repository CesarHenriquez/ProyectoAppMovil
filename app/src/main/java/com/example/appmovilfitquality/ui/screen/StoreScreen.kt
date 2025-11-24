package com.example.appmovilfitquality.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.appmovilfitquality.domain.model.Product

import com.example.appmovilfitquality.ui.components.CameraCaptureRow
import com.example.appmovilfitquality.ui.components.DeleteFromGalleryButton
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.viewmodel.CartViewModel
import com.example.appmovilfitquality.viewmodel.StoreViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    onGoToCart: () -> Unit,
    onGoToSupport: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToProfile: () -> Unit,
    viewModel: StoreViewModel,
    cartViewModel: CartViewModel,
    onLogout: () -> Unit = {}
) {
    // uiState.products es List<Product> (Dominio)
    val uiState by viewModel.uiState.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FitQuality Store", color = Color.White) },
                    actions = {
                        // Botón de Perfil
                        IconButton(onClick = onGoToProfile) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil", tint = Color.White)
                        }
                        TextButton(onClick = onGoToHistory) { Text("Historial", color = Color.White) }
                        TextButton(onClick = onGoToSupport) { Text("Soporte", color = Color.White) }
                        IconButton(onClick = onGoToCart) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Ver carrito", tint = Color.White)
                        }
                        TextButton(onClick = onLogout) { Text("Logout", color = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (uiState.products.isEmpty()) {
                    Text("No hay productos disponibles.", Modifier.align(Alignment.Center), color = Color.White)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        val validProducts = uiState.products.filter { it.id > 0 }

                        items(validProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onAddToCart = { p ->
                                    scope.launch {
                                        val result = cartViewModel.tryAddToCart(p)
                                        if (!result.success) {
                                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "${p.name} añadido al carrito.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReview = {
                                    selectedProduct = product
                                    showReviewDialog = true
                                }
                            )
                        }
                    }
                }

                if (showReviewDialog && selectedProduct != null) {
                    ReviewDialog(
                        productId = selectedProduct!!.id,
                        onDismiss = { showReviewDialog = false },
                        onSubmit = { _imageUri, _comment ->
                            showReviewDialog = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ProductCard(
    product: Product,
    onAddToCart: (Product) -> Unit,
    onReview: () -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }



    val localResourceName = product.imageUri?.substringBeforeLast(".")

    val localImageResId = remember(localResourceName) {
        if (!localResourceName.isNullOrBlank()) {

            context.resources.getIdentifier(localResourceName, "drawable", context.packageName)
        } else {
            0
        }
    }


    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            val remoteImageUrl = product.imageUri

            if (localImageResId != 0) {
                Image(
                    painter = painterResource(id = localImageResId),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin imagen", color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(12.dp))
            }


            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(product.description, style = MaterialTheme.typography.bodySmall)

            Text("Stock: ${product.stock} unidades", style = MaterialTheme.typography.labelMedium, color = if (product.stock > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)

            Text(formatter.format(product.price), color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAddToCart(product) }, enabled = product.stock > 0) {
                    Text(if (product.stock > 0) "Agregar al carrito" else "Agotado")
                }
                OutlinedButton(onClick = onReview) { Text("Reseñar") }
            }
        }
    }
}

// ... (El diálogo ReviewDialog se mantiene igual) ...

@Composable
fun ReviewDialog(
    productId: Int,
    onDismiss: () -> Unit,
    onSubmit: (imageUri: String?, comment: String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    // ... (resto del cuerpo sin cambios)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSubmit(imageUri, comment) }) { Text("Publicar") } },
        dismissButton = {
            Row {
                if (imageUri != null) {
                    DeleteFromGalleryButton(
                        imageUriString = imageUri!!,
                        onDeleted = { imageUri = null }
                    )
                    Spacer(Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
        title = { Text("Tu reseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comentario") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    CameraCaptureRow { uriStr -> imageUri = uriStr }
                }
                Text(if (imageUri != null) "Imagen seleccionada " else "Sin imagen")
            }
        }
    )
}