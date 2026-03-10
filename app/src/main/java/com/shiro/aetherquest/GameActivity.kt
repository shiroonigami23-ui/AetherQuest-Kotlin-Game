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
            GameAudio.playSwitch()
            if (!session.inBattle) {
                BattleEngine.startBattle(session)
                render()
            }
        }

        binding.attackBtn.setOnClickListener {
            GameAudio.playHit()
            if (session.inBattle) {
                BattleEngine.playerAttack(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.skillBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (session.inBattle) {
                BattleEngine.playerSkill(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.healBtn.setOnClickListener {
            GameAudio.playClick()
            if (session.inBattle) {
                BattleEngine.playerHeal(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.defendBtn.setOnClickListener {
            GameAudio.playClick()
            if (session.inBattle) {
                BattleEngine.playerDefend(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.buyPotionBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle) {
                CampEngine.buyPotion(session)
                render()
            }
        }

        binding.restBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle) {
                CampEngine.restAtCamp(session)
                render()
            }
        }

        binding.upgradeWeaponBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle) {
                CampEngine.upgradeWeapon(session)
                render()
            }
        }

        binding.upgradeArmorBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle) {
                CampEngine.upgradeArmor(session)
                render()
            }
        }

        binding.saveExitBtn.setOnClickListener {
            GameAudio.playSuccess()
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
        binding.subStats.text = "HP ${p.hp}/${p.maxHp}  XP ${p.xp}  Coins ${p.coins}  Gems ${p.gems}"
        binding.questText.text = "Quest: Slay ${p.questTarget} foes (${p.questKills}/${p.questTarget})  Completed: ${p.questsCompleted}  Gear W${p.weaponTier}/A${p.armorTier}"
        binding.logText.text = session.lastLog
        binding.gameView.session = session

        val inBattle = session.inBattle
        binding.attackBtn.isEnabled = inBattle
        binding.skillBtn.isEnabled = inBattle
        binding.healBtn.isEnabled = inBattle
        binding.defendBtn.isEnabled = inBattle
        binding.nextBattleBtn.isEnabled = !inBattle
        binding.buyPotionBtn.isEnabled = !inBattle
        binding.restBtn.isEnabled = !inBattle
        binding.upgradeWeaponBtn.isEnabled = !inBattle
        binding.upgradeArmorBtn.isEnabled = !inBattle
    }

    private fun playPostTurnAudio() {
        val msg = session.lastLog.lowercase()
        if (msg.contains("victory")) {
            GameAudio.playSuccess()
        }
        if (msg.contains("defeated")) {
            GameAudio.playSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        GameAudio.startBattleMusic(this)
    }

    override fun onStop() {
        super.onStop()
        GameAudio.stopBattleMusic()
    }
}
