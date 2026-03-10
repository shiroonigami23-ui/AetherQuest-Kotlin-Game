package com.shiro.aetherquest

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var session: GameSession
    private var lastPromptVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SaveManager.load(this) ?: GameFactory.newSession(HeroClass.KNIGHT, DifficultyMode.EASY)
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
        binding.healBtn.setOnLongClickListener {
            GameAudio.playSwitch()
            if (session.inBattle && !session.gameOver) {
                BattleEngine.playerUseBomb(session)
                playPostTurnAudio()
                render()
            }
            true
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

        binding.buyElixirBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.buyElixir(session)
                render()
            }
        }

        binding.buyBombBtn.setOnClickListener {
            GameAudio.playClick()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.buyBomb(session)
                render()
            }
        }

        binding.openChestBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.openChest(session)
                render()
            }
        }

        binding.secretBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.exploreSecret(session)
                render()
            }
        }

        binding.craftAccessoryBtn.setOnClickListener {
            GameAudio.playSwitch()
            if (!session.inBattle && !session.gameOver) {
                CampEngine.craftAccessory(session)
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
            session = GameFactory.newSession(session.player.heroClass, session.difficultyMode)
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
        binding.headerStats.text = "${p.heroClass}  ${session.difficultyMode}  Lv.${p.level}  Stage ${p.stage}  [$region]"
        binding.subStats.text = "HP ${p.hp}/${p.maxHp} | XP ${p.xp} | Coins ${p.coins} | Gems ${p.gems} | Potions ${p.potions} | Elixirs ${p.elixirs} | Bombs ${p.bombs}"
        binding.questText.text = "Chapter ${p.chapter} | Quest ${p.questKills}/${p.questTarget} | Completed ${p.questsCompleted} | Lives ${p.lives} | Keys ${p.keys} | Relics ${p.relicShards} | Secrets ${session.discoveredSecrets}"
        binding.logText.text = if (session.gameOver) {
            "Ending: ${session.ending}\n${NarrativeEngine.endingText(session.ending, p)}"
        } else {
            session.lastLog
        }
        binding.romanceInfoText.text = companionStatus(p)
        binding.romancePortrait.setImageResource(companionPortraitRes(p))
        binding.gameView.session = session
        NarrativeEngine.maybeTriggerStory(session)
        val promptVisible = session.pendingStoryEvent.isNotBlank()
        binding.storyChoiceRow.visibility = if (promptVisible) View.VISIBLE else View.GONE
        if (promptVisible && !lastPromptVisible) {
            animateStoryPrompt()
        }
        lastPromptVisible = promptVisible
        binding.storyPromptText.text = session.storyPrompt
        binding.choiceABtn.text = session.choiceA
        binding.choiceBBtn.text = session.choiceB

        val inBattle = session.inBattle
        val locked = session.gameOver || session.pendingStoryEvent.isNotBlank()
        binding.attackBtn.isEnabled = inBattle && !session.gameOver
        binding.skillBtn.isEnabled = inBattle && !session.gameOver
        binding.healBtn.isEnabled = inBattle && !session.gameOver
        binding.defendBtn.isEnabled = inBattle && !session.gameOver
        binding.nextBattleBtn.isEnabled = !inBattle && !locked
        binding.buyPotionBtn.isEnabled = !inBattle && !locked
        binding.restBtn.isEnabled = !inBattle && !locked
        binding.upgradeWeaponBtn.isEnabled = !inBattle && !locked
        binding.upgradeArmorBtn.isEnabled = !inBattle && !locked
        binding.buyElixirBtn.isEnabled = !inBattle && !locked
        binding.buyBombBtn.isEnabled = !inBattle && !locked
        binding.openChestBtn.isEnabled = !inBattle && !locked
        binding.secretBtn.isEnabled = !inBattle && !locked
        binding.craftAccessoryBtn.isEnabled = !inBattle && !locked
        binding.choiceABtn.isEnabled = session.pendingStoryEvent.isNotBlank() && !session.gameOver
        binding.choiceBBtn.isEnabled = session.pendingStoryEvent.isNotBlank() && !session.gameOver
        animatePortraitPulse()
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

    private fun animateStoryPrompt() {
        binding.storyChoiceRow.alpha = 0f
        binding.storyChoiceRow.translationY = 20f
        binding.storyChoiceRow.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(280L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animatePortraitPulse() {
        ObjectAnimator.ofFloat(binding.romancePortrait, View.SCALE_X, 1f, 1.035f, 1f).apply {
            duration = 620L
            start()
        }
        ObjectAnimator.ofFloat(binding.romancePortrait, View.SCALE_Y, 1f, 1.035f, 1f).apply {
            duration = 620L
            start()
        }
    }

    private fun companionStatus(p: PlayerProfile): String {
        val names = NarrativeEngine.topRomances(p)
        val route = when (p.relationshipStyle) {
            RelationshipStyle.HAREM -> "Open route"
            RelationshipStyle.THROUPLE -> "Throuple route"
            RelationshipStyle.SINGLE -> "Single route"
            else -> "Undecided"
        }
        val lead = names.firstOrNull() ?: "No companion bonded yet"
        return "Companion: $lead | Route: $route | Arc(S/W/E): ${p.strengthArc}/${p.wisdomArc}/${p.empathyArc}\nGear: ${p.weaponName}, ${p.armorName}, ${p.accessoryName}"
    }

    private fun companionPortraitRes(p: PlayerProfile): Int {
        val top = NarrativeEngine.topRomances(p).firstOrNull()
        return when (top) {
            "Nyra" -> R.drawable.portrait_nyra
            "Lyra" -> R.drawable.portrait_lyra
            "Sera" -> R.drawable.portrait_sera
            "Mira" -> R.drawable.portrait_mira
            "Kaela" -> R.drawable.portrait_kaela
            else -> R.drawable.portrait_party
        }
    }
}
