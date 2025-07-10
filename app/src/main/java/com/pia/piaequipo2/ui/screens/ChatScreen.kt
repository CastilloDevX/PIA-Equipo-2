package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pia.piaequipo2.R
import com.pia.piaequipo2.model.Message
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, contactUid: String) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserUid = currentUser?.uid ?: return
    val messageList = remember { mutableStateListOf<Message>() }
    var messageText by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("Chat") }

    val messagesRef = FirebaseDatabase.getInstance().getReference("messages")
        .child(currentUserUid).child(contactUid)

    // Obtener nombre del contacto
    LaunchedEffect(contactUid) {
        val contactRef = FirebaseDatabase.getInstance().getReference("users")
            .child(contactUid)
        contactRef.get().addOnSuccessListener {
            contactName = it.child("name").getValue(String::class.java) ?: "Chat"
        }.addOnFailureListener {
            contactName = "Chat"
        }
    }

    // Escuchar mensajes en tiempo real
    LaunchedEffect(Unit) {
        messagesRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (child in snapshot.children) {
                    val msg = child.getValue(Message::class.java)
                    if (msg != null) {
                        messageList.add(msg)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_to_send_msgs),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Chat, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messageList.reversed()) { message ->
                    val isMe = message.sender == currentUserUid
                    MessageBubble(message = message, isMe = isMe)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.write_msg)) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        val timestamp = System.currentTimeMillis()

                        // Mensaje para TI (ya leído)
                        val myMessage = Message(
                            sender = currentUserUid,
                            text = messageText,
                            timestamp = timestamp,
                            seen = true
                        )

                        // Mensaje para EL OTRO (no leído)
                        val contactMessage = Message(
                            sender = currentUserUid,
                            text = messageText,
                            timestamp = timestamp,
                            seen = false
                        )

                        val myRef = FirebaseDatabase.getInstance().getReference("messages")
                            .child(currentUserUid).child(contactUid).push()

                        val contactRef = FirebaseDatabase.getInstance().getReference("messages")
                            .child(contactUid).child(currentUserUid).push()

                        myRef.setValue(myMessage)
                        contactRef.setValue(contactMessage)

                        messageText = ""
                    }
                }) {
                    Text(stringResource(R.string.send))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    val formatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    val time = formatter.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(message.text)
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
