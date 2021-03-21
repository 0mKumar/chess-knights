package com.oapps.chessknights.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.currentUserAsState

@Composable
fun SignUp(navController: NavController) {
    val currentUser by Firebase.auth.currentUserAsState()
    currentUser?.let {
        var firstName by remember {
            mutableStateOf(
                it.displayName?.substringBefore(' ') ?: ""
            )
        }
        var lastName by remember {
            mutableStateOf(
                it.displayName?.substringAfter(' ', "") ?: ""
            )
        }

        Column {
            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, isError = firstName.isBlank())
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, isError = lastName.isBlank())
            Button(onClick = {}){
                Text(text = "Create account")
            }
        }
    }?: run {
        navController.navigate("home")
    }
}