package com.shiro.aetherquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread {
            runCatching {
                GameAudio.init(this)
                AssetRepository.preload(this) { progress, label ->
                    runOnUiThread {
                        binding.loadingProgress.progress = progress
                        binding.loadingText.text = "Loading $label... $progress%"
                    }
                }
            }.onFailure {
                runOnUiThread {
                    binding.loadingText.text = "Loading fallback assets..."
                }
            }
            runOnUiThread {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
        }.start()
    }
}
