package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.data.local.OrderEntity
import com.example.appmovilfitquality.ui.components.CameraCaptureRow
import com.example.appmovilfitquality.ui.components.DeleteFromGalleryButton
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.viewmodel.DeliveryViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable private fun Loading() { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { CircularProgressIndicator(color = Color.White); Spacer(Modifier.height(12.dp)); Text("Cargando pedidos…", color = Color.White) } }
@Composable private fun Error(msg: String) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("ERROR: $msg", color = MaterialTheme.colorScheme.error) } }
@Composable private fun Empty() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay pedidos asignados.", color = Color.White) } }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    onLogout: () -> Unit,
    viewModel: DeliveryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProofDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastMessage) {
        uiState.lastMessage?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) { viewModel.refresh() }

    GradientBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Transportista (Delivery)", color = Color.White) },
                    actions = { Button(onClick = onLogout) { Text("Logout") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                when {
                    uiState.isLoading -> Loading()
                    uiState.error != null -> Error(uiState.error!!)
                    uiState.orders.isEmpty() -> Empty()
                    else -> {
                        DeliveryList(
                            orders = uiState.orders,
                            onUploadProof = { order ->
                                selectedOrder = order
                                showProofDialog = true
                            }
                        )
                    }
                }

                if (showProofDialog && selectedOrder != null) {
                    ProofDialog(
                        orderId = selectedOrder!!.id,
                        onDismiss = { showProofDialog = false },
                        onSubmit = { uri ->
                            showProofDialog = false
                            uri?.let { viewModel.submitProof(selectedOrder!!.id, it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryList(
    orders: List<OrderEntity>,
    onUploadProof: (OrderEntity) -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(orders, key = { it.id }) { order ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {

                    Text("Orden #${order.id}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(order.customerName, style = MaterialTheme.typography.bodyMedium)
                    Text("Email: ${order.customerEmail}", style = MaterialTheme.typography.bodySmall)
                    Text("Teléfono: ${order.customerPhone}", style = MaterialTheme.typography.bodySmall)
                    Text("Dirección: ${order.shippingAddress}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))

                    Text("Total: ${formatter.format(order.totalCLP)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))

                    if (order.proofImageUri != null || order.delivered) {
                        Text("Comprobante cargado", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { onUploadProof(order) }, modifier = Modifier.align(Alignment.End)) {
                            Text("Reemplazar comprobante")
                        }
                    } else {
                        Button(onClick = { onUploadProof(order) }, modifier = Modifier.align(Alignment.End)) {
                            Text("Subir comprobante ")
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------- Diálogo subir comprobante ---------------------- */

@Composable
fun ProofDialog(
    orderId: Int,
    onDismiss: () -> Unit,
    onSubmit: (imageUri: String?) -> Unit
) {
    var imageUri by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSubmit(imageUri) }, enabled = imageUri != null) {
                Text("Enviar comprobante")
            }
        },
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
        title = { Text("Subir comprobante de entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Toma una foto como evidencia de entrega.")

                CameraCaptureRow { uriStr -> imageUri = uriStr }
                Text(if (imageUri != null) "Imagen seleccionada     " else "Sin imagen aún")
            }
        }
    )
}