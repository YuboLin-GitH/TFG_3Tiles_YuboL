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

    private val idSonidoClick: Int
    private val idSonidoCoincidencia: Int
    private val sonidosCargados = mutableSetOf<Int>()

    var volumenEfectos = prefs.getFloat("sfx_volume", 1f)
        private set
    var volumenMusica = prefs.getFloat("bgm_volume", 1f)
        private set

    init {
        soundPool.setOnLoadCompleteListener { _, idMuestra, status ->
            if (status == 0) {
                sonidosCargados.add(idMuestra)
            }
        }

        idSonidoClick = soundPool.load(context, R.raw.click, 1)
        idSonidoCoincidencia = soundPool.load(context, R.raw.match_3, 1)
    }

    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.lily_paddling_down_the_stream)

    init {
        mediaPlayer?.isLooping = true
    }

    fun reproducirClick() {
        if (idSonidoClick in sonidosCargados) {
            soundPool.play(idSonidoClick, volumenEfectos, volumenEfectos, 1, 0, 1f)
        }
    }

    fun reproducirCoincidencia() {
        if (idSonidoCoincidencia in sonidosCargados) {
            soundPool.play(idSonidoCoincidencia, volumenEfectos, volumenEfectos, 1, 0, 1f)
        }
    }

    fun cambiarVolumenEfectos(volume: Float) {
        volumenEfectos = volume.coerceIn(0f, 1f)
        prefs.edit { putFloat("sfx_volume", volumenEfectos) }
    }

    fun cambiarVolumenMusica(volume: Float) {
        volumenMusica = volume.coerceIn(0f, 1f)
        prefs.edit { putFloat("bgm_volume", volumenMusica) }
        mediaPlayer?.setVolume(volumenMusica, volumenMusica)
    }

    fun iniciarMusicaFondo() {
        mediaPlayer?.let { if (!it.isPlaying) it.start() }
    }

    fun pausarMusicaFondo() {
        mediaPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun liberar() {
        soundPool.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
