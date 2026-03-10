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
        NarrativeEngine.maybeTriggerStory(session)
        setupClicks()
        render()
    }

    private fun setupClicks() {
        binding.nextBattleBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver && session.pendingStoryEvent.isBlank()) {
                BattleEngine.startBattle(session)
                render()
            }
        }

        binding.attackBtn.setOnClickListener {
            GameAudio.playHit()
            if (session.inBattle && !session.gameOver) {
                BattleEngine.playerAttack(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.skillBtn.setOnClickListener {
            if (session.inBattle && !session.gameOver) {
                when (session.player.heroClass) {
                    HeroClass.MYSTIC -> GameAudio.playSpellDark()
                    HeroClass.RANGER -> GameAudio.playSpellLight()
                    HeroClass.KNIGHT -> GameAudio.playSwitch()
                }
                BattleEngine.playerSkill(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.healBtn.setOnClickListener {
            GameAudio.playClick()
            if (session.inBattle && !session.gameOver) {
                BattleEngine.playerHeal(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.defendBtn.setOnClickListener {
            GameAudio.playClick()
            if (session.inBattle && !session.gameOver) {
                BattleEngine.playerDefend(session)
                playPostTurnAudio()
                render()
            }
        }

        binding.buyPotionBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.buyPotion(session)
                render()
            }
        }

        binding.restBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.restAtCamp(session)
                render()
            }
        }

        binding.upgradeWeaponBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.upgradeWeapon(session)
                render()
            }
        }

        binding.upgradeArmorBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.upgradeArmor(session)
                render()
            }
        }

        binding.choiceABtn.setOnClickListener {
            if (session.pendingStoryEvent.isNotBlank() && !session.gameOver) {
                GameAudio.playSuccess()
                NarrativeEngine.applyChoice(session, chooseA = true)
                render()
            } else {
                GameAudio.playError()
            }
        }

        binding.choiceBBtn.setOnClickListener {
            if (session.pendingStoryEvent.isNotBlank() && !session.gameOver) {
                GameAudio.playSwitch()
                NarrativeEngine.applyChoice(session, chooseA = false)
                render()
            } else {
                GameAudio.playError()
            }
        }

        binding.saveExitBtn.setOnClickListener {
            GameAudio.playSuccess()
            SaveManager.save(this, session)
            finish()
        }

        binding.newLifeBtn.setOnClickListener {
            GameAudio.playSwitch()
            session = GameSession(GameFactory.newProfile(session.player.heroClass))
            SaveManager.save(this, session)
            render()
        }
    }

    override fun onPause() {
        super.onPause()
        SaveManager.save(this, session)
    }

    private fun render() {
        val p = session.player
        val region = NarrativeEngine.regionName(p.stage)
        binding.headerStats.text = "${p.heroClass}  Lv.${p.level}  Stage ${p.stage}  [${region}]"
        binding.subStats.text = "HP ${p.hp}/${p.maxHp}  XP ${p.xp}  Coins ${p.coins}  Gems ${p.gems}"
        binding.questText.text = "Chapter ${p.chapter} | Quest ${p.questKills}/${p.questTarget} | Completed ${p.questsCompleted} | Lives ${p.lives} | Nyra ${p.affinityNyra} / Crown ${p.affinityCrown}"
        binding.logText.text = if (session.gameOver) {
            "Ending: ${session.ending}\n${NarrativeEngine.endingText(session.ending)}"
        } else {
            session.lastLog
        }
        binding.gameView.session = session
        NarrativeEngine.maybeTriggerStory(session)
        binding.storyChoiceRow.visibility = if (session.pendingStoryEvent.isNotBlank()) android.view.View.VISIBLE else android.view.View.GONE
        binding.storyPromptText.text = session.storyPrompt
        binding.choiceABtn.text = session.choiceA
        binding.choiceBBtn.text = session.choiceB

        val inBattle = session.inBattle
        val locked = session.gameOver || session.pendingStoryEvent.isNotBlank()
        binding.attackBtn.isEnabled = inBattle
        binding.skillBtn.isEnabled = inBattle
        binding.healBtn.isEnabled = inBattle
        binding.defendBtn.isEnabled = inBattle
        binding.nextBattleBtn.isEnabled = !inBattle && !locked
        binding.buyPotionBtn.isEnabled = !inBattle && !locked
        binding.restBtn.isEnabled = !inBattle && !locked
        binding.upgradeWeaponBtn.isEnabled = !inBattle && !locked
        binding.upgradeArmorBtn.isEnabled = !inBattle && !locked
        binding.choiceABtn.isEnabled = session.pendingStoryEvent.isNotBlank() && !session.gameOver
        binding.choiceBBtn.isEnabled = session.pendingStoryEvent.isNotBlank() && !session.gameOver
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
        GameAudio.refreshSettings()
        GameAudio.startBattleMusic(this)
    }

    override fun onStop() {
        super.onStop()
        GameAudio.stopBattleMusic()
    }
}
