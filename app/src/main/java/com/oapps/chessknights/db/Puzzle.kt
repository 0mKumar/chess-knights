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
): RealmObject(){
    override fun toString(): String {
        if (!isValid(this)) {
            return "Invalid object"
        }
        val stringBuilder = StringBuilder("Puzzle = [")
        stringBuilder.append("{pid:")
        stringBuilder.append(pid)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{fen:")
        stringBuilder.append(fen)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{moves:")
        stringBuilder.append(moves.size).append(" [")
        stringBuilder.append(moves.joinToString())
        stringBuilder.append("]}")
        stringBuilder.append(",")
        stringBuilder.append("{rating:")
        stringBuilder.append(rating)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{ratingDeviation:")
        stringBuilder.append(ratingDeviation)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{popularity:")
        stringBuilder.append(popularity)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{nbPlays:")
        stringBuilder.append(nbPlays)
        stringBuilder.append("}")
        stringBuilder.append(",")
        stringBuilder.append("{themes:")
        stringBuilder.append(themes.size).append(" [")
        stringBuilder.append(themes.joinToString())
        stringBuilder.append("]}")
        stringBuilder.append(",")
        stringBuilder.append("{url:")
        stringBuilder.append(url)
        stringBuilder.append("}")
        stringBuilder.append("]")
        return stringBuilder.toString()
    }
}