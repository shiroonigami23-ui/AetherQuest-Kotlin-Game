package com.shiro.aetherquest.shared

enum class SharedHeroClass { KNIGHT, RANGER, MYSTIC }

data class SharedProfile(
    val heroClass: SharedHeroClass,
    val level: Int,
    val stage: Int,
    val hp: Int,
    val maxHp: Int
)

data class SharedQuestState(
    val chapter: Int,
    val questKills: Int,
    val questTarget: Int,
    val endingsUnlocked: Int
)

object SharedBalancing {
    fun requiredXp(level: Int): Int = 80 + (level - 1).coerceAtLeast(0) * 35
}
