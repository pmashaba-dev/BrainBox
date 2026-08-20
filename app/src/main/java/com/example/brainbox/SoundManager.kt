// File: app/src/main/java/com/example/brainbox/SoundManager.kt

package com.example.brainbox

import android.content.Context
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes

private const val TAG = "SoundManager"

/**
 * Manages the loading and playback of sound effects using SoundPool.
 *
 * @param context The application context.
 */
class SoundManager(private val context: Context) {

    // SoundPool is an API for playing short sound effects
    private var soundPool: SoundPool? = null
    // A map to store the loaded sound IDs
    private val soundMap = mutableMapOf<Int, Int>()

    init {
        // Build the SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Max number of simultaneous streams
            .build()
        Log.d(TAG, "SoundPool initialized.")
    }

    /**
     * Loads a sound file from the raw resource directory.
     *
     * @param resId The resource ID of the sound file (e.g., R.raw.button_click).
     * @return The ID of the loaded sound.
     */
    fun loadSound(@RawRes resId: Int): Int {
        // Load the sound and store its ID in the map
        val soundId = soundPool?.load(context, resId, 1) ?: 0
        soundMap[resId] = soundId
        Log.d(TAG, "Sound with resource ID $resId loaded with SoundPool ID: $soundId")
        return soundId
    }

    /**
     * Plays a loaded sound.
     *
     * @param resId The resource ID of the sound to play.
     */
    fun playSound(@RawRes resId: Int) {
        val soundId = soundMap[resId]
        if (soundId != null) {
            // Play the sound with standard settings
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
            Log.d(TAG, "Playing sound with ID: $soundId")
        } else {
            Log.e(TAG, "Sound not loaded. Did you call loadSound() first? Resource ID: $resId")
        }
    }

    /**
     * Releases the SoundPool resources. This should be called when the app is no longer using it.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        Log.d(TAG, "SoundPool released.")
    }
}