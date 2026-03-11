package com.shiro.aetherquest

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BattleEngine {
    private val enemyNames = listOf("Ashfang", "Night Warden", "Bone Raider", "Storm Hound", "Void Acolyte", "Crag Titan")

    fun spawnEnemy(stage: Int): Enemy {
        if (stage >= 25) {
            val hp = 420 + stage * 20
            val atk = 48 + stage * 2
            val def = 28 + stage
            return Enemy("Abyss Sovereign", true, hp, hp, atk, def)
        }
        val boss = stage % 5 == 0
        val hp = if (boss) 160 + stage * 18 else 90 + stage * 12
        val atk = if (boss) 16 + stage * 2 else 10 + stage
        val def = if (boss) 10 + stage else 6 + stage / 2
        val label = if (boss) "Ancient ${enemyNames.random()}" else enemyNames.random()
        return Enemy(label, boss, hp, hp, atk, def)
    }

    fun startBattle(session: GameSession, forcedName: String? = null) {
        if (session.gameOver) return
        val spawned = spawnEnemy(session.player.stage)
        val enemy = if (forcedName.isNullOrBlank()) spawned else {
            Enemy(forcedName, spawned.isBoss, spawned.maxHp, spawned.hp, spawned.attack, spawned.defense)
        }
        val tunedEnemy = applySecretBossRules(session, enemy)
        applyDifficultyToEnemy(session, tunedEnemy)
        session.enemy = tunedEnemy
        session.inBattle = true
        session.shieldTurns = 0
        session.enemyShieldTurns = 0
        session.enemyChargeTurns = 0
        session.playerBleedTurns = 0
        session.comboCount = 0
        session.turnCounter = 0
        session.enemyIntent = chooseEnemyIntent(session, enemy)
        session.lastLog = DialogueEngine.enemyEncounter(tunedEnemy.name, session.player.stage, tunedEnemy.isBoss)
        if (session.difficultyMode == DifficultyMode.STORY) {
            session.enemy?.hp = 0
            onVictory(session)
            session.lastLog = "Story Mode blessing: battle auto-resolved in your favor."
        }
    }

    fun playerAttack(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        val base = effectiveAttack(p) + Random.nextInt(0, 8)
        val defenseCut = effectiveEnemyDefense(enemy, p.weaponTrait)
        val critChance = p.critChance + critTraitBonus(p.weaponTrait) + session.comboCount * 0.01f
        val crit = Random.nextFloat() <= critChance
        var damage = max(1, base - defenseCut)
        if (p.weaponTrait == WeaponTrait.ARCANE) {
            damage += 2 + p.level / 2
        }
        if (crit) {
            damage = (damage * (1.55f + p.weaponMastery * 0.01f)).toInt()
        }
        damage = dealDamageToEnemy(session, damage)
        session.comboCount = min(6, session.comboCount + 1)
        val traitLog = applyWeaponOnHitEffects(session, damage)
        session.lastLog = (if (crit) "Critical strike! You dealt $damage." else "You dealt $damage damage.") + traitLog
        if (enemy.hp <= 0) onVictory(session) else enemyTurn(session)
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
            HeroClass.KNIGHT -> effectiveAttack(p) + 22 + p.armorTier
            HeroClass.RANGER -> effectiveAttack(p) + 18 + p.weaponMastery / 2
            HeroClass.MYSTIC -> effectiveAttack(p) + 25 + p.level / 2
        }
        var damage = max(1, base + Random.nextInt(4, 14) - (effectiveEnemyDefense(enemy, p.weaponTrait) / 2))
        if (p.weaponTrait == WeaponTrait.ARCANE) damage += 4 + p.level / 3
        damage = dealDamageToEnemy(session, damage)
        session.comboCount = min(6, session.comboCount + 2)
        val traitLog = applyWeaponOnHitEffects(session, damage)
        session.lastLog = "Signature skill hits for $damage.$traitLog"
        if (enemy.hp <= 0) onVictory(session) else enemyTurn(session)
    }

    fun playerHeal(session: GameSession) {
        val p = session.player
        if (p.potions <= 0 && p.elixirs <= 0) {
            session.lastLog = "No healing items left."
            return
        }
        val amount = if (p.elixirs > 0) {
            p.elixirs -= 1
            max(24, (p.maxHp * 0.52f).toInt())
        } else {
            p.potions -= 1
            max(12, (p.maxHp * 0.35f).toInt())
        }
        p.hp = min(p.maxHp, p.hp + amount)
        session.comboCount = 0
        session.lastLog = "Potion restored $amount HP."
        enemyTurn(session)
    }

    fun playerUseBomb(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        if (p.bombs <= 0) {
            session.lastLog = "No bombs available."
            return
        }
        p.bombs -= 1
        val damage = dealDamageToEnemy(session, max(18, 26 + p.level * 2 + p.weaponTier))
        session.comboCount = min(6, session.comboCount + 1)
        session.lastLog = "Bomb explodes for $damage damage."
        if (enemy.hp <= 0) onVictory(session) else enemyTurn(session)
    }

    fun playerDefend(session: GameSession) {
        session.shieldTurns = 1
        session.comboCount = 0
        session.lastLog = "You brace for impact. Incoming damage reduced."
        enemyTurn(session)
    }

    private fun enemyTurn(session: GameSession) {
        val enemy = session.enemy ?: return
        val p = session.player
        session.turnCounter += 1
        val turnLog = mutableListOf<String>()

        if (session.playerBleedTurns > 0) {
            val dot = max(2, enemy.attack / 4)
            p.hp = max(0, p.hp - dot)
            session.playerBleedTurns -= 1
            turnLog += "Bleed deals $dot."
            if (p.hp <= 0) {
                session.lastLog += " ${turnLog.joinToString(" ")}"
                onDefeat(session)
                return
            }
        }

        val intent = session.enemyIntent
        if (session.turnCounter % 2 == 1) {
            turnLog += DialogueEngine.enemyIntentLine(enemy.name, intent)
        }
        when (intent) {
            EnemyIntent.ATTACK -> {
                val damage = applyEnemyHit(session, enemy.attack + Random.nextInt(0, 7))
                turnLog += "${enemy.name} attacks for $damage."
            }
            EnemyIntent.HEAVY -> {
                val heavyBase = if (session.enemyChargeTurns > 0) enemy.attack + 12 else enemy.attack + 7
                val damage = applyEnemyHit(session, heavyBase + Random.nextInt(2, 9))
                session.enemyChargeTurns = 0
                if (Random.nextFloat() < 0.35f) {
                    session.playerBleedTurns = max(session.playerBleedTurns, 2)
                    turnLog += "You are bleeding."
                }
                turnLog += "${enemy.name} lands a heavy strike for $damage."
            }
            EnemyIntent.GUARD -> {
                session.enemyShieldTurns = 1
                val heal = max(3, enemy.maxHp / 14)
                enemy.hp = min(enemy.maxHp, enemy.hp + heal)
                turnLog += "${enemy.name} guards and recovers $heal."
            }
            EnemyIntent.DRAIN -> {
                val damage = applyEnemyHit(session, enemy.attack + 4 + Random.nextInt(0, 6))
                val heal = max(2, damage / 2)
                enemy.hp = min(enemy.maxHp, enemy.hp + heal)
                turnLog += "${enemy.name} drains $damage and heals $heal."
            }
            EnemyIntent.ENRAGE -> {
                enemy.attack += 3
                enemy.defense += 2
                session.enemyChargeTurns = 1
                turnLog += "${enemy.name} enrages. Attack and defense rise."
            }
        }

        if (p.hp <= 0) {
            session.lastLog += " ${turnLog.joinToString(" ")}"
            onDefeat(session)
            return
        }
        session.enemyIntent = chooseEnemyIntent(session, enemy)
        session.lastLog += " ${turnLog.joinToString(" ")} Next intent: ${session.enemyIntent}."
    }

    private fun onVictory(session: GameSession) {
        val p = session.player
        val enemy = session.enemy ?: return
        val xpGain = if (enemy.isBoss) 90 + p.stage * 8 else 35 + p.stage * 5
        val coinGain = if (enemy.isBoss) 50 + p.stage * 4 else 18 + p.stage * 2
        p.xp += xpGain
        p.coins += coinGain
        p.weaponMastery += 2 + p.weaponTier / 2
        p.battlesWon += 1
        p.stage += 1
        p.bestStage = max(p.bestStage, p.stage)
        p.skillCharges = min(3 + p.level / 3, p.skillCharges + 1)
        p.hp = min(p.maxHp, p.hp + (p.maxHp * 0.18f).toInt())
        applyLoot(session, enemy.isBoss)
        val questLine = updateQuest(session)
        updateTimedEvents(session)
        levelUpIfNeeded(p)
        session.inBattle = false
        session.enemy = null
        session.comboCount = 0
        session.enemyShieldTurns = 0
        session.enemyChargeTurns = 0
        session.playerBleedTurns = 0
        session.lastLog = "Victory! +$xpGain XP, +$coinGain coins. Stage ${p.stage} unlocked. ${session.lastLoot}${if (questLine.isNotBlank()) " $questLine" else ""}"
        NarrativeEngine.maybeTriggerStory(session)
        val resolved = NarrativeEngine.resolveEnding(session)
        if (resolved != EndingType.NONE) {
            session.ending = resolved
            session.gameOver = true
            session.lastLog = NarrativeEngine.endingText(resolved, p)
        }
    }

    private fun onDefeat(session: GameSession) {
        val p = session.player
        if (session.difficultyMode == DifficultyMode.EXTREME) {
            p.lives = 0
        }
        p.lives -= 1
        session.inBattle = false
        session.enemy = null
        session.comboCount = 0
        session.enemyShieldTurns = 0
        session.enemyChargeTurns = 0
        session.playerBleedTurns = 0
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
            p.weaponMastery += 1
            p.skillCharges = max(p.skillCharges, 3)
            needed = 80 + (p.level - 1) * 35
        }
    }

    private fun effectiveAttack(p: PlayerProfile): Int {
        val traitBonus = when (p.weaponTrait) {
            WeaponTrait.BALANCED -> 2
            WeaponTrait.CRIT -> 1
            WeaponTrait.LIFESTEAL -> 1
            WeaponTrait.PIERCE -> 3
            WeaponTrait.ARCANE -> 2
        }
        return p.attack + p.weaponTier * 3 + p.weaponMastery / 4 + traitBonus
    }

    private fun effectiveDefense(p: PlayerProfile): Int = p.defense + p.armorTier * 3

    private fun effectiveEnemyDefense(enemy: Enemy, trait: WeaponTrait): Int {
        val base = enemy.defense
        return when (trait) {
            WeaponTrait.PIERCE -> max(0, (base * 0.58f).toInt())
            WeaponTrait.ARCANE -> max(0, (base * 0.74f).toInt())
            else -> base
        }
    }

    private fun critTraitBonus(trait: WeaponTrait): Float {
        return when (trait) {
            WeaponTrait.CRIT -> 0.1f
            WeaponTrait.BALANCED -> 0.03f
            else -> 0f
        }
    }

    private fun dealDamageToEnemy(session: GameSession, rawDamage: Int): Int {
        val enemy = session.enemy ?: return 0
        var damage = max(1, rawDamage)
        if (session.enemyShieldTurns > 0) {
            damage = max(1, (damage * 0.55f).toInt())
            session.enemyShieldTurns = 0
        }
        enemy.hp = max(0, enemy.hp - damage)
        return damage
    }

    private fun applyWeaponOnHitEffects(session: GameSession, damage: Int): String {
        val p = session.player
        when (p.weaponTrait) {
            WeaponTrait.LIFESTEAL -> {
                val heal = max(1, (damage * (0.08f + p.weaponTier * 0.01f)).toInt())
                p.hp = min(p.maxHp, p.hp + heal)
                return " Lifesteal heals $heal."
            }
            WeaponTrait.ARCANE -> {
                if (Random.nextFloat() < 0.16f) {
                    val bonus = 4 + p.level / 2
                    dealDamageToEnemy(session, bonus)
                    return " Arcane burst triggers for $bonus."
                }
            }
            else -> {}
        }
        return ""
    }

    private fun applyEnemyHit(session: GameSession, raw: Int): Int {
        val p = session.player
        var damage = max(1, raw - effectiveDefense(p) / 2)
        if (session.shieldTurns > 0) {
            damage = (damage * 0.45f).toInt().coerceAtLeast(1)
            session.shieldTurns = 0
        }
        p.hp = max(0, p.hp - damage)
        return damage
    }

    private fun chooseEnemyIntent(session: GameSession, enemy: Enemy): EnemyIntent {
        if (session.difficultyMode == DifficultyMode.STORY) return EnemyIntent.ATTACK
        val hpRatio = enemy.hp.toFloat() / max(1, enemy.maxHp).toFloat()
        if (enemy.isBoss && hpRatio < 0.45f && session.turnCounter > 1 && Random.nextFloat() < 0.30f) {
            return EnemyIntent.ENRAGE
        }
        if (hpRatio < 0.28f && Random.nextFloat() < 0.36f) return EnemyIntent.GUARD
        if (session.turnCounter % 4 == 0 && session.turnCounter > 0) return EnemyIntent.HEAVY
        if (enemy.isBoss && Random.nextFloat() < 0.22f) return EnemyIntent.DRAIN
        return if (Random.nextFloat() < 0.72f) EnemyIntent.ATTACK else EnemyIntent.HEAVY
    }

    private fun applySecretBossRules(session: GameSession, enemy: Enemy): Enemy {
        val p = session.player
        if (!enemy.isBoss && session.discoveredSecrets >= 4 && p.stage % 9 == 0) {
            val name = listOf("Dread Revenant", "Mirror Wraith", "Grave Templar").random()
            return Enemy(
                name = name,
                isBoss = true,
                maxHp = (enemy.maxHp * 1.55f).toInt(),
                hp = (enemy.maxHp * 1.55f).toInt(),
                attack = (enemy.attack * 1.4f).toInt(),
                defense = enemy.defense + 10
            )
        }
        if (enemy.name == "Abyss Sovereign") {
            p.lives = max(1, p.lives)
        }
        return enemy
    }

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
                if (Random.nextFloat() > 0.7f) p.crystalShards += 1
                session.lastLoot = "Loot: supply pouch (+10 coins)."
            }
            LootRarity.RARE -> {
                p.gems += 1
                p.coins += 18
                p.weaponCores += 1
                session.lastLoot = "Loot: arcane gem (+1 gem) and weapon core."
            }
            LootRarity.EPIC -> {
                p.gems += 2
                p.potions += 1
                p.coins += 35
                p.armorPlates += 1
                p.crystalShards += 1
                session.lastLoot = "Loot: mythic cache (+2 gems, +1 potion, +1 armor plate, +1 crystal)."
            }
        }
    }

    private fun updateQuest(session: GameSession): String {
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
            return DialogueEngine.questGiverLine(p.stage, p.questsCompleted)
        }
        return ""
    }

    private fun updateTimedEvents(session: GameSession) {
        val p = session.player
        session.timedEventTick += 1
        if (session.timedEventTick % 3 == 0) {
            session.chestReady = true
        }
        if (session.timedEventTick % 5 == 0) {
            p.keys += 1
            session.lastLoot += " Timed event: caravan gift (+1 key)."
        }
        if (p.stage % 7 == 0) {
            p.relicShards += 1
            session.discoveredSecrets += 1
            session.lastLoot += " Secret location uncovered (+1 relic shard)."
        }
    }

    private fun applyDifficultyToEnemy(session: GameSession, enemy: Enemy) {
        when (session.difficultyMode) {
            DifficultyMode.STORY -> {
                enemy.hp = 1
                enemy.attack = 0
                enemy.defense = 0
            }
            DifficultyMode.EASY -> {
                enemy.hp = (enemy.hp * 0.85f).toInt()
                enemy.attack = (enemy.attack * 0.85f).toInt()
            }
            DifficultyMode.HARD -> {
                enemy.hp = (enemy.hp * 1.3f).toInt()
                enemy.attack = (enemy.attack * 1.25f).toInt()
                enemy.defense += 4
            }
            DifficultyMode.EXTREME -> {
                enemy.hp = (enemy.hp * 1.65f).toInt()
                enemy.attack = (enemy.attack * 1.55f).toInt()
                enemy.defense += 8
            }
        }
    }
}
