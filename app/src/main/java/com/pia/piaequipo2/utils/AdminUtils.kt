package com.pia.piaequipo2.utils

import com.google.firebase.database.FirebaseDatabase

fun suspendUser(uid: String, durationSeconds: Long = 30) {
    val suspendedUntil = System.currentTimeMillis() + (durationSeconds * 1000)
    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    userRef.child("suspendedUntil").setValue(suspendedUntil)
}

fun promoteToAdmin(uid: String) {
    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
    userRef.child("isAdmin").setValue(true)
}
