package com.example.appmovilfitquality.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator // ⬇️ Agregado para el spinner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState // ⬇️ Agregado para leer flows
import androidx.compose.runtime.getValue // ⬇️ Agregado para leer flows
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appmovilfitquality.data.localstore.SessionManager
import com.example.appmovilfitquality.data.remote.ApiService
import com.example.appmovilfitquality.data.remote.RetrofitClient
import com.example.appmovilfitquality.data.repository.AuthRepository
import com.example.appmovilfitquality.data.repository.ChatRepository
import com.example.appmovilfitquality.data.repository.OrderRepository
import com.example.appmovilfitquality.data.repository.ProductRepository
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.ui.screen.AppHostScreen
import com.example.appmovilfitquality.ui.screen.CartScreen
import com.example.appmovilfitquality.ui.screen.ChatSupportScreen
import com.example.appmovilfitquality.ui.screen.DeliveryScreen
import com.example.appmovilfitquality.ui.screen.HomeScreen
import com.example.appmovilfitquality.ui.screen.LoginScreen
import com.example.appmovilfitquality.ui.screen.OrderHistoryScreen
import com.example.appmovilfitquality.ui.screen.ProfileScreen
import com.example.appmovilfitquality.ui.screen.RegisterScreen
import com.example.appmovilfitquality.ui.screen.StockChatListScreen
import com.example.appmovilfitquality.ui.screen.StockChatScreen
import com.example.appmovilfitquality.ui.screen.StockManagerScreen
import com.example.appmovilfitquality.ui.screen.StoreScreen
import com.example.appmovilfitquality.ui.theme.GreenEnergy
import com.example.appmovilfitquality.viewmodel.AuthViewModel
import com.example.appmovilfitquality.viewmodel.CartViewModel
import com.example.appmovilfitquality.viewmodel.ChatViewModel
import com.example.appmovilfitquality.viewmodel.CheckoutViewModel
import com.example.appmovilfitquality.viewmodel.DeliveryViewModel
import com.example.appmovilfitquality.viewmodel.HistoryViewModel
import com.example.appmovilfitquality.viewmodel.ProfileViewModel
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

    // Historial
    const val CLIENT_ORDER_HISTORY = "client_order_history"
    const val STOCK_SALES_HISTORY = "stock_sales_history"

    // Chat
    const val CLIENT_SUPPORT = "client_support"
    const val STOCK_CHAT_LIST = "stock_chat_list"
    const val STOCK_CHAT = "stock_chat"

    const val PROFILE = "profile"
}
@Composable
fun NavGraph(navController: NavHostController) {

    val context = LocalContext.current


    val sessionManager = remember { SessionManager(context) }


    val apiService: ApiService = remember {
        RetrofitClient.createApiService(sessionManager)
    }


    val authRepository = remember { AuthRepository(apiService, sessionManager) }
    val productRepository = remember { ProductRepository(apiService) }


    val orderRepository = remember { OrderRepository(apiService,  authRepository) }


    val chatRepository = remember { ChatRepository() } // Simulación local


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
                return DeliveryViewModel(orderRepository) as T
            }
            throw IllegalArgumentException("Unknown DeliveryViewModel class")
        }
    })

    val checkoutViewModel: CheckoutViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CheckoutViewModel(orderRepository, authRepository, sessionManager, productRepository) as T
            }
            throw IllegalArgumentException("Unknown CheckoutViewModel class")
        }
    })

    val cartViewModel: CartViewModel = viewModel()


    val historyViewModel: HistoryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(orderRepository, sessionManager, authRepository) as T
            }
            throw IllegalArgumentException("Unknown HistoryViewModel class")
        }
    })



    val chatViewModel: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(chatRepository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ChatViewModel class")
        }
    })


    val profileViewModel: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(authRepository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ProfileViewModel class")
        }
    })


    val isSessionLoaded by authViewModel.isSessionLoaded.collectAsState()
    val userRole by authViewModel.currentUserRole.collectAsState()


    if (!isSessionLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GreenEnergy)
        }
        return
    }


    val startRoute = if (userRole == UserRole.GUEST) Routes.WELCOME else Routes.APP_HOST


    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        /* ------------------- WELCOME / LOGIN / REGISTER / APP HOST  ------------------- */
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
                onGoToSupport = { navController.navigate(Routes.CLIENT_SUPPORT) },
                onGoToHistory = { navController.navigate(Routes.CLIENT_ORDER_HISTORY) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.CLIENT_ORDER_HISTORY) {
            OrderHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = historyViewModel,
                isAdminView = false
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

                onGoToSupport = { navController.navigate(Routes.STOCK_CHAT_LIST) },
                onGoToSalesHistory = { navController.navigate(Routes.STOCK_SALES_HISTORY) }
            )
        }

        composable(Routes.STOCK_SALES_HISTORY) {
            OrderHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = historyViewModel,
                isAdminView = true
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

        /* ------------------- CARRITO / CHAT / PERFIL ------------------- */
        composable(Routes.CART) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                cartViewModel = cartViewModel,
                checkoutViewModel = checkoutViewModel
            )
        }

        // CHAT PRIVADO (CLIENTE ↔ ADMIN)
        composable(Routes.CLIENT_SUPPORT) {
            LaunchedEffect(Unit) {
                chatViewModel.ensureSupportPeerForClient()
            }
            ChatSupportScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = chatViewModel
            )
        }

        // Lista de chats privados para STOCK (Admin)
        composable(Routes.STOCK_CHAT_LIST) {
            StockChatListScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenChat = { peerEmail ->
                    navController.navigate("${Routes.STOCK_CHAT}?peerEmail=$peerEmail")
                },
                viewModel = chatViewModel
            )
        }

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

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }
    }
}