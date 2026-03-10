package com.shiro.aetherquest

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object GameAudio {
    private var menuMusic: MediaPlayer? = null
    private var battleMusic: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var sfxClick = 0
    private var sfxHit = 0
    private var sfxSuccess = 0
    private var sfxSwitch = 0

    fun init(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attrs)
            .setMaxStreams(6)
            .build()
        sfxClick = soundPool!!.load(context, R.raw.sfx_ui_click, 1)
        sfxHit = soundPool!!.load(context, R.raw.sfx_hit, 1)
        sfxSuccess = soundPool!!.load(context, R.raw.sfx_ui_success, 1)
        sfxSwitch = soundPool!!.load(context, R.raw.sfx_switch, 1)
    }

    fun startMenuMusic(context: Context) {
        if (menuMusic == null) {
            menuMusic = MediaPlayer.create(context, R.raw.bgm_menu).apply {
                isLooping = true
                setVolume(0.35f, 0.35f)
            }
        }
        if (menuMusic?.isPlaying != true) {
            menuMusic?.start()
        }
    }

    fun stopMenuMusic() {
        menuMusic?.pause()
    }

    fun startBattleMusic(context: Context) {
        if (battleMusic == null) {
            battleMusic = MediaPlayer.create(context, R.raw.bgm_battle).apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
            }
        }
        if (battleMusic?.isPlaying != true) {
            battleMusic?.start()
        }
    }

    fun stopBattleMusic() {
        battleMusic?.pause()
    }

    fun playClick() = soundPool?.play(sfxClick, 1f, 1f, 1, 0, 1f)
    fun playHit() = soundPool?.play(sfxHit, 1f, 1f, 1, 0, 1f)
    fun playSuccess() = soundPool?.play(sfxSuccess, 1f, 1f, 1, 0, 1f)
    fun playSwitch() = soundPool?.play(sfxSwitch, 1f, 1f, 1, 0, 1f)

    fun release() {
        menuMusic?.release()
        battleMusic?.release()
        menuMusic = null
        battleMusic = null
        soundPool?.release()
        soundPool = null
    }
}
