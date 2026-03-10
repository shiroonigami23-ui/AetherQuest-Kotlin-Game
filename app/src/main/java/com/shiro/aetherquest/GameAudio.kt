package com.shiro.aetherquest

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object GameAudio {
    private var menuMusic: MediaPlayer? = null
    private var battleMusic: MediaPlayer? = null
    private var appContext: Context? = null
    private var soundPool: SoundPool? = null
    private var sfxClick = 0
    private var sfxHit = 0
    private var sfxSuccess = 0
    private var sfxSwitch = 0
    private var sfxError = 0
    private var sfxSpellDark = 0
    private var sfxSpellLight = 0

    fun init(context: Context) {
        appContext = context.applicationContext
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
        sfxError = soundPool!!.load(context, R.raw.sfx_ui_error, 1)
        sfxSpellDark = soundPool!!.load(context, R.raw.sfx_spell_dark, 1)
        sfxSpellLight = soundPool!!.load(context, R.raw.sfx_spell_light, 1)
    }

    fun refreshSettings() {
        val ctx = appContext ?: return
        val volume = SettingsManager.getMasterVolume(ctx)
        menuMusic?.setVolume(volume * 0.45f, volume * 0.45f)
        battleMusic?.setVolume(volume * 0.55f, volume * 0.55f)
    }

    fun startMenuMusic(context: Context) {
        if (menuMusic == null) {
            menuMusic = MediaPlayer.create(context, R.raw.bgm_menu).apply {
                isLooping = true
            }
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
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
            }
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
        if (battleMusic?.isPlaying != true) {
            battleMusic?.start()
        }
    }

    fun stopBattleMusic() {
        battleMusic?.pause()
    }

    fun playClick() = playSfx(sfxClick)
    fun playHit() = playSfx(sfxHit)
    fun playSuccess() = playSfx(sfxSuccess)
    fun playSwitch() = playSfx(sfxSwitch)
    fun playError() = playSfx(sfxError)
    fun playSpellDark() = playSfx(sfxSpellDark)
    fun playSpellLight() = playSfx(sfxSpellLight)

    private fun playSfx(id: Int) {
        val ctx = appContext ?: return
        if (!SettingsManager.isSfxEnabled(ctx)) return
        val volume = SettingsManager.getMasterVolume(ctx)
        soundPool?.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() {
        menuMusic?.release()
        battleMusic?.release()
        menuMusic = null
        battleMusic = null
        soundPool?.release()
        soundPool = null
    }
}
