package com.pia.piaequipo2.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pia.piaequipo2.R

@Composable
fun LoginScreen(navController: NavController) {
    Log.d("Pantalla", "Entrando a LoginScreen")

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                checkEmailExistsAndGetUid(email) { uid ->
                    if (uid == null) {
                        Toast.makeText(context, context.getString(R.string.error_email_not_found), Toast.LENGTH_SHORT).show()
                        return@checkEmailExistsAndGetUid
                    }

                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            if (user?.isEmailVerified != true) {
                                Toast.makeText(context, context.getString(R.string.error_verify_email), Toast.LENGTH_LONG).show()
                                return@addOnSuccessListener
                            }

                            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
                            userRef.get().addOnSuccessListener { snapshot ->
                                val suspendedUntil = snapshot.child("suspendedUntil").getValue(Long::class.java) ?: 0L
                                val now = System.currentTimeMillis()

                                if (now < suspendedUntil) {
                                    val remaining = ((suspendedUntil - now) / 1000).toInt()
                                    Toast.makeText(context, context.getString(R.string.account_suspended_try_on_x_seconds, remaining), Toast.LENGTH_LONG).show()
                                    FirebaseAuth.getInstance().signOut()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, context.getString(R.string.error_verifying_suspension), Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, context.getString(R.string.error_wrong_password), Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login))
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text(stringResource(R.string.no_account_register))
        }
    }
}

fun checkEmailExistsAndGetUid(email: String, onResult: (String?) -> Unit) {
    val usersRef = FirebaseDatabase.getInstance().getReference("users")
    usersRef.get().addOnSuccessListener { snapshot ->
        val match = snapshot.children.firstOrNull { user ->
            val userEmail = user.child("email").getValue(String::class.java)
            userEmail.equals(email.trim(), ignoreCase = true)
        }
        val uid = match?.key
        Log.d("DEBUG_LOGIN", "Email buscado: '$email' → uid encontrada: $uid")
        onResult(uid)
    }.addOnFailureListener {
        Log.e("DEBUG_LOGIN", "Error al buscar email: ${it.message}")
        onResult(null)
    }
}
