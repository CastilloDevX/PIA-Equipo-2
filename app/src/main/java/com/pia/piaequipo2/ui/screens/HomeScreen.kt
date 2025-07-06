package com.pia.piaequipo2.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    var name by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        uid?.let {
            val database = Firebase.database.reference
            database.child("users").child(it).get().addOnSuccessListener { snapshot ->
                name = snapshot.child("name").value.toString()
                Log.d("Home", "Nombre obtenido: $name")
            }.addOnFailureListener {
                Log.e("Home", "Error al obtener nombre: ${it.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Hola, $name!", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
