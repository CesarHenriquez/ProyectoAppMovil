package com.example.appmovilfitquality

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.appmovilfitquality.navigation.NavGraph
import com.example.appmovilfitquality.ui.theme.AppMovilFitQualityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)



        setContent {
            AppMovilFitQualityTheme  {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}