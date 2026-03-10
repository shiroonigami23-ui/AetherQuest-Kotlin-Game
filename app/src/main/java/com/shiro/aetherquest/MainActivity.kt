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

        val classes = HeroClass.entries
        binding.classPicker.setSimpleItems(classes.map { it.name.lowercase().replaceFirstChar(Char::uppercase) }.toTypedArray())
        binding.classPicker.setText("Knight", false)

        binding.newQuestBtn.setOnClickListener {
            val selectedClass = when (binding.classPicker.text?.toString()?.trim()?.lowercase()) {
                "ranger" -> HeroClass.RANGER
                "mystic" -> HeroClass.MYSTIC
                else -> HeroClass.KNIGHT
            }
            val session = GameSession(GameFactory.newProfile(selectedClass))
            SaveManager.save(this, session)
            openGame()
        }

        binding.continueBtn.setOnClickListener {
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
    }

    private fun openGame() {
        startActivity(Intent(this, GameActivity::class.java))
    }
}
