package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pia.piaequipo2.model.Contact

@Composable
fun AddContactScreen(navController: NavController) {
    val context = LocalContext.current
    val userList = remember { mutableStateListOf<Triple<String, String, String>>() } // uid, name, email
    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var showNotFound by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUid = currentUser?.uid ?: return
    val currentUserName = remember { mutableStateOf("") }
    val currentUserEmail = remember { mutableStateOf("") }

    // Cargar nombre/email del usuario actual
    LaunchedEffect(currentUid) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid)
        userRef.get().addOnSuccessListener { snapshot ->
            currentUserName.value = snapshot.child("name").getValue(String::class.java) ?: ""
            currentUserEmail.value = snapshot.child("email").getValue(String::class.java) ?: ""
        }
    }

    // Cargar lista de usuarios (excepto yo)
    LaunchedEffect(true) {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.get().addOnSuccessListener { snapshot ->
            userList.clear()
            snapshot.children.forEach { user ->
                val uid = user.key ?: return@forEach
                val name = user.child("name").getValue(String::class.java) ?: ""
                val email = user.child("email").getValue(String::class.java) ?: ""
                if (uid != currentUid) {
                    userList.add(Triple(uid, name, email))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text("Añadir contacto", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                selectedUser = null
                showNotFound = false
            },
            label = { Text("Buscar por correo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val user = userList.find { it.third.equals(searchQuery.trim(), ignoreCase = true) }
                if (user != null) {
                    selectedUser = user
                    showNotFound = false
                } else {
                    selectedUser = null
                    showNotFound = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buscar")
        }

        Spacer(Modifier.height(16.dp))

        if (showNotFound) {
            Text("Usuario no encontrado.", color = Color.Red)
        }

        selectedUser?.let { (uid, name, email) ->
            Text("Usuario encontrado:", color = Color(0xFF2E7D32))
            Spacer(Modifier.height(8.dp))
            Text("Nombre: $name")
            Text("Correo: $email", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val db = FirebaseDatabase.getInstance().getReference("contacts")

                    val myContact = Contact(uid = uid, name = name, email = email)
                    val theirContact = Contact(
                        uid = currentUid,
                        name = currentUserName.value,
                        email = currentUserEmail.value
                    )

                    val myRef = db.child(currentUid).child(uid)
                    val theirRef = db.child(uid).child(currentUid)

                    myRef.setValue(myContact)
                    theirRef.setValue(theirContact)

                    Toast.makeText(context, "Contacto añadido", Toast.LENGTH_SHORT).show()
                    navController.popBackStack("home", inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir contacto")
            }
        }
    }
}
