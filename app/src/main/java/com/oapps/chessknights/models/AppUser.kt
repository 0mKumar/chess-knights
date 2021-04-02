package com.oapps.chessknights.models

// users/$uid
class AppUser(
    val uName: String = "",
    val fName: String = "",
    val lName: String = "",
    var rating: Int = 1200,
    var rd: Int = 300,
) {
    companion object{
        val None = AppUser()
    }
}