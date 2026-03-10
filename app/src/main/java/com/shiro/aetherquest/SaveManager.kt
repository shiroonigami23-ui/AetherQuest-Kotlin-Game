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
                questsCompleted = o.optInt("questsCompleted", 0)
            )
            GameSession(profile)
        } catch (_: Exception) {
            null
        }
    }
}
