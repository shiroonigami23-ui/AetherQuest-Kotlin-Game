package com.shiro.aetherquest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var session: GameSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SaveManager.load(this) ?: GameSession(GameFactory.newProfile(HeroClass.KNIGHT))
        setupClicks()
        render()
    }

    private fun setupClicks() {
        binding.nextBattleBtn.setOnClickListener {
            if (!session.inBattle) {
                BattleEngine.startBattle(session)
                render()
            }
        }

        binding.attackBtn.setOnClickListener {
            if (session.inBattle) {
                BattleEngine.playerAttack(session)
                render()
            }
        }

        binding.skillBtn.setOnClickListener {
            if (session.inBattle) {
                BattleEngine.playerSkill(session)
                render()
            }
        }

        binding.healBtn.setOnClickListener {
            if (session.inBattle) {
                BattleEngine.playerHeal(session)
                render()
            }
        }

        binding.defendBtn.setOnClickListener {
            if (session.inBattle) {
                BattleEngine.playerDefend(session)
                render()
            }
        }

        binding.saveExitBtn.setOnClickListener {
            SaveManager.save(this, session)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        SaveManager.save(this, session)
    }

    private fun render() {
        val p = session.player
        binding.headerStats.text = "${p.heroClass}  Lv.${p.level}  Stage ${p.stage}"
        binding.subStats.text = "HP ${p.hp}/${p.maxHp}  XP ${p.xp}  Coins ${p.coins}  Wins ${p.battlesWon}"
        binding.logText.text = session.lastLog
        binding.gameView.session = session

        val inBattle = session.inBattle
        binding.attackBtn.isEnabled = inBattle
        binding.skillBtn.isEnabled = inBattle
        binding.healBtn.isEnabled = inBattle
        binding.defendBtn.isEnabled = inBattle
        binding.nextBattleBtn.isEnabled = !inBattle
    }
}
