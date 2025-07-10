package com.pia.piaequipo2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pia.piaequipo2.utils.getSavedLanguage
import com.pia.piaequipo2.utils.saveLanguage
import com.pia.piaequipo2.utils.setLocale
import com.pia.piaequipo2.R
import android.app.Activity
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf(getSavedLanguage(context)) }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")

        ref.get().addOnSuccessListener {
            isAdmin = it.child("isAdmin").getValue(Boolean::class.java) ?: false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.language))
            DropdownMenuButton(selectedLang) { newLang ->
                selectedLang = newLang
                saveLanguage(context, newLang)
                setLocale(context, newLang)
                Toast.makeText(context, context.getString(R.string.lang_changed), Toast.LENGTH_SHORT).show()

                val activity = context as? Activity
                activity?.recreate() // Reinicia la actividad para aplicar idioma
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.logout) + ":")
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.logout))
            }
        }

        if (isAdmin) {
            Text(stringResource(R.string.only_admins))
            Button(
                onClick = { navController.navigate("suspend_users") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.suspension_accounts))
            }

            Button(
                onClick = { navController.navigate("add_admin") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_admin))
            }
        }
    }
}

@Composable
fun DropdownMenuButton(selected: String, onLanguageChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.uppercase())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Español") }, onClick = {
                expanded = false
                onLanguageChange("es")
            })
            DropdownMenuItem(text = { Text("English") }, onClick = {
                expanded = false
                onLanguageChange("en")
            })
        }
    }
}
