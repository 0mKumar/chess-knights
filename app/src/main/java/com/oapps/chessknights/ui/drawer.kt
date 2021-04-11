package com.oapps.chessknights.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.firebase.auth.FirebaseUser
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun AppDrawer(navController: NavController, currentUser: FirebaseUser?, signOut: () -> Unit){
    Column {
        CoilImage(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White, CircleShape),
            data = currentUser?.photoUrl?:"",
            contentDescription = ""
        )
        if(currentUser?.displayName != null){
            Text(text = currentUser.displayName ?:"Unnamed user")
        }
        DrawerButton("Home"){
            navController.navigate("home")
        }
        DrawerButton("Puzzles"){
            navController.navigate("puzzle")
        }
        DrawerButton("Live"){
            navController.navigate("live")
        }

        if(currentUser == null) {
            DrawerButton("Login") {
                navController.navigate("login")
            }
        }else{
            DrawerButton("Signout") {
                signOut()
                navController.navigate("login")
            }
        }
    }
}

@Composable
fun DrawerButton(text: String, onClick: () -> Unit){
    TextButton(onClick = onClick,
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)){
        Text(text = text, textAlign = TextAlign.Start)
    }
}