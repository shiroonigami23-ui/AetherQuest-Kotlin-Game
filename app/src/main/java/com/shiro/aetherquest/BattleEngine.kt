package com.shiro.aetherquest

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BattleEngine {
    private val enemyNames = listOf("Ashfang", "Night Warden", "Bone Raider", "Storm Hound", "Void Acolyte", "Crag Titan")

    fun spawnEnemy(stage: Int): Enemy {
        val boss = stage % 5 == 0
        val hp = if (boss) 160 + stage * 18 else 90 + stage * 12
        val atk = if (boss) 16 + stage * 2 else 10 + stage
        val def = if (boss) 10 + stage else 6 + stage / 2
        val label = if (boss) "Ancient ${enemyNames.random()}" else enemyNames.random()
        return Enemy(label, boss, hp, hp, atk, def)
    }

    fun startBattle(session: GameSession) {
        if (session.gameOver) return
        session.enemy = spawnEnemy(session.player.stage)
        session.inBattle = true
        session.shieldTurns = 0
        session.lastLog = "Encounter! ${session.enemy?.name} appears."
    }

    fun playerAttack(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        val crit = Random.nextFloat() <= p.critChance
        var damage = max(1, effectiveAttack(p) + Random.nextInt(0, 8) - enemy.defense / 2)
        if (crit) damage = (damage * 1.7f).toInt()
        enemy.hp = max(0, enemy.hp - damage)
        session.lastLog = if (crit) "Critical strike! You dealt $damage." else "You dealt $damage damage."
        if (enemy.hp <= 0) {
            onVictory(session)
        } else {
            enemyTurn(session)
        }
    }

    fun playerSkill(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        if (p.skillCharges <= 0) {
            session.lastLog = "No skill charges left this expedition."
            return
        }
        p.skillCharges -= 1
        val base = when (p.heroClass) {
            HeroClass.KNIGHT -> effectiveAttack(p) + 20
            HeroClass.RANGER -> effectiveAttack(p) + 16
            HeroClass.MYSTIC -> effectiveAttack(p) + 24
        }
        val damage = max(1, base + Random.nextInt(4, 14) - enemy.defense / 3)
        enemy.hp = max(0, enemy.hp - damage)
        session.lastLog = "Signature skill hits for $damage!"
        if (enemy.hp <= 0) onVictory(session) else enemyTurn(session)
    }

    fun playerHeal(session: GameSession) {
        val p = session.player
        if (p.potions <= 0) {
            session.lastLog = "No potions left."
            return
        }
        p.potions -= 1
        val amount = max(12, (p.maxHp * 0.35f).toInt())
        p.hp = min(p.maxHp, p.hp + amount)
        session.lastLog = "Potion restored $amount HP."
        enemyTurn(session)
    }

    fun playerDefend(session: GameSession) {
        session.shieldTurns = 1
        session.lastLog = "You brace for impact. Incoming damage reduced."
        enemyTurn(session)
    }

    private fun enemyTurn(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        var damage = max(1, enemy.attack + Random.nextInt(0, 7) - effectiveDefense(p) / 2)
        if (session.shieldTurns > 0) {
            damage = (damage * 0.45f).toInt().coerceAtLeast(1)
            session.shieldTurns = 0
        }
        p.hp = max(0, p.hp - damage)
        session.lastLog += "  ${enemy.name} hits you for $damage."
        if (p.hp <= 0) {
            onDefeat(session)
        }
    }

    private fun onVictory(session: GameSession) {
        val p = session.player
        val enemy = session.enemy ?: return
        val xpGain = if (enemy.isBoss) 90 + p.stage * 8 else 35 + p.stage * 5
        val coinGain = if (enemy.isBoss) 50 + p.stage * 4 else 18 + p.stage * 2
        p.xp += xpGain
        p.coins += coinGain
        p.battlesWon += 1
        p.stage += 1
        p.bestStage = max(p.bestStage, p.stage)
        p.skillCharges = min(3 + p.level / 3, p.skillCharges + 1)
        p.hp = min(p.maxHp, p.hp + (p.maxHp * 0.18f).toInt())
        applyLoot(session, enemy.isBoss)
        updateQuest(session)
        levelUpIfNeeded(p)
        session.inBattle = false
        session.enemy = null
        session.lastLog = "Victory! +$xpGain XP, +$coinGain coins. Stage ${p.stage} unlocked. ${session.lastLoot}"
        NarrativeEngine.maybeTriggerStory(session)
        val resolved = NarrativeEngine.resolveEnding(session)
        if (resolved != EndingType.NONE) {
            session.ending = resolved
            session.gameOver = true
            session.lastLog = NarrativeEngine.endingText(resolved)
        }
    }

    private fun onDefeat(session: GameSession) {
        val p = session.player
        p.lives -= 1
        session.inBattle = false
        session.enemy = null
        if (p.lives <= 0) {
            session.gameOver = true
            session.ending = EndingType.TRAGIC
            session.lastLog = NarrativeEngine.endingText(EndingType.TRAGIC)
            return
        }
        p.stage = max(1, p.stage - 1)
        p.hp = p.maxHp
        p.potions = max(1, p.potions)
        p.skillCharges = max(1, p.skillCharges)
        session.lastLog = "Defeated. Lives left: ${p.lives}. You fall back to Stage ${p.stage}."
    }

    private fun levelUpIfNeeded(p: PlayerProfile) {
        var needed = 80 + (p.level - 1) * 35
        while (p.xp >= needed) {
            p.xp -= needed
            p.level += 1
            p.maxHp += 16
            p.attack += 3
            p.defense += 2
            p.hp = p.maxHp
            p.potions += 1
            p.skillCharges = max(p.skillCharges, 3)
            needed = 80 + (p.level - 1) * 35
        }
    }

    private fun effectiveAttack(p: PlayerProfile): Int = p.attack + p.weaponTier * 3

    private fun effectiveDefense(p: PlayerProfile): Int = p.defense + p.armorTier * 3

    private fun applyLoot(session: GameSession, boss: Boolean) {
        val p = session.player
        val roll = Random.nextFloat()
        val rarity = when {
            roll > 0.96f || boss -> LootRarity.EPIC
            roll > 0.78f -> LootRarity.RARE
            else -> LootRarity.COMMON
        }
        when (rarity) {
            LootRarity.COMMON -> {
                p.coins += 10
                session.lastLoot = "Loot: supply pouch (+10 coins)."
            }
            LootRarity.RARE -> {
                p.gems += 1
                p.coins += 18
                session.lastLoot = "Loot: arcane gem (+1 gem)."
            }
            LootRarity.EPIC -> {
                p.gems += 2
                p.potions += 1
                p.coins += 35
                session.lastLoot = "Loot: mythic cache (+2 gems, +1 potion)."
            }
        }
    }

    private fun updateQuest(session: GameSession) {
        val p = session.player
        p.questKills += 1
        if (p.questKills >= p.questTarget) {
            val reward = 80 + p.level * 8
            p.coins += reward
            p.gems += 1
            p.questsCompleted += 1
            p.questKills = 0
            p.questTarget += 2
            session.lastLoot += " Quest complete! +$reward coins +1 gem."
        }
    }
}
