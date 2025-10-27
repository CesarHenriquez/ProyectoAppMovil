package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import coil.compose.AsyncImage
import com.example.appmovilfitquality.data.local.ProductEntity
import com.example.appmovilfitquality.ui.components.DeleteFromGalleryButton
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.viewmodel.CartViewModel
import com.example.appmovilfitquality.viewmodel.StoreViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    onGoToCart: () -> Unit,
    onGoToSupport: () -> Unit,
    viewModel: StoreViewModel,
    cartViewModel: CartViewModel,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FitQuality Store") },
                    actions = {
                        // Botón Soporte que abre el chat
                        TextButton(onClick = onGoToSupport) { Text("Soporte") }
                        IconButton(onClick = onGoToCart) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Ver carrito")
                        }
                        TextButton(onClick = onLogout) { Text("Logout") }
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
                    .padding(16.dp)
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
                        items(uiState.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onAddToCart = { cartViewModel.addToCart(product) },
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
    product: ProductEntity,
    onAddToCart: () -> Unit,
    onReview: () -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }


    val imageResId = remember(product.imageResourceName, product.imageUri) {
        if (product.imageUri.isNullOrBlank()) {
            product.imageResourceName?.let {
                context.resources.getIdentifier(it, "drawable", context.packageName)
            } ?: 0
        } else 0
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            // Foto real (URI) si existe
            if (!product.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))

                //  Si no hay URI, intenta drawable por nombre
            } else if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))

                // Placeholder si no hay nada
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
            Text(formatter.format(product.price), color = MaterialTheme.colorScheme.primary)

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddToCart) { Text("Agregar al carrito") }
                OutlinedButton(onClick = onReview) { Text("Reseñar") }
            }
        }
    }
}

/* ----------------------------- Diálogo reseña ----------------------------- */

@Composable
fun ReviewDialog(
    productId: Int,
    onDismiss: () -> Unit,
    onSubmit: (imageUri: String?, comment: String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

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

                    com.example.appmovilfitquality.ui.components.CameraCaptureRow { uriStr -> imageUri = uriStr }
                }
                Text(if (imageUri != null) "Imagen seleccionada " else "Sin imagen")
            }
        }
    )
}