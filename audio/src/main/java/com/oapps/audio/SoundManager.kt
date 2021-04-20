package com.oapps.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Process
import java.util.HashMap
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

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
*/ /** Plays sounds (frequently and fast) while managing the resources efficiently  */
@SuppressLint("UseSparseArrays")
class SoundManager(context: Context, maxSimultaneousStreams: Int) :
    Thread() {
    private class SoundManagerTask private constructor(
        val soundResourceId: Int,
        val volume: Float,
        val repetitions: Int,
        private val mAction: Int
    ) {
        val isLoad: Boolean
            get() = mAction == ACTION_LOAD
        val isPlay: Boolean
            get() = mAction == ACTION_PLAY
        val isUnload: Boolean
            get() = mAction == ACTION_UNLOAD
        val isCancel: Boolean
            get() = mAction == ACTION_CANCEL

        companion object {
            private const val ACTION_LOAD = 1
            private const val ACTION_PLAY = 2
            private const val ACTION_UNLOAD = 3
            private const val ACTION_CANCEL = 4
            fun load(soundResourceId: Int): SoundManagerTask {
                return SoundManagerTask(soundResourceId, 0f, 0, ACTION_LOAD)
            }

            fun play(soundResourceId: Int, volume: Float, repetitions: Int): SoundManagerTask {
                return SoundManagerTask(soundResourceId, volume, repetitions, ACTION_PLAY)
            }

            fun unload(soundResourceId: Int): SoundManagerTask {
                return SoundManagerTask(soundResourceId, 0f, 0, ACTION_UNLOAD)
            }

            fun cancel(): SoundManagerTask {
                return SoundManagerTask(0, 0f, 0, ACTION_CANCEL)
            }
        }
    }

    private val mSoundPool: SoundPool?
    private val mContext: Context
    private val mSounds: MutableMap<Int, Int>?
    private val mTasks: BlockingQueue<SoundManagerTask> = LinkedBlockingQueue()

    @Volatile
    private var mCancelled = false
    fun load(soundResourceId: Int) {
        try {
            mTasks.put(SoundManagerTask.load(soundResourceId))
        } catch (e: InterruptedException) {
        }
    }

    @JvmOverloads
    fun play(soundResourceId: Int, volume: Float = 1.0f, repetitions: Int = 0) {
        if (!isAlive) {
            return
        }
        try {
            mTasks.put(SoundManagerTask.play(soundResourceId, volume, repetitions))
        } catch (e: InterruptedException) {
        }
    }

    fun unload(soundResourceId: Int) {
        try {
            mTasks.put(SoundManagerTask.unload(soundResourceId))
        } catch (e: InterruptedException) {
        }
    }

    fun cancel() {
        try {
            mTasks.put(SoundManagerTask.cancel())
        } catch (e: InterruptedException) {
        }
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        try {
            var task: SoundManagerTask
            while (!mCancelled) {
                task = mTasks.take()
                if (task.isCancel) {
                    mCancelled = true
                    break
                } else {
                    val currentMapping: Int?
                    synchronized(mSounds!!) {
                        currentMapping = mSounds[task.soundResourceId]
                    }
                    if (task.isLoad) {
                        if (currentMapping == null) {
                            val newMapping = mSoundPool!!.load(mContext, task.soundResourceId, 1)
                            synchronized(mSounds) {
                                mSounds.put(
                                    task.soundResourceId,
                                    newMapping
                                )
                            }
                        }
                    } else if (task.isPlay) {
                        if (currentMapping != null) {
                            mSoundPool!!.play(
                                currentMapping.toInt(),
                                task.volume,
                                task.volume,
                                0,
                                task.repetitions,
                                1.0f
                            )
                        }
                    } else if (task.isUnload) {
                        if (currentMapping != null) {
                            mSoundPool!!.unload(currentMapping.toInt())
                            synchronized(mSounds) { mSounds.remove(task.soundResourceId) }
                        }
                    }
                }
            }
        } catch (e: InterruptedException) {
        }
        if (mSounds != null) {
            synchronized(mSounds) { mSounds.clear() }
        }
        mSoundPool?.release()
    }

    init {
        mSoundPool = SoundPool(maxSimultaneousStreams, AudioManager.STREAM_MUSIC, 0)
        mContext = context.applicationContext
        mSounds = HashMap()
    }
}