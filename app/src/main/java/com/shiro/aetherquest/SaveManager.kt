package com.shiro.aetherquest

import android.content.Context
import org.json.JSONObject

object SaveManager {
    private const val PREFS = "aetherquest_prefs"
    private const val KEY = "save_slot"

    fun save(context: Context, session: GameSession) {
        val p = session.player
        val obj = JSONObject().apply {
            put("heroClass", p.heroClass.name)
            put("heroName", p.heroName)
            put("level", p.level)
            put("xp", p.xp)
            put("stage", p.stage)
            put("bestStage", p.bestStage)
            put("battlesWon", p.battlesWon)
            put("coins", p.coins)
            put("maxHp", p.maxHp)
            put("hp", p.hp)
            put("attack", p.attack)
            put("defense", p.defense)
            put("critChance", p.critChance.toDouble())
            put("potions", p.potions)
            put("skillCharges", p.skillCharges)
            put("weaponTier", p.weaponTier)
            put("armorTier", p.armorTier)
            put("gems", p.gems)
            put("questKills", p.questKills)
            put("questTarget", p.questTarget)
            put("questsCompleted", p.questsCompleted)
            put("lives", p.lives)
            put("affinityNyra", p.affinityNyra)
            put("affinityLyra", p.affinityLyra)
            put("affinitySera", p.affinitySera)
            put("affinityMira", p.affinityMira)
            put("affinityKaela", p.affinityKaela)
            put("affinityVeya", p.affinityVeya)
            put("affinityIris", p.affinityIris)
            put("affinityCrown", p.affinityCrown)
            put("chapter", p.chapter)
            put("strengthArc", p.strengthArc)
            put("wisdomArc", p.wisdomArc)
            put("empathyArc", p.empathyArc)
            put("bombs", p.bombs)
            put("elixirs", p.elixirs)
            put("keys", p.keys)
            put("relicShards", p.relicShards)
            put("weaponName", p.weaponName)
            put("armorName", p.armorName)
            put("accessoryName", p.accessoryName)
            put("relationshipStyle", p.relationshipStyle.name)
            put("weaponTrait", p.weaponTrait.name)
            put("weaponMastery", p.weaponMastery)
            put("worldX", p.worldX.toDouble())
            put("worldY", p.worldY.toDouble())
            put("gameOver", session.gameOver)
            put("ending", session.ending.name)
            put("storyPrompt", session.storyPrompt)
            put("choiceA", session.choiceA)
            put("choiceB", session.choiceB)
            put("pendingStoryEvent", session.pendingStoryEvent)
            put("storyEventsSeen", session.storyEventsSeen)
            put("difficultyMode", session.difficultyMode.name)
            put("discoveredSecrets", session.discoveredSecrets)
            put("timedEventTick", session.timedEventTick)
            put("chestReady", session.chestReady)
            put("enemyIntent", session.enemyIntent.name)
            put("enemyShieldTurns", session.enemyShieldTurns)
            put("enemyChargeTurns", session.enemyChargeTurns)
            put("playerBleedTurns", session.playerBleedTurns)
            put("comboCount", session.comboCount)
            put("turnCounter", session.turnCounter)
            put("cameraMode", session.cameraMode.name)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, obj.toString())
            .apply()
    }

    fun load(context: Context): GameSession? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return null
        return try {
            val o = JSONObject(raw)
            val profile = PlayerProfile(
                heroClass = HeroClass.valueOf(o.getString("heroClass")),
                heroName = o.optString("heroName", "Auren Valen"),
                level = o.getInt("level"),
                xp = o.getInt("xp"),
                stage = o.getInt("stage"),
                bestStage = o.getInt("bestStage"),
                battlesWon = o.getInt("battlesWon"),
                coins = o.getInt("coins"),
                maxHp = o.getInt("maxHp"),
                hp = o.getInt("hp"),
                attack = o.getInt("attack"),
                defense = o.getInt("defense"),
                critChance = o.getDouble("critChance").toFloat(),
                potions = o.getInt("potions"),
                skillCharges = o.getInt("skillCharges"),
                weaponTier = o.optInt("weaponTier", 0),
                armorTier = o.optInt("armorTier", 0),
                gems = o.optInt("gems", 0),
                questKills = o.optInt("questKills", 0),
                questTarget = o.optInt("questTarget", 5),
                questsCompleted = o.optInt("questsCompleted", 0),
                lives = o.optInt("lives", 3),
                affinityNyra = o.optInt("affinityNyra", 0),
                affinityLyra = o.optInt("affinityLyra", 0),
                affinitySera = o.optInt("affinitySera", 0),
                affinityMira = o.optInt("affinityMira", 0),
                affinityKaela = o.optInt("affinityKaela", 0),
                affinityVeya = o.optInt("affinityVeya", 0),
                affinityIris = o.optInt("affinityIris", 0),
                affinityCrown = o.optInt("affinityCrown", 0),
                chapter = o.optInt("chapter", 1),
                strengthArc = o.optInt("strengthArc", 0),
                wisdomArc = o.optInt("wisdomArc", 0),
                empathyArc = o.optInt("empathyArc", 0),
                bombs = o.optInt("bombs", 1),
                elixirs = o.optInt("elixirs", 1),
                keys = o.optInt("keys", 0),
                relicShards = o.optInt("relicShards", 0),
                weaponName = o.optString("weaponName", "Rustforged Blade"),
                armorName = o.optString("armorName", "Traveler Mail"),
                accessoryName = o.optString("accessoryName", "Plain Charm"),
                relationshipStyle = parseRelationshipStyle(o.optString("relationshipStyle", RelationshipStyle.UNSET.name)),
                weaponTrait = parseWeaponTrait(o.optString("weaponTrait", WeaponTrait.BALANCED.name)),
                weaponMastery = o.optInt("weaponMastery", 0),
                worldX = o.optDouble("worldX", -1.0).toFloat(),
                worldY = o.optDouble("worldY", -1.0).toFloat()
            )
            GameSession(
                player = profile,
                gameOver = o.optBoolean("gameOver", false),
                ending = parseEndingType(o.optString("ending", EndingType.NONE.name)),
                storyPrompt = o.optString("storyPrompt", ""),
                choiceA = o.optString("choiceA", ""),
                choiceB = o.optString("choiceB", ""),
                pendingStoryEvent = o.optString("pendingStoryEvent", ""),
                storyEventsSeen = o.optString("storyEventsSeen", ""),
                difficultyMode = parseDifficultyMode(o.optString("difficultyMode", DifficultyMode.EASY.name)),
                discoveredSecrets = o.optInt("discoveredSecrets", 0),
                timedEventTick = o.optInt("timedEventTick", 0),
                chestReady = o.optBoolean("chestReady", false),
                enemyIntent = parseEnemyIntent(o.optString("enemyIntent", EnemyIntent.ATTACK.name)),
                enemyShieldTurns = o.optInt("enemyShieldTurns", 0),
                enemyChargeTurns = o.optInt("enemyChargeTurns", 0),
                playerBleedTurns = o.optInt("playerBleedTurns", 0),
                comboCount = o.optInt("comboCount", 0),
                turnCounter = o.optInt("turnCounter", 0),
                cameraMode = parseCameraMode(o.optString("cameraMode", CameraMode.THIRD_PERSON.name))
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDifficultyMode(name: String): DifficultyMode {
        return runCatching { DifficultyMode.valueOf(name) }.getOrDefault(DifficultyMode.EASY)
    }

    private fun parseEndingType(name: String): EndingType {
        return runCatching { EndingType.valueOf(name) }.getOrDefault(EndingType.NONE)
    }

    private fun parseWeaponTrait(name: String): WeaponTrait {
        return runCatching { WeaponTrait.valueOf(name) }.getOrDefault(WeaponTrait.BALANCED)
    }

    private fun parseEnemyIntent(name: String): EnemyIntent {
        return runCatching { EnemyIntent.valueOf(name) }.getOrDefault(EnemyIntent.ATTACK)
    }

    private fun parseRelationshipStyle(name: String): RelationshipStyle {
        return runCatching { RelationshipStyle.valueOf(name) }.getOrDefault(RelationshipStyle.UNSET)
    }

    private fun parseCameraMode(name: String): CameraMode {
        return runCatching { CameraMode.valueOf(name) }.getOrDefault(CameraMode.THIRD_PERSON)
    }
}
