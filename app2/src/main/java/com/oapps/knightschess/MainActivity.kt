package com.oapps.knightschess

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.oapps.audio.SoundManager
import com.oapps.knightschess.ui.chess.DynamicChessBoard
import com.oapps.knightschess.ui.chess.DynamicChessBoardPreview
import com.oapps.knightschess.ui.chess.theme.Image
import com.oapps.knightschess.ui.theme.ChessKnightsTheme


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
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
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        var images by remember { mutableStateOf<Image>(Image.Staunty) }
                        var name by remember { mutableStateOf(images.type) }
                        var ind by remember { mutableStateOf(0) }
                        DynamicChessBoard(modifier = Modifier.fillMaxWidth(), soundManager = mSoundManager.value, images = images)
                        Button(onClick = {
                            Log.d(TAG, "onCreate: $ind")
                            ind++
                            Log.d(TAG, "onCreate: total themes = ${Image.Type.values().size}")
                            if(ind >= Image.Type.values().size){
                                ind = 0
                                Log.d(TAG, "onCreate: making 0")
                            }
                            Log.d(TAG, "onCreate: index = $ind")
                            images = Image.from(Image.Type.values()[ind])
                            name = images.type
                        }) {
                            Text(name)
                        }
                    }
                }
            }
        }
    }
}
