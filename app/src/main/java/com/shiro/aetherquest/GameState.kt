package com.shiro.aetherquest

enum class HeroClass { KNIGHT, RANGER, MYSTIC }
enum class DifficultyMode { STORY, EASY, HARD, EXTREME }
enum class RelationshipStyle { UNSET, SINGLE, THROUPLE, HAREM }

enum class LootRarity { COMMON, RARE, EPIC }
enum class EndingType { NONE, TRAGIC, RUTHLESS, HEROIC, ROMANTIC, THROUPLE, HAREM, LONE_WOLF }

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
    var questsCompleted: Int = 0,
    var lives: Int = 3,
    var affinityNyra: Int = 0,
    var affinityLyra: Int = 0,
    var affinitySera: Int = 0,
    var affinityMira: Int = 0,
    var affinityKaela: Int = 0,
    var affinityCrown: Int = 0,
    var chapter: Int = 1,
    var strengthArc: Int = 0,
    var wisdomArc: Int = 0,
    var empathyArc: Int = 0,
    var bombs: Int = 1,
    var elixirs: Int = 1,
    var keys: Int = 0,
    var relicShards: Int = 0,
    var weaponName: String = "Rustforged Blade",
    var armorName: String = "Traveler Mail",
    var accessoryName: String = "Plain Charm",
    var relationshipStyle: RelationshipStyle = RelationshipStyle.UNSET
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
    var lastLoot: String = "No loot yet",
    var gameOver: Boolean = false,
    var ending: EndingType = EndingType.NONE,
    var storyPrompt: String = "",
    var choiceA: String = "",
    var choiceB: String = "",
    var pendingStoryEvent: String = "",
    var storyEventsSeen: String = "",
    var difficultyMode: DifficultyMode = DifficultyMode.EASY,
    var discoveredSecrets: Int = 0,
    var timedEventTick: Int = 0,
    var chestReady: Boolean = false
)

object GameFactory {
    fun newSession(heroClass: HeroClass, difficulty: DifficultyMode): GameSession {
        val profile = when (heroClass) {
            HeroClass.KNIGHT -> PlayerProfile(heroClass, maxHp = 150, hp = 150, attack = 18, defense = 12, critChance = 0.12f)
            HeroClass.RANGER -> PlayerProfile(heroClass, maxHp = 120, hp = 120, attack = 22, defense = 8, critChance = 0.18f)
            HeroClass.MYSTIC -> PlayerProfile(heroClass, maxHp = 110, hp = 110, attack = 24, defense = 7, critChance = 0.14f)
        }
        profile.lives = when (difficulty) {
            DifficultyMode.STORY -> 99
            DifficultyMode.EASY -> 5
            DifficultyMode.HARD -> 3
            DifficultyMode.EXTREME -> 1
        }
        return GameSession(player = profile, difficultyMode = difficulty)
    }
}
