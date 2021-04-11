package com.oapps.chessknights.db

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

//PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl

open class Puzzle(
    @PrimaryKey
    var pid: String = "",
    var fen: String = "",
    var moves: RealmList<String> = RealmList(),
    var rating: Int = 1200,
    var ratingDeviation: Int = 50,
    var popularity: Int = 0,
    var nbPlays: Int = 0,
    var themes: RealmList<String> = RealmList(),
    var url: String = ""
): RealmObject()