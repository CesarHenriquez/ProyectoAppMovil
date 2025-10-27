package com.example.appmovilfitquality.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.appmovilfitquality.domain.model.UserRole
import com.example.appmovilfitquality.viewmodel.AuthViewModel

@Composable
fun AppHostScreen(

    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onGoToStore: () -> Unit,
    onGoToStock: () -> Unit,
    onGoToDelivery: () -> Unit
) {
    val userRole by authViewModel.currentUserRole.collectAsState()

    when (userRole) {
        UserRole.CLIENTE -> onGoToStore()
        UserRole.STOCK -> onGoToStock()
        UserRole.DELIVERY -> onGoToDelivery()
        UserRole.GUEST -> onLogout()
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}