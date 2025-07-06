package com.pia.piaequipo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pia.piaequipo2.ui.theme.PIAEquipo2Theme
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.pia.piaequipo2.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PIAEquipo2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDest = if (FirebaseAuth.getInstance().currentUser != null)
                        "home" else "login"

                    NavHost(navController = navController, startDestination = startDest) {
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("verify") { VerifyEmailScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                    }

                }
            }
        }
    }
}