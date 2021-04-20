package com.oapps.knightschess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.oapps.audio.SoundManager
import com.oapps.knightschess.ui.chess.DynamicChessBoard
import com.oapps.knightschess.ui.chess.DynamicChessBoardPreview
import com.oapps.knightschess.ui.theme.ChessKnightsTheme


class MainActivity : ComponentActivity() {

    var mSoundManager: MutableState<SoundManager?> = mutableStateOf(null)

    override fun onResume() {
        super.onResume()
        val maxSimultaneousStreams = 3
        mSoundManager.value = SoundManager(this, maxSimultaneousStreams)
        mSoundManager.value?.start()
        mSoundManager.value?.load(R.raw.move)
    }

    override fun onPause() {
        super.onPause()
        if (mSoundManager.value != null) {
            mSoundManager.value?.cancel()
            mSoundManager.value = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChessKnightsTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    DynamicChessBoard(modifier = Modifier.fillMaxWidth(), soundManager = mSoundManager.value)
                }
            }
        }
    }
}
