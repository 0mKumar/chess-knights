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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.R
import com.oapps.chessknights.TAG

@ExperimentalAnimationApi
@Composable
fun SignUpScreen(navController: NavController, firebaseSignIn: () -> Unit) {
    val horizontalPadding = 64.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var registerError by remember { mutableStateOf(false) }
        var errorText by remember { mutableStateOf("") }

        AnimatedVisibility(visible = registerError && errorText.isNotEmpty()) {
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

        var userName by remember {
            mutableStateOf("")
        }

        var userNameError by remember {
            mutableStateOf(false)
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = userName,
            onValueChange = {
                userName = it
                userNameError = it.isEmpty() || it.contains(' ')
                registerError = false
            },
            isError = registerError || userNameError,
            label = { Text("Username") },

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )



        Divider(Modifier.padding(vertical = 32.dp))

        var emailError by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = email,
            onValueChange = {
                email = it
                emailError = it.isEmpty()
                registerError = false
            },
            isError = registerError || emailError,
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
                registerError = false
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
            isError = registerError || passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        var confirmPassword by remember { mutableStateOf("") }
        var confirmPassVisible by remember { mutableStateOf(false) }
        var confirmPasswordError by remember {
            mutableStateOf(false)
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = it.isEmpty()
                registerError = false
            },
            label = { Text("Confirm password") },
            visualTransformation = if (confirmPassVisible) VisualTransformation.None else passwordVisualTransformation,
            trailingIcon = {
                Icon(
                    painterResource(id = if (confirmPassVisible) R.drawable.ic_twotone_visibility_24 else R.drawable.ic_twotone_visibility_off_24),
                    "Toggle password",
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        confirmPassVisible = !confirmPassVisible
                    }
                )
            },
            isError = registerError || confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        var registeringWait by remember { mutableStateOf(false) }
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
                        } else if (password != confirmPassword) {
                            confirmPasswordError = true
                        } else if (userName.isEmpty()) {
                            userNameError = true
                        } else {
                            registeringWait = true
                            Firebase.auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success")
                                        val user = Firebase.auth.currentUser
                                        registeringWait = false
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                        errorText = task.exception?.message?:""
                                        registeringWait = false
                                        registerError = true
                                    }
                                }
                        }
                    }
                },
                enabled = valuesEntered && !registeringWait && !registerError && !emailError && password == confirmPassword && !userNameError
            ) {
                Text("Create account")
            }
            if (registeringWait) {
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
                Text("Create account using Google")
            }
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already registered? Login here")
        }
    }
}