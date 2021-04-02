package com.oapps.chessknights.models

data class TimeControl(
    val timeMillis: Int = 10 * MINUTE,
    val incrementMillis: Int = 0 * SECOND
) {
    constructor(control: String) : this(
        control.substringBefore('+').toInt() * MINUTE,
        control.substringAfter('+').toInt() * SECOND
    )

    fun hasIncrement() = incrementMillis > 0
    fun timeMinutes() = timeMillis / MINUTE
    fun incrementSeconds() = incrementMillis / SECOND

    fun type() = when{
        timeMillis < 2 * MINUTE -> "bullet"
        timeMillis < 10 * MINUTE -> "blitz"
        timeMillis < 30 * MINUTE -> "rapid"
        else -> "classical"
    }

    companion object {
        const val SECOND = 1000
        const val MINUTE = 60 * SECOND
        fun fixed(timeMinute: Int) = TimeControl(timeMinute * MINUTE)
        fun withIncrement(timeMinute: Int, incrementSecond: Int) =
            TimeControl(timeMinute * MINUTE, incrementSecond * SECOND)
    }

    fun notation() = "${timeMillis / MINUTE}+${incrementMillis / SECOND}"
}
