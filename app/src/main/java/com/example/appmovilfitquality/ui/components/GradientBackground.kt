package com.example.appmovilfitquality.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.appmovilfitquality.ui.theme.GreenEnergy


@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color.Black, GreenEnergy)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        content()
    }
}