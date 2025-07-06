package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VerifyEmailScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verifica tu correo", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Text("Revisa tu bandeja de entrada. Debes verificar antes de continuar.")

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().currentUser?.reload()?.addOnSuccessListener {
                if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                    Toast.makeText(context, "Correo verificado", Toast.LENGTH_SHORT).show()
                    navController.navigate("home")
                } else {
                    Toast.makeText(context, "Aún no has verificado tu correo", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Ya verifiqué")
        }

        TextButton(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo("verify") { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
