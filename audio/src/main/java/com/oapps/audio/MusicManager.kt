package com.oapps.audio

import android.content.Context
import android.media.MediaPlayer

/*
* Copyright (c) delight.im <info@delight.im>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/** Plays music and one-off sound files while managing the resources efficiently  */
class MusicManager private constructor() {
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * Plays the sound with the given resource ID
     *
     * @param context a valid `Context` reference
     * @param soundResourceId the resource ID of the sound (e.g. `R.raw.my_sound`)
     */
    @Synchronized
    fun play(context: Context, soundResourceId: Int) {
        // if there's an existing stream playing already
        if (mMediaPlayer != null) {
            // stop the stream in case it's still playing
            try {
                mMediaPlayer!!.stop()
            } catch (e: Exception) {
            }

            // release the resources
            mMediaPlayer!!.release()

            // unset the reference
            mMediaPlayer = null
        }

        // create a new stream for the sound to play
        mMediaPlayer = MediaPlayer.create(context.applicationContext, soundResourceId)

        // if the instance could be created
        if (mMediaPlayer != null) {
            // set a listener that is called when playback has been finished
            mMediaPlayer!!.setOnCompletionListener { mp ->
                // if the instance is set
                if (mp != null) {
                    // release the resources
                    mp.release()

                    // unset the reference
                    mMediaPlayer = null
                }
            }

            // start playback
            mMediaPlayer!!.start()
        }
    }

    companion object {
        private var mInstance: MusicManager? = null

        /**
         * Returns the single instance of this class
         *
         * @return the instance
         */
        val instance: MusicManager?
            get() {
                if (mInstance == null) {
                    mInstance = MusicManager()
                }
                return mInstance
            }
    }
}