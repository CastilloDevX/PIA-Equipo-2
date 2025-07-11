package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pia.piaequipo2.R
import com.pia.piaequipo2.model.ChatPreview
import com.pia.piaequipo2.model.Contact
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: return
    var userName by remember { mutableStateOf("") }
    var chatPreviews by remember { mutableStateOf(listOf<ChatPreview>()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        val database = Firebase.database.reference
        val userRef = database.child("users").child(uid)

        // Obtener el nombre del usuario
        userRef.child("name").get().addOnSuccessListener {
            userName = it.value?.toString() ?: ""
        }

        // Verificación de suspensión
        userRef.child("suspendedUntil").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suspendedUntil = snapshot.getValue(Long::class.java) ?: 0L
                if (System.currentTimeMillis() < suspendedUntil) {
                    val remaining = ((suspendedUntil - System.currentTimeMillis()) / 1000).toInt()
                    Toast.makeText(
                        context,
                        context.getString(R.string.account_suspended_try_on_x_seconds, remaining),
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Precargar usuarios
        val allUsersSnapshot = database.child("users").get().await()
        val allUsers = mutableMapOf<String, Contact>()
        allUsersSnapshot.children.forEach { userSnap ->
            val id = userSnap.key ?: return@forEach
            val name = userSnap.child("name").getValue(String::class.java) ?: "Desconocido"
            val email = userSnap.child("email").getValue(String::class.java) ?: ""
            allUsers[id] = Contact(name, email, id)
        }

        // Obtener contactos del usuario
        val contactSnap = database.child("contacts").child(uid).get().await()
        val contacts = mutableMapOf<String, Contact>()
        contactSnap.children.forEach { child ->
            val contact = child.getValue(Contact::class.java)
            if (contact != null) contacts[contact.uid] = contact
        }

        // Escuchar mensajes
        database.child("messages").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(msgSnap: DataSnapshot) {
                val previews = mutableListOf<ChatPreview>()
                val allContactUids = contacts.keys

                // Chats con mensajes
                msgSnap.children.forEach { chat ->
                    val contactUid = chat.key ?: return@forEach
                    val lastMessageSnap = chat.children.maxByOrNull {
                        it.child("timestamp").getValue(Long::class.java) ?: 0L
                    }

                    val lastText = lastMessageSnap?.child("text")?.getValue(String::class.java) ?: ""
                    val seen = lastMessageSnap?.child("seen")?.getValue(Boolean::class.java) ?: true
                    val contact = contacts[contactUid] ?: allUsers[contactUid]
                    if (contact != null) {
                        previews.add(ChatPreview(contact, lastText, seen))
                    }
                }

                // Agregar contactos sin mensajes
                allContactUids.forEach { contactUid ->
                    val alreadyInList = previews.any { it.contact.uid == contactUid }
                    if (!alreadyInList) {
                        val contact = contacts[contactUid] ?: allUsers[contactUid]
                        if (contact != null) {
                            previews.add(ChatPreview(contact, lastMessage = "", seen = true))
                        }
                    }
                }

                // Ordenar por nombre alfabéticamente
                chatPreviews = previews.sortedBy { it.contact.name }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filteredPreviews = chatPreviews.filter {
        it.contact.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.greeting, userName)) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_contact") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.search_contacts)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            LazyColumn {
                items(filteredPreviews) { preview ->
                    ChatPreviewItem(preview) {
                        navController.navigate("chat/${preview.contact.uid}")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatPreviewItem(preview: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = preview.contact.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = preview.lastMessage.ifEmpty { "Sin mensajes" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (!preview.seen) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (preview.seen) FontStyle.Italic else FontStyle.Normal
                )
            )
        }
        Icon(Icons.Default.Chat, contentDescription = "Chat")
    }
}
