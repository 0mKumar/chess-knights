package com.oapps.chessknights

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.oapps.chessknights.ui.AppDrawer
import com.oapps.chessknights.ui.LivePlayScreen
import com.oapps.chessknights.ui.LoginScreen
import com.oapps.chessknights.ui.SignUpScreen
import com.oapps.chessknights.ui.theme.ChessKnightsTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

val TAG = "Compose"

class MainActivity : AppCompatActivity() {
    lateinit var loginLauncher: ActivityResultLauncher<Intent>
    lateinit var navController: NavHostController

    @ExperimentalAnimationApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }

        loginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task =
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account =
                            task.getResult(ApiException::class.java)!!
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e)
                    }
                }
            }

        chess.state.resetCastling("KkQq")

        val appBarMap = mapOf(
            "login" to "Login",
            "signup" to "Create Account",
            "home" to "Home",
            "live" to "Live chess"
        )

        setContent {
            val internetAvailable by ConnectionLiveData(this).observeAsState(false)
            val darkMode = remember { mutableStateOf(true) }
            ChessKnightsTheme(window, darkTheme = darkMode.value) {
                val currentUser by Firebase.auth.currentUserAsState()
                val navState by navController.currentBackStackEntryAsState()
                Scaffold(
                    topBar = {
                        navState?.arguments?.getString(KEY_ROUTE)?.let{
                            if(appBarMap.containsKey(it)){
                                AppTopBar(appBarMap[it])
                            }
                        }
                    },
                    drawerContent = {
                        AppDrawer(
                            navController = navController,
                            currentUser = currentUser,
                            signOut = { firebaseSignOut() }
                        )
                    }
                ) {

                    NavHost(
                        navController = navController,
                        startDestination = if(currentUser == null) "login" else "home"
                    ) {
                        composable("error") {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Error encountered")
                            }
                        }
                        composable("live") {
                            LivePlayScreen(darkMode, internetAvailable)
                        }
                        composable("home") {
                            HomeScreen(currentUser, ::firebaseSignIn, ::firebaseSignOut, navController)
                        }
                        composable("signup") {
                            SignUpScreen(navController, ::firebaseSignIn)
                        }
                        composable("login") {
                            LoginScreen(::firebaseSignIn, navController)
                        }
                    }
                }
            }
        }
    }

    private fun firebaseSignIn() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleSignInClient =
            GoogleSignIn.getClient(this@MainActivity, gso)
        val signInIntent = googleSignInClient.signInIntent

        loginLauncher.launch(signInIntent.apply {
            putExtra("input_int", 12)
        })
    }

    private fun firebaseSignOut() {
        Firebase.auth.signOut()
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleSignInClient =
            GoogleSignIn.getClient(this@MainActivity, gso)
        googleSignInClient.signOut()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = Firebase.auth.currentUser
//                updateUI(user)
//                    navController.navigate("home")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Sign in failed: " + task.exception?.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
//                    navController.navigate("error")
                }
            }
    }

}

@Composable
private fun HomeScreen(
    currentUser: FirebaseUser?,
    firebaseSignIn: () -> Unit,
    firebaseSignOut: () -> Unit,
    navController: NavHostController
) {
    Column(Modifier.padding(16.dp)) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate("live") }) {
            Image(painterResource(id = R.drawable.wn), contentDescription = "Live chess", Modifier.size(32.dp).padding(end = 8.dp))
            Text("Live chess")
        }
    }
}


@Composable
fun AppTopBar(title: String?) {
    TopAppBar(
        title = {
            Text(
                text = title ?: "Knight Chess",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 24.sp,
                color = Color.White
            )
        },
        actions = {

        }
    )
}