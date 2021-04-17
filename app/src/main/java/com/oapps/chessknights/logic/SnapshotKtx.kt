package com.oapps.chessknights.logic

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.oapps.chessknights.Vec
import com.oapps.chessknights.ui.chess.Tile

fun SnapshotStateMap<Vec, Tile>.getOrMake(key: Vec) = getOrPut(key) {Tile(key)}