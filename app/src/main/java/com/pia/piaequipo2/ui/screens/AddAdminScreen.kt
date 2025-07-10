package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.pia.piaequipo2.utils.promoteToAdmin
import com.pia.piaequipo2.R

@Composable
fun AddAdminScreen() {
    val context = LocalContext.current
    val userList = remember { mutableStateListOf<Triple<String, String, String>>() } // uid, name, email
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.get().addOnSuccessListener { snapshot ->
            userList.clear()
            snapshot.children.take(10).forEach { user ->
                val name = user.child("name").getValue(String::class.java) ?: ""
                val email = user.child("email").getValue(String::class.java) ?: ""
                val uid = user.key ?: ""
                userList.add(Triple(uid, name, email))
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Spacer(Modifier.height(30.dp))
        Text(stringResource(R.string.assing_admin), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por correo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        val filteredUsers = userList.filter {
            it.third.contains(searchQuery.trim(), ignoreCase = true)
        }

        filteredUsers.forEach { (uid, name, email) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(name)
                    Text(email, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = {
                    promoteToAdmin(uid)
                    Toast.makeText(context, context.getString(R.string.user_is_admin, name), Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.make_admin))
                }
            }
        }
    }
}