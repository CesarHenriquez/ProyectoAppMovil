package com.example.appmovilfitquality.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appmovilfitquality.data.local.AppDataBase
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.DeliveryRepository
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.ui.screen.AppHostScreen
import com.example.appmovilfitquality.ui.screen.CartScreen
import com.example.appmovilfitquality.ui.screen.ChatSupportScreen
import com.example.appmovilfitquality.ui.screen.DeliveryScreen
import com.example.appmovilfitquality.ui.screen.HomeScreen
import com.example.appmovilfitquality.ui.screen.LoginScreen
import com.example.appmovilfitquality.ui.screen.RegisterScreen
import com.example.appmovilfitquality.ui.screen.StockChatListScreen
import com.example.appmovilfitquality.ui.screen.StockChatScreen
import com.example.appmovilfitquality.ui.screen.StockManagerScreen
import com.example.appmovilfitquality.ui.screen.StoreScreen
import com.example.appmovilfitquality.viewmodel.AuthViewModel
import com.example.appmovilfitquality.viewmodel.CartViewModel
import com.example.appmovilfitquality.viewmodel.ChatViewModel
import com.example.appmovilfitquality.viewmodel.CheckoutViewModel
import com.example.appmovilfitquality.viewmodel.DeliveryViewModel
import com.example.appmovilfitquality.viewmodel.StoreViewModel

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val APP_HOST = "app_host"
    const val CART = "cart"

    const val STORE_CLIENT_HOME = "client_store_home"
    const val STOCK_MANAGER_HOME = "stock_home"
    const val DELIVERY_HOME = "delivery_home"

    // Chat
    const val CLIENT_SUPPORT = "client_support"
    const val STOCK_CHAT_LIST = "stock_chat_list"
    const val STOCK_CHAT = "stock_chat" // requiere arg ?peerEmail=
}
@Composable
fun NavGraph(navController: NavHostController) {

    val context = LocalContext.current

    // Infra compartida
    val appDatabase = remember { AppDataBase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }

    val authRepository = remember { AuthRepository(appDatabase.userDao()) }
    val productRepository = remember { ProductRepository(appDatabase.productDao()) }
    val deliveryRepository = remember { DeliveryRepository(appDatabase.orderDao()) }

    // ViewModels con Factory
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown AuthViewModel class")
        }
    })

    val storeViewModel: StoreViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(productRepository) as T
            }
            throw IllegalArgumentException("Unknown StoreViewModel class")
        }
    })

    val deliveryViewModel: DeliveryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DeliveryViewModel(deliveryRepository) as T
            }
            throw IllegalArgumentException("Unknown DeliveryViewModel class")
        }
    })

    val checkoutViewModel: CheckoutViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CheckoutViewModel(deliveryRepository, authRepository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown CheckoutViewModel class")
        }
    })

    val cartViewModel: CartViewModel = viewModel()

    // Chat VM (usa contexto + session)
    val chatViewModel: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(context.applicationContext, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ChatViewModel class")
        }
    })

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {

        /* ------------------- WELCOME  ------------------- */
        composable("${Routes.WELCOME}?logout={logout}") { backStackEntry ->
            val logoutFlag = backStackEntry.arguments?.getString("logout") == "true"
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(logoutFlag) {
                if (logoutFlag) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Sesión cerrada correctamente",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                        navController.navigate(Routes.WELCOME) { popUpTo(0) }
                    }
                }
            }
            @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
            Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { _ ->
                HomeScreen(
                    onGoLogin = { navController.navigate(Routes.LOGIN) },
                    onGoRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
        }

        /* ------------------- LOGIN ------------------- */
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.APP_HOST) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoRegister = {
                    navController.navigate(Routes.REGISTER) { launchSingleTop = true }
                },
                onGoHome = { navController.navigate(Routes.WELCOME) { popUpTo(0) } },
                viewModel = authViewModel
            )
        }

        /* ------------------- REGISTER ------------------- */
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisteredSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onGoLogin = { navController.navigate(Routes.LOGIN) { launchSingleTop = true } },
                onGoHome = { navController.navigate(Routes.WELCOME) { popUpTo(0) } },
                viewModel = authViewModel
            )
        }

        /* ------------------- APP HOST ------------------- */
        composable(Routes.APP_HOST) {
            AppHostScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    cartViewModel.clearCart()
                    checkoutViewModel.reset()
                    navController.navigate("${Routes.WELCOME}?logout=true") { popUpTo(0) }
                },
                onGoToStore = { navController.navigate(Routes.STORE_CLIENT_HOME) },
                onGoToStock = { navController.navigate(Routes.STOCK_MANAGER_HOME) },
                onGoToDelivery = { navController.navigate(Routes.DELIVERY_HOME) }
            )
        }

        /* ------------------- CLIENTE ------------------- */
        composable(Routes.STORE_CLIENT_HOME) {
            StoreScreen(
                onGoToCart = { navController.navigate(Routes.CART) },
                viewModel = storeViewModel,
                cartViewModel = cartViewModel,
                onLogout = {
                    authViewModel.logout()
                    cartViewModel.clearCart()
                    checkoutViewModel.reset()
                    navController.navigate("${Routes.WELCOME}?logout=true") { popUpTo(0) }
                },
                // botón de soporte
                onGoToSupport = { navController.navigate(Routes.CLIENT_SUPPORT) }
            )
        }

        /* ------------------- STOCK (Admin) ------------------- */
        composable(Routes.STOCK_MANAGER_HOME) {
            StockManagerScreen(
                onLogout = {
                    authViewModel.logout()
                    cartViewModel.clearCart()
                    checkoutViewModel.reset()
                    navController.navigate("${Routes.WELCOME}?logout=true") { popUpTo(0) }
                },
                viewModel = storeViewModel,
                // NUEVO: lista de chats (clientes)
                onGoToSupport = { navController.navigate(Routes.STOCK_CHAT_LIST) }
            )
        }

        /* ------------------- DELIVERY ------------------- */
        composable(Routes.DELIVERY_HOME) {
            DeliveryScreen(
                onLogout = {
                    authViewModel.logout()
                    cartViewModel.clearCart()
                    checkoutViewModel.reset()
                    navController.navigate("${Routes.WELCOME}?logout=true") { popUpTo(0) }
                },
                viewModel = deliveryViewModel
            )
        }

        /* ------------------- CARRITO ------------------- */
        composable(Routes.CART) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                cartViewModel = cartViewModel,
                checkoutViewModel = checkoutViewModel
            )
        }

        /* ======================= CHAT ======================= */

        // Chat de soporte del CLIENTE (cliente ↔ stock)
        composable(Routes.CLIENT_SUPPORT) {
            LaunchedEffect(Unit) {
                chatViewModel.openConversation("admin@stock.com")
            }
            ChatSupportScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = chatViewModel
            )
        }

        // Lista de conversaciones para STOCK (admin)
        composable(Routes.STOCK_CHAT_LIST) {
            StockChatListScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { peerEmail ->
                    navController.navigate("${Routes.STOCK_CHAT}?peerEmail=$peerEmail")
                },
                viewModel = chatViewModel
            )
        }

        // Chat específico para STOCK (admin) con un cliente
        composable(
            route = "${Routes.STOCK_CHAT}?peerEmail={peerEmail}",
            arguments = listOf(
                navArgument("peerEmail") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val peerEmail = backStackEntry.arguments?.getString("peerEmail").orEmpty()

            LaunchedEffect(peerEmail) {
                if (peerEmail.isNotBlank()) {
                    chatViewModel.openConversation(peerEmail)
                }
            }

            StockChatScreen(
                onNavigateBack = { navController.popBackStack() },
                peerEmail = peerEmail,
                viewModel = chatViewModel
            )
        }
    }
}