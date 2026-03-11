package com.shiro.aetherquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shiro.aetherquest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val slotLabels = arrayOf("Slot 1", "Slot 2", "Slot 3")

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
        binding.slotPicker.setSimpleItems(slotLabels)
        binding.slotPicker.setText(slotLabels[SaveManager.getActiveSlot(this) - 1], false)
        refreshSlotStatus()

        binding.slotPicker.setOnItemClickListener { _, _, position, _ ->
            SaveManager.setActiveSlot(this, position + 1)
            refreshSlotStatus()
        }

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
            val selectedSlot = slotFromPicker()
            SaveManager.setActiveSlot(this, selectedSlot)
            val session = GameFactory.newSession(selectedClass, selectedDifficulty)
            SaveManager.save(this, session)
            refreshSlotStatus()
            openGame()
        }

        binding.continueBtn.setOnClickListener {
            GameAudio.playClick()
            SaveManager.setActiveSlot(this, slotFromPicker())
            refreshSlotStatus()
            if (SaveManager.load(this) == null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("No Save Found")
                    .setMessage("No save in ${slotLabels[SaveManager.getActiveSlot(this) - 1]}. Start a new quest first.")
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
        refreshSlotStatus()
        GameAudio.refreshSettings()
        GameAudio.startMenuMusic(this)
    }

    override fun onPause() {
        super.onPause()
        GameAudio.stopMenuMusic()
    }

    private fun slotFromPicker(): Int {
        val text = binding.slotPicker.text?.toString()?.trim() ?: ""
        return when (text) {
            "Slot 2" -> 2
            "Slot 3" -> 3
            else -> 1
        }
    }

    private fun refreshSlotStatus() {
        val slot = SaveManager.getActiveSlot(this)
        binding.slotPicker.setText(slotLabels[slot - 1], false)
        val hasSave = SaveManager.hasSaveInSlot(this, slot)
        binding.newQuestBtn.text = "Start New Quest (${slotLabels[slot - 1]})"
        binding.continueBtn.text = "Continue (${slotLabels[slot - 1]})"
        binding.slotStatusText.text = if (hasSave) {
            "Status: ${slotLabels[slot - 1]} contains saved progress."
        } else {
            "Status: ${slotLabels[slot - 1]} is empty."
        }
    }
}
