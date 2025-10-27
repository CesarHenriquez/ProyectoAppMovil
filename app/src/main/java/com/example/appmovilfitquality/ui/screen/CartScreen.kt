package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmovilfitquality.domain.model.CartItem
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.viewmodel.CartViewModel
import com.example.appmovilfitquality.viewmodel.CheckoutUiState
import com.example.appmovilfitquality.viewmodel.CheckoutViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    cartViewModel: CartViewModel,
    checkoutViewModel: CheckoutViewModel
) {
    val gradientBrush = Brush.linearGradient(colors = listOf(Color.Black, GreenEnergy))

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartTotal by cartViewModel.cartTotal.collectAsState()
    val checkoutState by checkoutViewModel.ui.collectAsState()

    var showAddressDialog by remember { mutableStateOf(false) }
    var shippingAddress by remember { mutableStateOf("") }

    // Si el pedido fue exitoso, limpiar carrito
    LaunchedEffect(checkoutState.success) {
        if (checkoutState.success) {
            cartViewModel.clearCart()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = GreenEnergy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            CartBottomBar(
                cartTotal = cartTotal,
                currencyFormatter = currencyFormatter,
                onCheckout = {
                    checkoutViewModel.reset()
                    showAddressDialog = true
                },
                isEnabled = cartTotal > 0.0
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(Modifier.fillMaxSize()) {
                if (cartItems.isEmpty()) {
                    EmptyCartMessage(Modifier.weight(1f))
                } else {
                    CartItemList(
                        items = cartItems,
                        viewModel = cartViewModel,
                        currencyFormatter = currencyFormatter,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Mensajes de checkout
                when {
                    checkoutState.isPlacing -> {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = GreenEnergy
                        )
                        Text(
                            "Procesando compra...",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    checkoutState.error != null -> {
                        Text(
                            checkoutState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    checkoutState.success -> {
                        Text(
                            "¡Compra realizada con éxito!",
                            color = GreenEnergy,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAddressDialog) {
        AddressDialog(
            address = shippingAddress,
            onAddressChange = { shippingAddress = it },
            onDismiss = { showAddressDialog = false },
            onConfirm = {
                checkoutViewModel.placeOrderFromCart(cartItems, shippingAddress)
                showAddressDialog = false
            },
            isPlacing = checkoutState.isPlacing
        )
    }
}

/* ---------------- Lista de ítems ---------------- */

@Composable
private fun CartItemList(
    items: List<CartItem>,
    viewModel: CartViewModel,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.product.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenEnergy
                    )
                    Text(
                        text = "Precio Unitario: ${currencyFormatter.format(item.product.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("x${item.quantity}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        text = currencyFormatter.format(item.subtotal),
                        color = GreenEnergy,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.removeFromCart(item.product.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Divider(color = Color.DarkGray)
        }
    }
}

/* ---------------- Barra inferior ---------------- */

@Composable
private fun CartBottomBar(
    cartTotal: Double,
    currencyFormatter: NumberFormat,
    onCheckout: () -> Unit,
    isEnabled: Boolean
) {
    Surface(shadowElevation = 8.dp, color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total:",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = currencyFormatter.format(cartTotal),
                    style = MaterialTheme.typography.headlineSmall,
                    color = GreenEnergy
                )
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenEnergy,
                    contentColor = Color.White
                )
            ) {
                Text("Proceder al Pago")
            }
        }
    }
}

/* ---------------- Diálogo dirección ---------------- */

@Composable
private fun AddressDialog(
    address: String,
    onAddressChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isPlacing: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isPlacing && address.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = GreenEnergy, contentColor = Color.White)) {
                Text("Confirmar compra")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isPlacing) { Text("Cancelar") }
        },
        title = { Text("Dirección de envío") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Ingresa tu dirección completa. El nombre, email y teléfono se tomarán de tu cuenta.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2
                )
            }
        }
    )
}

/* ---------------- Vacío ---------------- */

@Composable
private fun EmptyCartMessage(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Tu carrito está vacío. ¡Añade productos!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}