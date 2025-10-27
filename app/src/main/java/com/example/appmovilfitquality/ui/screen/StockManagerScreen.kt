package com.example.appmovilfitquality.ui.screen

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.data.local.ProductEntity
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.viewmodel.StoreViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerScreen(
    onLogout: () -> Unit,
    viewModel: StoreViewModel,
    onGoToSupport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de Stock (Admin)") },
                    actions = {
                        TextButton(onClick = onGoToSupport) { Text("Soporte") }
                        Button(onClick = onLogout) { Text("Cerrar Sesión") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editingProduct = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    "Inventario actual",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))

                if (uiState.products.isEmpty()) {
                    Text("No hay productos registrados.", color = Color.White)
                } else {
                    ProductListAdmin(
                        products = uiState.products,
                        onEdit = {
                            editingProduct = it
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteProduct(it) }
                    )
                }
            }

            if (showDialog) {
                ProductDialog(
                    product = editingProduct,
                    onDismiss = { showDialog = false },
                    onSave = { product ->
                        if (editingProduct == null) viewModel.addProduct(product)
                        else viewModel.updateProduct(product.copy(id = editingProduct!!.id))
                        showDialog = false
                    }
                )
            }
        }
    }
}

/* ---------------- Lista ---------------- */

@Composable
private fun ProductListAdmin(
    products: List<ProductEntity>,
    onEdit: (ProductEntity) -> Unit,
    onDelete: (ProductEntity) -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.description, style = MaterialTheme.typography.bodySmall)
                        Text(formatter.format(product.price), color = MaterialTheme.colorScheme.primary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onEdit(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { onDelete(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }
}

/* ------------- Diálogo producto ------------- */

@Composable
private fun ProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imageResName by remember { mutableStateOf(product?.imageResourceName ?: "") }
    var imageUri by remember { mutableStateOf(product?.imageUri) }

    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) imageUri = pendingCameraUri?.toString() }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it.toString() } }

    fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "fitq_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                val newProduct = ProductEntity(
                    id = product?.id ?: 0,
                    name = name.trim(),
                    description = description.trim(),
                    price = parsedPrice,
                    imageResourceName = imageResName.ifBlank { null },
                    imageUri = imageUri
                )
                onSave(newProduct)
            }) { Text(if (product == null) "Agregar" else "Guardar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(if (product == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio (CLP)") })
                OutlinedTextField(
                    value = imageResName,
                    onValueChange = { imageResName = it },
                    label = { Text("Nombre drawable (ej: munequeras_fit)") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val uri = createImageUri()
                        pendingCameraUri = uri
                        if (uri != null) takePictureLauncher.launch(uri)
                    }) { Text("Tomar foto") }

                    OutlinedButton(onClick = { pickImageLauncher.launch("image/*") }) {
                        Text("Galería")
                    }
                }
                Text(
                    when {
                        imageUri != null -> "Foto seleccionada"
                        imageResName.isNotBlank() -> "Usando drawable: $imageResName"
                        else -> "Sin imagen"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}