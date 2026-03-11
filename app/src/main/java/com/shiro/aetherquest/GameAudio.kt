package com.shiro.aetherquest

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object GameAudio {
    private var menuMusic: MediaPlayer? = null
    private var battleMusic: MediaPlayer? = null
    private var exploreMusic: MediaPlayer? = null
    private var storyMusic: MediaPlayer? = null
    private var appContext: Context? = null
    private var soundPool: SoundPool? = null
    private var sfxClick = 0
    private var sfxHit = 0
    private var sfxSuccess = 0
    private var sfxSwitch = 0
    private var sfxError = 0
    private var sfxSpellDark = 0
    private var sfxSpellLight = 0
    private var sfxFootstep = 0
    private var sfxLoot = 0
    private var sfxTavern = 0

    fun init(context: Context) {
        appContext = context.applicationContext
        if (soundPool != null) return
        val attrs = runCatching {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }.getOrNull() ?: return
        soundPool = runCatching {
            SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(6)
                .build()
        }.getOrNull()
        val sp = soundPool ?: return
        sfxClick = safeLoadSfx(sp, context, R.raw.sfx_ui_click)
        sfxHit = safeLoadSfx(sp, context, R.raw.sfx_hit)
        sfxSuccess = safeLoadSfx(sp, context, R.raw.sfx_ui_success)
        sfxSwitch = safeLoadSfx(sp, context, R.raw.sfx_switch)
        sfxError = safeLoadSfx(sp, context, R.raw.sfx_ui_error)
        sfxSpellDark = safeLoadSfx(sp, context, R.raw.sfx_spell_dark)
        sfxSpellLight = safeLoadSfx(sp, context, R.raw.sfx_spell_light)
        sfxFootstep = safeLoadSfx(sp, context, R.raw.sfx_footstep)
        sfxLoot = safeLoadSfx(sp, context, R.raw.sfx_loot)
        sfxTavern = safeLoadSfx(sp, context, R.raw.sfx_tavern)
    }

    fun refreshSettings() {
        val ctx = appContext ?: return
        val volume = SettingsManager.getMasterVolume(ctx)
        runCatching { menuMusic?.setVolume(volume * 0.45f, volume * 0.45f) }
        runCatching { battleMusic?.setVolume(volume * 0.55f, volume * 0.55f) }
        runCatching { exploreMusic?.setVolume(volume * 0.5f, volume * 0.5f) }
        runCatching { storyMusic?.setVolume(volume * 0.5f, volume * 0.5f) }
    }

    fun startMenuMusic(context: Context) {
        if (menuMusic == null) {
            menuMusic = createLoopPlayer(context, R.raw.bgm_menu)
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
        runCatching {
            if (menuMusic?.isPlaying != true) menuMusic?.start()
        }
    }

    fun stopMenuMusic() {
        runCatching { menuMusic?.pause() }
    }

    fun startBattleMusic(context: Context) {
        if (battleMusic == null) {
            battleMusic = createLoopPlayer(context, R.raw.bgm_battle)
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
        runCatching {
            if (battleMusic?.isPlaying != true) battleMusic?.start()
        }
    }

    fun startExploreMusic(context: Context) {
        if (exploreMusic == null) {
            exploreMusic = createLoopPlayer(context, R.raw.bgm_explore)
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
        runCatching {
            if (exploreMusic?.isPlaying != true) exploreMusic?.start()
        }
    }

    fun startStoryMusic(context: Context) {
        if (storyMusic == null) {
            storyMusic = createLoopPlayer(context, R.raw.bgm_story)
        }
        if (!SettingsManager.isMusicEnabled(context)) return
        refreshSettings()
        runCatching {
            if (storyMusic?.isPlaying != true) storyMusic?.start()
        }
    }

    fun stopAllNonMenuMusic() {
        runCatching { battleMusic?.pause() }
        runCatching { exploreMusic?.pause() }
        runCatching { storyMusic?.pause() }
    }

    fun stopBattleMusic() {
        runCatching { battleMusic?.pause() }
    }

    fun playClick() = playSfx(sfxClick)
    fun playHit() = playSfx(sfxHit)
    fun playSuccess() = playSfx(sfxSuccess)
    fun playSwitch() = playSfx(sfxSwitch)
    fun playError() = playSfx(sfxError)
    fun playSpellDark() = playSfx(sfxSpellDark)
    fun playSpellLight() = playSfx(sfxSpellLight)
    fun playFootstep() = playSfx(sfxFootstep)
    fun playLoot() = playSfx(sfxLoot)
    fun playTavern() = playSfx(sfxTavern)

    private fun playSfx(id: Int) {
        val ctx = appContext ?: return
        if (id <= 0) return
        if (!SettingsManager.isSfxEnabled(ctx)) return
        val volume = SettingsManager.getMasterVolume(ctx)
        runCatching { soundPool?.play(id, volume, volume, 1, 0, 1f) }
    }

    fun release() {
        runCatching { menuMusic?.release() }
        runCatching { battleMusic?.release() }
        runCatching { exploreMusic?.release() }
        runCatching { storyMusic?.release() }
        menuMusic = null
        battleMusic = null
        exploreMusic = null
        storyMusic = null
        runCatching { soundPool?.release() }
        soundPool = null
    }

    private fun createLoopPlayer(context: Context, resId: Int): MediaPlayer? {
        val player = runCatching { MediaPlayer.create(context, resId) }.getOrNull() ?: return null
        runCatching {
            player.isLooping = true
            player.setOnErrorListener { mp, _, _ ->
                runCatching { mp.reset() }
                true
            }
        }
        return player
    }

    private fun safeLoadSfx(pool: SoundPool, context: Context, resId: Int): Int {
        return runCatching { pool.load(context, resId, 1) }.getOrDefault(0)
    }
}
