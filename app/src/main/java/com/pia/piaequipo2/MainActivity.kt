package com.pia.piaequipo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pia.piaequipo2.ui.theme.PIAEquipo2Theme
import com.pia.piaequipo2.ui.screens.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pia.piaequipo2.utils.getSavedLanguage
import com.pia.piaequipo2.utils.setLocale
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.navArgument
import androidx.navigation.NavType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            setContent {
                PIAEquipo2Theme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        var startDest by remember { mutableStateOf<String?>(null) }
                        val context = LocalContext.current

                        LaunchedEffect(Unit) {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user == null) {
                                startDest = "login"
                            } else {
                                checkIfUserSuspended { isSuspended ->
                                    if (isSuspended) {
                                        Toast.makeText(context, context.getString(R.string.your_account_is_suspended), Toast.LENGTH_LONG).show()

                                        FirebaseAuth.getInstance().signOut()
                                        startDest = "login"
                                    } else {
                                        startDest = "home"
                                    }
                                }
                            }
                        }

                        if (startDest != null) {
                            NavHost(navController = navController, startDestination = startDest!!) {
                                composable("login") { LoginScreen(navController) }
                                composable("register") { RegisterScreen(navController) }
                                composable("verify") { VerifyEmailScreen(navController) }
                                composable("home") { HomeScreen(navController) }
                                composable("settings") { SettingsScreen(navController) }
                                composable("suspend_users") { SuspendUsersScreen() }
                                composable("add_admin") { AddAdminScreen() }
                                composable("add_contact") { AddContactScreen(navController) }
                                composable(
                                    "chat/{contactUid}",
                                    arguments = listOf(navArgument("contactUid") { type = NavType.StringType })
                                ) { backEntry ->
                                    ChatScreen(navController, backEntry.arguments!!.getString("contactUid")!!)
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = getSavedLanguage(newBase)
        val context = setLocale(newBase, langCode)
        super.attachBaseContext(context)
    }

}


fun checkIfUserSuspended(onResult: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onResult(false)
        return
    }
    val ref = com.google.firebase.database.FirebaseDatabase.getInstance()
        .getReference("users/${user.uid}/suspendedUntil")

    ref.get().addOnSuccessListener { snapshot ->
        val suspendedUntil = snapshot.getValue(Long::class.java) ?: 0L
        val now = System.currentTimeMillis()
        onResult(suspendedUntil > now)
    }.addOnFailureListener {
        onResult(false) // Si falla, asumimos que no está suspendido
    }
}
