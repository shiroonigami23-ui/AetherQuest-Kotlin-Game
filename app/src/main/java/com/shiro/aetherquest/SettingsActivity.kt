package com.shiro.aetherquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val volume = SettingsManager.getMasterVolume(this)
        binding.volumeSeek.progress = (volume * 100).toInt()
        binding.musicSwitch.isChecked = SettingsManager.isMusicEnabled(this)
        binding.sfxSwitch.isChecked = SettingsManager.isSfxEnabled(this)

        binding.saveSettingsBtn.setOnClickListener {
            SettingsManager.setMasterVolume(this, binding.volumeSeek.progress / 100f)
            SettingsManager.setMusicEnabled(this, binding.musicSwitch.isChecked)
            SettingsManager.setSfxEnabled(this, binding.sfxSwitch.isChecked)
            GameAudio.refreshSettings()
            if (!binding.musicSwitch.isChecked) {
                GameAudio.stopMenuMusic()
                GameAudio.stopBattleMusic()
            }
            GameAudio.playSuccess()
            finish()
        }

        binding.backBtn.setOnClickListener {
            GameAudio.playClick()
            finish()
        }
    }
}
