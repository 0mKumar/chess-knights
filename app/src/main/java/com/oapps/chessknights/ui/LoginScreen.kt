package com.oapps.chessknights.ui

import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.R
import com.oapps.chessknights.TAG

@ExperimentalAnimationApi
@Composable
fun LoginScreen(firebaseSignIn: () -> Unit, navController: NavHostController) {
    val horizontalPadding = 64.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var loginError by remember { mutableStateOf(false) }

        var errorText by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf(false) }

        AnimatedVisibility(visible = errorText.isNotEmpty() && (emailError || loginError)) {
            Text(
                text = errorText,
                Modifier
                    .padding(bottom = 16.dp)
                    .background(
                        MaterialTheme.colors.error,
                        remember { RoundedCornerShape(4.dp) })
                    .padding(16.dp),
                color = MaterialTheme.colors.onError
            )
        }

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = email,
            onValueChange = {
                email = it
                emailError = it.isEmpty()
                loginError = false
            },
            isError = loginError || emailError,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        var passwordError by remember {
            mutableStateOf(false)
        }
        var password by remember { mutableStateOf("") }

        var passVisible by remember { mutableStateOf(false) }
        val passwordVisualTransformation = remember { PasswordVisualTransformation() }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = password,
            onValueChange = {
                password = it
                passwordError = it.isEmpty()
                loginError = false
            },
            label = { Text("Password") },
            visualTransformation = if (passVisible) VisualTransformation.None else passwordVisualTransformation,
            trailingIcon = {
                Icon(
                    painterResource(id = if (passVisible) R.drawable.ic_twotone_visibility_24 else R.drawable.ic_twotone_visibility_off_24),
                    "Toggle password",
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        passVisible = !passVisible
                    }
                )
            },
            isError = loginError || passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        var loggingIn by remember { mutableStateOf(false) }
        val valuesEntered = password.isNotEmpty() && email.isNotEmpty()
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth(), contentAlignment = Alignment.CenterStart
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (valuesEntered) {
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = true
                            errorText = "Email id is incorrect"
                        } else {
                            loggingIn = true
                            Firebase.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success")
                                        val user = Firebase.auth.currentUser
                                        loggingIn = false
                                        navController.navigate("home")
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                                        errorText = task.exception?.message?:""
                                        loggingIn = false
                                        loginError = true
                                    }
                                }
                        }
                    }
                }, enabled = valuesEntered && !loggingIn && !loginError && !emailError
            ) {
                Text("Log in")
            }
            if (loggingIn) {
                CircularProgressIndicator(
                    Modifier
                        .padding(start = 8.dp)
                        .requiredSize(24.dp), strokeWidth = 2.dp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Divider(
                Modifier.weight(1f)
            )
            Text(
                "OR",
                Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
            )
            Divider(
                Modifier.weight(1f)
            )
        }

        if (Firebase.auth.currentUser == null) {
            Button(onClick = {
                firebaseSignIn()
            }) {
                Icon(
                    painterResource(id = R.drawable.google),
                    "Google gmail",
                    Modifier.padding(end = 8.dp)
                )
                Text("Login using Google")
            }
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = {navController.navigate("signup")}) {
            Text("Not already a user? Register here")
        }
    }
}
