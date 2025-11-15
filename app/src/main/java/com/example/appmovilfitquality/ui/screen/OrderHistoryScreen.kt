package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.domain.model.Order
import com.example.appmovilfitquality.domain.model.OrderItem
import com.example.appmovilfitquality.ui.components.GradientBackground
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.viewmodel.HistoryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel,
    isAdminView: Boolean // true para Historial de Ventas (Admin), false para Compras (Cliente)
) {
    val uiState by viewModel.uiState.collectAsState()
    val title = if (isAdminView) "Historial de Ventas" else "Historial de Compras"

    LaunchedEffect(Unit) {
        viewModel.loadHistory(isAdminView)
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = GreenEnergy)
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    uiState.error != null -> Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                    uiState.orders.isEmpty() -> Text("No hay historial para mostrar.", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    else -> OrderList(uiState.orders, isAdminView)
                }
            }
        }
    }
}

@Composable
private fun OrderList(orders: List<Order>, isAdminView: Boolean) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(orders, key = { it.id }) { order ->
            OrderCard(order, isAdminView)
        }
    }
}

@Composable
private fun OrderCard(order: Order, isAdminView: Boolean) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Fila de Encabezado: Fecha y Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(order.timestamp)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenEnergy
                )
                Text(
                    text = formatter.format(order.totalCLP),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))


            // Detalles específicos para Admin (Ventas)
            if (isAdminView) {
                Text(
                    "Cliente: ${order.customerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Dirección: ${order.shippingAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "Entregado: ${if (order.delivered) "Sí" else "No"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (order.delivered) GreenEnergy else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(10.dp))
            } else {
                // Detalles específicos para Cliente (Compras)
                Text(
                    "Dirección de envío: ${order.shippingAddress}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Estado: ${if (order.delivered) "Entregado" else "Pendiente"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (order.delivered) GreenEnergy else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(10.dp))
            }

            // Listado de Ítems
            Text("Productos:", fontWeight = FontWeight.SemiBold)
            order.items.forEach { item ->
                OrderItemRow(item, formatter)
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, formatter: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = " • ${item.productName} (x${item.quantity})",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatter.format(item.subtotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}