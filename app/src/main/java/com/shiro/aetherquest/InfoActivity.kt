package com.shiro.aetherquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.legalTabBtn.setOnClickListener { loadText(R.raw.legal_notice) }
        binding.privacyTabBtn.setOnClickListener { loadText(R.raw.privacy_policy) }
        binding.aboutTabBtn.setOnClickListener { loadText(R.raw.about_game) }
        binding.infoBackBtn.setOnClickListener { finish() }

        loadText(R.raw.legal_notice)
    }

    private fun loadText(rawId: Int) {
        val text = resources.openRawResource(rawId).bufferedReader().use { it.readText() }
        binding.infoText.text = text
    }
}
