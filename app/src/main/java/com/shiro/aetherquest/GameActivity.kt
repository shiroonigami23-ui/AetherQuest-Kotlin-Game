package com.shiro.aetherquest

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.shiro.aetherquest.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private lateinit var session: GameSession
    private var lastPromptVisible = false
    private var lastTypedLog = ""
    private val typewriterHandler = Handler(Looper.getMainLooper())
    private var typewriterRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SaveManager.load(this) ?: GameFactory.newSession(HeroClass.KNIGHT, DifficultyMode.EASY)
        NarrativeEngine.maybeTriggerStory(session)
        setupRendererCallbacks()
        setupClicks()
        render()
    }

    private fun setupRendererCallbacks() {
        binding.gameView.onEncounterRequested = { name ->
            if (!session.inBattle && !session.gameOver && session.pendingStoryEvent.isBlank()) {
                session.lastLog = "Encountered $name in the wild."
                BattleEngine.startBattle(session, name)
                render()
            }
        }
        binding.gameView.onPoiClaimed = { label, coins, relics ->
            session.player.coins += coins
            session.player.relicShards += relics
            session.player.crystalShards += 1
            if (session.player.level % 2 == 0) session.player.weaponCores += 1 else session.player.armorPlates += 1
            GameAudio.playLoot()
            session.lastLog = "$label claimed: +$coins coins, +$relics relic shard(s), +1 crystal, +1 forge component."
            render()
        }
        binding.gameView.onPotBroken = { log ->
            GameAudio.playLoot()
            session.lastLog = log
            render()
        }
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
                GameAudio.playTavern()
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

        setupMoveButton(binding.moveLeftBtn, -1f, 0f)
        setupMoveButton(binding.moveRightBtn, 1f, 0f)
        setupMoveButton(binding.moveUpBtn, 0f, -1f)
        setupMoveButton(binding.moveDownBtn, 0f, 1f)

        binding.interactBtn.setOnClickListener {
            if (!session.inBattle && !session.gameOver) {
                binding.gameView.interact()
            }
        }

        binding.cameraModeBtn.setOnClickListener {
            val mode = binding.gameView.cycleCameraMode()
            session.cameraMode = mode
            session.lastLog = "Camera switched to $mode."
            render()
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
        val objective = NarrativeEngine.stageObjective(p.stage)
        val enemyIntentText = if (session.inBattle) " | Enemy Intent ${session.enemyIntent}" else ""
        val buffs = buildList {
            if (session.furyBuffTurns > 0) add("Fury:${session.furyBuffTurns}")
            if (session.stoneSkinBuffTurns > 0) add("Stone:${session.stoneSkinBuffTurns}")
            if (session.fortuneBuffTurns > 0) add("Fortune:${session.fortuneBuffTurns}")
        }.joinToString(" ")
        binding.headerStats.text = "${p.heroName} (${p.heroClass})  ${session.difficultyMode}  Lv.${p.level}  Stage ${p.stage}  [$region]${if (buffs.isNotBlank()) "  Buffs: $buffs" else ""}"
        binding.subStats.text = "HP ${p.hp}/${p.maxHp} | XP ${p.xp} | Coins ${p.coins} | Gems ${p.gems} | Potions ${p.potions} | Elixirs ${p.elixirs} | Bombs ${p.bombs} | ${p.weaponTrait} M${p.weaponMastery}$enemyIntentText"
        binding.questText.text = "Chapter ${p.chapter} | Quest ${p.questKills}/${p.questTarget} | Completed ${p.questsCompleted} | Lives ${p.lives} | Keys ${p.keys} | Relics ${p.relicShards} | Crystals ${p.crystalShards} | Cores ${p.weaponCores} | Plates ${p.armorPlates} | Secrets ${session.discoveredSecrets}\nObjective: $objective"
        binding.logText.text = if (session.gameOver) {
            "Ending: ${session.ending}\n${NarrativeEngine.endingText(session.ending, p)}"
        } else {
            session.lastLog
        }
        applyTypewriter(binding.logText.text.toString())
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
        val inStory = session.pendingStoryEvent.isNotBlank() && !session.gameOver
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
        val canMove = !session.inBattle && !locked
        binding.moveLeftBtn.isEnabled = canMove
        binding.moveRightBtn.isEnabled = canMove
        binding.moveUpBtn.isEnabled = canMove
        binding.moveDownBtn.isEnabled = canMove
        binding.interactBtn.isEnabled = canMove
        binding.cameraModeBtn.isEnabled = canMove
        binding.cameraModeBtn.text = when (session.cameraMode) {
            CameraMode.TOP_DOWN -> "Top"
            CameraMode.THIRD_PERSON -> "3rd"
            CameraMode.FIRST_PERSON -> "1st"
        }
        binding.combatControlsRow.visibility = if (inBattle) View.VISIBLE else View.GONE
        binding.exploreControlsRow.visibility = if (!inBattle && !inStory && !session.gameOver) View.VISIBLE else View.GONE
        val showCamp = !inBattle && !inStory && !session.gameOver
        binding.campControlsRow1.visibility = if (showCamp) View.VISIBLE else View.GONE
        binding.campControlsRow2.visibility = if (showCamp) View.VISIBLE else View.GONE
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
        when {
            session.pendingStoryEvent.isNotBlank() -> {
                GameAudio.stopAllNonMenuMusic()
                GameAudio.startStoryMusic(this)
            }
            session.inBattle -> {
                GameAudio.stopAllNonMenuMusic()
                GameAudio.startBattleMusic(this)
            }
            else -> {
                GameAudio.stopAllNonMenuMusic()
                GameAudio.startExploreMusic(this)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopTypewriter()
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
        val lead = names.firstOrNull()
        val affection = lead?.let { DialogueEngine.romanceDevotionLine(it) } ?: "The camp still whispers about the commander called ${p.heroName}."
        val politics = if (p.stage >= 12) " ${DialogueEngine.courtPoliticsLine(p.stage)}" else ""
        return "Companion: ${lead ?: "No companion bonded yet"} | Route: $route | Arc(S/W/E): ${p.strengthArc}/${p.wisdomArc}/${p.empathyArc}\nGear: ${p.weaponName}, ${p.armorName}, ${p.accessoryName}\nLore: ${NarrativeEngine.regionLore(p.stage)} ${DialogueEngine.arcProgressLine(p)}$politics\n$affection"
    }

    private fun companionPortraitRes(p: PlayerProfile): Int {
        val top = NarrativeEngine.topRomances(p).firstOrNull()
        return when (top) {
            "Nyra" -> R.drawable.portrait_nyra
            "Lyra" -> R.drawable.portrait_lyra
            "Sera" -> R.drawable.portrait_sera
            "Mira" -> R.drawable.portrait_mira
            "Kaela" -> R.drawable.portrait_kaela
            "Veya" -> R.drawable.portrait_veya
            "Iris" -> R.drawable.portrait_iris
            else -> if (p.stage % 2 == 0) R.drawable.mc_render_auren else R.drawable.mc_render_auren_alt
        }
    }

    private fun setupMoveButton(button: View, dx: Float, dy: Float) {
        button.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    GameAudio.playFootstep()
                    binding.gameView.setMoveDirection(dx, dy)
                }
                MotionEvent.ACTION_MOVE -> binding.gameView.setMoveDirection(dx, dy)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> binding.gameView.setMoveDirection(0f, 0f)
            }
            true
        }
    }

    private fun applyTypewriter(target: String) {
        if (target == lastTypedLog) return
        stopTypewriter()
        lastTypedLog = target
        binding.logText.text = ""
        var index = 0
        val step = when {
            target.length > 240 -> 3
            target.length > 120 -> 2
            else -> 1
        }
        typewriterRunnable = object : Runnable {
            override fun run() {
                if (index >= target.length) return
                index = minOf(target.length, index + step)
                binding.logText.text = target.substring(0, index)
                typewriterHandler.postDelayed(this, 14L)
            }
        }
        typewriterHandler.post(typewriterRunnable!!)
    }

    private fun stopTypewriter() {
        typewriterRunnable?.let { typewriterHandler.removeCallbacks(it) }
        typewriterRunnable = null
    }
}
