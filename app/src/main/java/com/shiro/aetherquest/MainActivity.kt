package com.shiro.aetherquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shiro.aetherquest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GameAudio.init(this)

        val classes = HeroClass.entries
        binding.classPicker.setSimpleItems(classes.map { it.name.lowercase().replaceFirstChar(Char::uppercase) }.toTypedArray())
        binding.classPicker.setText("Knight", false)
        val difficulties = DifficultyMode.entries.map { it.name.lowercase().replaceFirstChar(Char::uppercase) }.toTypedArray()
        binding.difficultyPicker.setSimpleItems(difficulties)
        binding.difficultyPicker.setText("Easy", false)

        binding.newQuestBtn.setOnClickListener {
            GameAudio.playClick()
            val selectedClass = when (binding.classPicker.text?.toString()?.trim()?.lowercase()) {
                "ranger" -> HeroClass.RANGER
                "mystic" -> HeroClass.MYSTIC
                else -> HeroClass.KNIGHT
            }
            val selectedDifficulty = when (binding.difficultyPicker.text?.toString()?.trim()?.lowercase()) {
                "story" -> DifficultyMode.STORY
                "hard" -> DifficultyMode.HARD
                "extreme" -> DifficultyMode.EXTREME
                else -> DifficultyMode.EASY
            }
            val session = GameFactory.newSession(selectedClass, selectedDifficulty)
            SaveManager.save(this, session)
            openGame()
        }

        binding.continueBtn.setOnClickListener {
            GameAudio.playClick()
            if (SaveManager.load(this) == null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("No Save Found")
                    .setMessage("Start a new quest first.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                openGame()
            }
        }

        binding.settingsBtn.setOnClickListener {
            GameAudio.playSwitch()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.legalBtn.setOnClickListener {
            GameAudio.playSwitch()
            startActivity(Intent(this, InfoActivity::class.java))
        }
    }

    private fun openGame() {
        GameAudio.playSwitch()
        startActivity(Intent(this, GameActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        GameAudio.refreshSettings()
        GameAudio.startMenuMusic(this)
    }

    override fun onPause() {
        super.onPause()
        GameAudio.stopMenuMusic()
    }
}
