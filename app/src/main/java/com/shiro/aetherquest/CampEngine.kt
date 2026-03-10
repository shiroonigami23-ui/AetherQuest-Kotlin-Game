package com.shiro.aetherquest

import kotlin.math.max
import kotlin.math.min

object CampEngine {
    fun buyPotion(session: GameSession): Boolean {
        val p = session.player
        val cost = 24 + p.level
        if (p.coins < cost) {
            session.lastLog = "Not enough coins for potion. Need $cost."
            return false
        }
        p.coins -= cost
        p.potions += 1
        session.lastLog = "Bought potion for $cost coins."
        return true
    }

    fun buyElixir(session: GameSession): Boolean {
        val p = session.player
        val cost = 45 + p.level * 2
        if (p.coins < cost) {
            session.lastLog = "Not enough coins for elixir. Need $cost."
            return false
        }
        p.coins -= cost
        p.elixirs += 1
        session.lastLog = "Bought elixir for $cost coins."
        return true
    }

    fun buyBomb(session: GameSession): Boolean {
        val p = session.player
        val cost = 38 + p.level
        if (p.coins < cost) {
            session.lastLog = "Not enough coins for bomb. Need $cost."
            return false
        }
        p.coins -= cost
        p.bombs += 1
        session.lastLog = "Bought bomb for $cost coins."
        return true
    }

    fun restAtCamp(session: GameSession): Boolean {
        val p = session.player
        val cost = max(12, p.level * 4)
        if (p.coins < cost) {
            session.lastLog = "Camp rest costs $cost coins."
            return false
        }
        p.coins -= cost
        p.hp = p.maxHp
        p.skillCharges = min(p.skillCharges + 1, 4 + p.level / 4)
        session.lastLog = "You rested at camp and recovered fully."
        return true
    }

    fun upgradeWeapon(session: GameSession): Boolean {
        val p = session.player
        val cost = 70 + p.weaponTier * 45
        if (p.coins < cost || p.gems < 1) {
            session.lastLog = "Weapon upgrade needs $cost coins + 1 gem."
            return false
        }
        p.coins -= cost
        p.gems -= 1
        p.weaponTier += 1
        p.weaponName = when {
            p.weaponTier >= 8 -> "Celestial Dragonfang"
            p.weaponTier >= 6 -> "Moonlit Warblade"
            p.weaponTier >= 4 -> "Stormreaver"
            p.weaponTier >= 2 -> "Knightsteel Edge"
            else -> "Rustforged Blade"
        }
        session.lastLog = "Weapon forged to Tier ${p.weaponTier}."
        return true
    }

    fun upgradeArmor(session: GameSession): Boolean {
        val p = session.player
        val cost = 65 + p.armorTier * 40
        if (p.coins < cost || p.gems < 1) {
            session.lastLog = "Armor upgrade needs $cost coins + 1 gem."
            return false
        }
        p.coins -= cost
        p.gems -= 1
        p.armorTier += 1
        p.armorName = when {
            p.armorTier >= 8 -> "Aegis of Astral Dawn"
            p.armorTier >= 6 -> "Dreadguard Plate"
            p.armorTier >= 4 -> "Mythic Wardmail"
            p.armorTier >= 2 -> "Tempered Bastion"
            else -> "Traveler Mail"
        }
        session.lastLog = "Armor reinforced to Tier ${p.armorTier}."
        return true
    }

    fun craftAccessory(session: GameSession): Boolean {
        val p = session.player
        val cost = 90 + p.level * 3
        if (p.coins < cost || p.relicShards < 2) {
            session.lastLog = "Accessory craft needs $cost coins + 2 relic shards."
            return false
        }
        p.coins -= cost
        p.relicShards -= 2
        p.accessoryName = when (p.accessoryName) {
            "Plain Charm" -> "Heartbound Sigil"
            "Heartbound Sigil" -> "Aether Compass"
            else -> "Crownbreaker Emblem"
        }
        when (p.accessoryName) {
            "Heartbound Sigil" -> p.empathyArc += 1
            "Aether Compass" -> p.wisdomArc += 1
            else -> p.strengthArc += 1
        }
        session.lastLog = "Forged accessory: ${p.accessoryName}."
        return true
    }

    fun openChest(session: GameSession): Boolean {
        val p = session.player
        if (!session.chestReady) {
            session.lastLog = "No chest discovered yet."
            return false
        }
        if (p.keys <= 0) {
            session.lastLog = "Chest found, but you need a key."
            return false
        }
        p.keys -= 1
        session.chestReady = false
        p.coins += 80 + p.level * 5
        p.gems += 1
        p.bombs += 1
        p.elixirs += 1
        session.lastLog = "Treasure chest opened: coins, gem, bomb, and elixir!"
        return true
    }

    fun exploreSecret(session: GameSession): Boolean {
        val p = session.player
        if (p.stage < 6) {
            session.lastLog = "Secret sites unlock after Stage 6."
            return false
        }
        if (p.relicShards <= 0) {
            session.lastLog = "You need relic shards to trace secret routes."
            return false
        }
        p.relicShards -= 1
        session.discoveredSecrets += 1
        p.coins += 55
        p.affinityMira += 1
        p.wisdomArc += 1
        session.lastLog = "Secret shrine found. Mira's trust grows."
        return true
    }
}
