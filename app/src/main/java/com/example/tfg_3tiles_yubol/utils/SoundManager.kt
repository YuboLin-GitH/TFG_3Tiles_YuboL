package com.example.tfg_3tiles_yubol.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.tfg_3tiles_yubol.R
import androidx.core.content.edit

class SoundManager(context: Context) {

    private val prefs = context.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()
        )
        .build()

    private val clickSoundId: Int
    private val matchSoundId: Int
    private val loadedSounds = mutableSetOf<Int>()

    var sfxVolume = prefs.getFloat("sfx_volume", 1f)
        private set
    var bgmVolume = prefs.getFloat("bgm_volume", 1f)
        private set

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        clickSoundId = soundPool.load(context, R.raw.click, 1)
        matchSoundId = soundPool.load(context, R.raw.match_3, 1)
    }

    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.lily_paddling_down_the_stream)

    init {
        mediaPlayer?.isLooping = true
    }

    fun playClick() {
        if (clickSoundId in loadedSounds) {
            soundPool.play(clickSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
        }
    }

    fun playMatch() {
        if (matchSoundId in loadedSounds) {
            soundPool.play(matchSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
        }
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        prefs.edit { putFloat("sfx_volume", sfxVolume) }
    }

    fun setBgmVolume(volume: Float) {
        bgmVolume = volume.coerceIn(0f, 1f)
        prefs.edit { putFloat("bgm_volume", bgmVolume) }
        mediaPlayer?.setVolume(bgmVolume, bgmVolume)
    }

    fun startBGM() {
        mediaPlayer?.let { if (!it.isPlaying) it.start() }
    }

    fun pauseBGM() {
        mediaPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun release() {
        soundPool.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
