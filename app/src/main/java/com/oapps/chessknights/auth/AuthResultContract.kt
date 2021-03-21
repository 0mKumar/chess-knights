package com.oapps.chessknights.auth
//
//import android.content.Context
//import android.content.Intent
//import androidx.activity.result.contract.ActivityResultContract
//
//class AuthResultContract : ActivityResultContract<Int, IdpResponse>() {
//
//    companion object {
//        const val INPUT_INT = "input_int"
//    }
//
//    private val providers = arrayListOf(
//        AuthUI.IdpConfig.EmailBuilder().build(),
//        AuthUI.IdpConfig.GoogleBuilder().build()
//    )
//
//    override fun createIntent(context: Context, input: Int?): Intent = AuthUI.getInstance()
//        .createSignInIntentBuilder()
//        .setLogo(R.drawable.img_auth_logo)
//        .setTheme(R.style.Theme_MyTheme_Auth)
//        .setAvailableProviders(providers)
//        .setIsSmartLockEnabled(true)
//        .build().apply { putExtra(INPUT_INT, input) }
//
//    override fun parseResult(resultCode: Int, intent: Intent?): IdpResponse? = when (resultCode) {
//        Activity.RESULT_OK -> IdpResponse.fromResultIntent(intent)
//        else -> null
//    }
//
//}