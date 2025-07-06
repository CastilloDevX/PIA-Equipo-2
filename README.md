# PIA - Equipo 2
* **Autor:** Jose Manuel Castillo Queh  
* **Cliente:** Gael Arath Mejía Ruiz  
* **Versión:** 1.0.0  
* **Fecha:** Julio 2025 

Aplicación móvil para Android desarrollada con **Kotlin** y **Jetpack Compose**, que permite a los usuarios:
- Registrarse con nombre, correo y contraseña
- Confirmar su contraseña
- Autenticar su cuenta por email
- Iniciar y cerrar sesión
## Tecnologías utilizadas
| Herramienta               | Uso principal                            |
|--------------------------|------------------------------------------|
| Android Studio           | Entorno de desarrollo                    |
| Kotlin                   | Lenguaje de programación principal       |
| Jetpack Compose          | UI declarativa moderna                   |
| Firebase Authentication  | Registro, login y verificación de email |
| Firebase Realtime DB     | Almacenamiento de nombre de usuario      |
| Material 3               | Estilos modernos para la UI              |

## Lógica por Script
### MainActivity.kt
```kotlin
val startDest = if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"
```
* Detecta si ya hay sesión iniciada.
* Inicia desde home si el usuario ya está autenticado.
* Caso contrario, lleva al login.

## LoginScreen.kt
```kotlin
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword(email, password)
    .addOnSuccessListener {
        if (it.user?.isEmailVerified == true) {
            navController.navigate("home")
        } else {
            Toast.makeText(context, "Verifica tu correo", Toast.LENGTH_SHORT).show()
        }
    }
```
* Valida los campos email y password.
* Inicia sesión solo si el email está verificado.
* En caso de error muestra mensajes como:
    * "Correo o contraseña incorrectos"
    * "Este correo no está verificado"

## RegisterScreen.kt
```kotlin
if (password != confirmPassword) {
    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
    return@Button
}
```
* Valida campos vacíos y que las contraseñas coincidan.
* Crea el usuario en Firebase Authentication.
* Guarda nombre y correo en Firebase Realtime DB:

```kotlin
val uid = it.user!!.uid
val userData = mapOf("name" to name, "email" to email)
Firebase.database.reference.child("users").child(uid).setValue(userData)
```
Envía correo de verificación:
```kotlin
it.user?.sendEmailVerification()
```
## VerifyEmailScreen.kt
```kotlin
Text("Te hemos enviado un correo de verificación. Por favor revisa tu bandeja de entrada.")
```
* Pantalla que muestra un mensaje luego del registro.
* Se indica al usuario verificar el correo para continuar.

## HomeScreen.kt
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
val uid = currentUser?.uid
val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid ?: "")
```
- Muestra saludo personalizado usando el nombre almacenado.
- Carga el nombre desde Realtime Database al iniciar la pantalla.

Posee un botón para cerrar sesión:
```kotlin
FirebaseAuth.getInstance().signOut()
navController.navigate("login") {
    popUpTo("home") { inclusive = true }
}
```

## Flujo de navegación
1. El usuario abre la app.
2. Si ya inició sesión y está verificado → entra a Home.
3. Si no ha iniciado sesión → Login o Registro.
4. Al registrarse:
    - Verifica que los campos sean correctos
    - Crea usuario
    - Guarda nombre y correo
    - Envía email de verificación
5. Al loguearse:
    - Solo entra si el correo fue verificado
6. En Home:
    - Muestra saludo
    - Botón para cerrar sesión

## Ejecución del proyecto
1. Clona el repositorio:
    ```bash
    git clone https://github.com/tuusuario/nombre-del-repo.git
    ```
2. Abre en Android Studio.
3. Asegúrate de tener el archivo google-services.json en la carpeta app/.
4. Ejecuta el proyecto en un emulador o dispositivo físico Android.

# Licencia
Este proyecto fue desarrollado como entrega profesional por **Jose Manuel Castillo Queh** para fines académicos y demostrativos.
Prohibida su distribución sin autorización del autor.
