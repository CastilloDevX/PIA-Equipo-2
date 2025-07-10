package com.pia.piaequipo2.model

data class Contact(
    val uid: String = "",
    val name: String = "",
    val email: String = ""
)

data class ChatPreview(
    val contact: Contact,
    val lastMessage: String = "",
    val seen: Boolean = true
)

data class Message(
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false
)
