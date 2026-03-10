package com.shiro.aetherquest

enum class HeroClass { KNIGHT, RANGER, MYSTIC }

enum class LootRarity { COMMON, RARE, EPIC }

data class PlayerProfile(
    var heroClass: HeroClass,
    var level: Int = 1,
    var xp: Int = 0,
    var stage: Int = 1,
    var bestStage: Int = 1,
    var battlesWon: Int = 0,
    var coins: Int = 0,
    var maxHp: Int,
    var hp: Int,
    var attack: Int,
    var defense: Int,
    var critChance: Float,
    var potions: Int = 3,
    var skillCharges: Int = 3,
    var weaponTier: Int = 0,
    var armorTier: Int = 0,
    var gems: Int = 0,
    var questKills: Int = 0,
    var questTarget: Int = 5,
    var questsCompleted: Int = 0
)

data class Enemy(
    val name: String,
    val isBoss: Boolean,
    var maxHp: Int,
    var hp: Int,
    var attack: Int,
    var defense: Int
)

data class GameSession(
    var player: PlayerProfile,
    var enemy: Enemy? = null,
    var inBattle: Boolean = false,
    var shieldTurns: Int = 0,
    var lastLog: String = "Welcome to AetherQuest",
    var lastLoot: String = "No loot yet"
)

object GameFactory {
    fun newProfile(heroClass: HeroClass): PlayerProfile {
        return when (heroClass) {
            HeroClass.KNIGHT -> PlayerProfile(heroClass, maxHp = 150, hp = 150, attack = 18, defense = 12, critChance = 0.12f)
            HeroClass.RANGER -> PlayerProfile(heroClass, maxHp = 120, hp = 120, attack = 22, defense = 8, critChance = 0.18f)
            HeroClass.MYSTIC -> PlayerProfile(heroClass, maxHp = 110, hp = 110, attack = 24, defense = 7, critChance = 0.14f)
        }
    }
}
