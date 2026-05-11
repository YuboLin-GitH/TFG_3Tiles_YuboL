package com.example.tfg_3tiles_yubol.utils



import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.tfg_3tiles_yubol.R

class SoundManager(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()
        )
        .build()

    private val clickSoundId: Int
    private val matchSoundId: Int
    private val loadedSounds = mutableSetOf<Int>() // 记录已加载完成的音效

    var sfxVolume = 1f
        private set
    var bgmVolume = 1f
        private set

    init {
        // 设置加载完成监听器
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) { // 0 = 成功
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
        if (clickSoundId in loadedSounds) { // 只有加载完才播放
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
    }

    fun setBgmVolume(volume: Float) {
        bgmVolume = volume.coerceIn(0f, 1f)
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
