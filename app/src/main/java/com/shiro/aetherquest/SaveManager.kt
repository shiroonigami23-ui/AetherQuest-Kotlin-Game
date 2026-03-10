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
                relationshipStyle = RelationshipStyle.valueOf(o.optString("relationshipStyle", RelationshipStyle.UNSET.name))
            )
            GameSession(
                player = profile,
                gameOver = o.optBoolean("gameOver", false),
                ending = EndingType.valueOf(o.optString("ending", EndingType.NONE.name)),
                storyPrompt = o.optString("storyPrompt", ""),
                choiceA = o.optString("choiceA", ""),
                choiceB = o.optString("choiceB", ""),
                pendingStoryEvent = o.optString("pendingStoryEvent", ""),
                storyEventsSeen = o.optString("storyEventsSeen", ""),
                difficultyMode = DifficultyMode.valueOf(o.optString("difficultyMode", DifficultyMode.EASY.name)),
                discoveredSecrets = o.optInt("discoveredSecrets", 0),
                timedEventTick = o.optInt("timedEventTick", 0),
                chestReady = o.optBoolean("chestReady", false)
            )
        } catch (_: Exception) {
            null
        }
    }
}
