package com.shiro.aetherquest

import kotlin.math.max
import kotlin.math.min

object CampEngine {
    fun buyPotion(session: GameSession): Boolean {
        val p = session.player
        val cost = 24 + p.level
        if (p.coins < cost) {
            session.lastLog = DialogueEngine.shopFail("Potion", cost)
            return false
        }
        p.coins -= cost
        p.potions += 1
        session.lastLog = DialogueEngine.shopPurchase("Potion", cost)
        return true
    }

    fun buyElixir(session: GameSession): Boolean {
        val p = session.player
        val cost = 45 + p.level * 2
        if (p.coins < cost) {
            session.lastLog = DialogueEngine.shopFail("Elixir", cost)
            return false
        }
        p.coins -= cost
        p.elixirs += 1
        session.lastLog = DialogueEngine.shopPurchase("Elixir", cost)
        return true
    }

    fun buyBomb(session: GameSession): Boolean {
        val p = session.player
        val cost = 38 + p.level
        if (p.coins < cost) {
            session.lastLog = DialogueEngine.shopFail("Bomb", cost)
            return false
        }
        p.coins -= cost
        p.bombs += 1
        session.lastLog = DialogueEngine.shopPurchase("Bomb", cost)
        return true
    }

    fun restAtCamp(session: GameSession): Boolean {
        val p = session.player
        val cost = max(12, p.level * 4)
        if (p.coins < cost) {
            session.lastLog = "Innkeeper Mara: \"A bed and hot stew costs $cost coins tonight.\""
            return false
        }
        p.coins -= cost
        p.hp = p.maxHp
        p.skillCharges = min(p.skillCharges + 1, 4 + p.level / 4)
        session.lastLog = "${DialogueEngine.campRestLine()} ${DialogueEngine.npcJokeLine()}"
        return true
    }

    fun upgradeWeapon(session: GameSession): Boolean {
        val p = session.player
        val cost = 70 + p.weaponTier * 45
        if (p.coins < cost || p.gems < 1 || p.weaponCores < 1) {
            session.lastLog = "Blacksmith Toren: \"Need $cost coins, 1 gem, and 1 weapon core for this forge step.\""
            return false
        }
        p.coins -= cost
        p.gems -= 1
        p.weaponCores -= 1
        p.weaponTier += 1
        p.weaponMastery += 2
        p.weaponName = when {
            p.weaponTier >= 8 -> "Celestial Dragonfang"
            p.weaponTier >= 6 -> "Moonlit Warblade"
            p.weaponTier >= 4 -> "Stormreaver"
            p.weaponTier >= 2 -> "Knightsteel Edge"
            else -> "Rustforged Blade"
        }
        p.weaponTrait = resolveWeaponTrait(p)
        session.lastLog = "${DialogueEngine.smithWeaponLine(p.weaponName, p.weaponTier, p.weaponTrait)} Mastery ${p.weaponMastery}."
        return true
    }

    fun upgradeArmor(session: GameSession): Boolean {
        val p = session.player
        val cost = 65 + p.armorTier * 40
        if (p.coins < cost || p.gems < 1 || p.armorPlates < 1) {
            session.lastLog = "Armorer Brunn: \"Bring $cost coins, 1 gem, and 1 armor plate for reinforced plating.\""
            return false
        }
        p.coins -= cost
        p.gems -= 1
        p.armorPlates -= 1
        p.armorTier += 1
        p.armorName = when {
            p.armorTier >= 8 -> "Aegis of Astral Dawn"
            p.armorTier >= 6 -> "Dreadguard Plate"
            p.armorTier >= 4 -> "Mythic Wardmail"
            p.armorTier >= 2 -> "Tempered Bastion"
            else -> "Traveler Mail"
        }
        session.lastLog = DialogueEngine.smithArmorLine(p.armorName, p.armorTier)
        return true
    }

    fun craftAccessory(session: GameSession): Boolean {
        val p = session.player
        val cost = 90 + p.level * 3
        if (p.coins < cost || p.relicShards < 2) {
            session.lastLog = "Relic Smith: \"Accessory craft needs $cost coins + 2 relic shards.\""
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
            else -> {
                p.strengthArc += 1
                p.weaponMastery += 1
            }
        }
        session.lastLog = "Relic Smith: \"Forged ${p.accessoryName}.\" ${DialogueEngine.arcProgressLine(p)}"
        return true
    }

    fun openChest(session: GameSession): Boolean {
        val p = session.player
        if (!session.chestReady) {
            session.lastLog = "Scout Nyra: \"No chest marks nearby right now.\""
            return false
        }
        if (p.keys <= 0) {
            session.lastLog = "Scout Nyra: \"Chest found, but we need a key.\""
            return false
        }
        p.keys -= 1
        session.chestReady = false
        p.coins += 80 + p.level * 5
        p.gems += 1
        p.bombs += 1
        p.elixirs += 1
        p.crystalShards += 1
        if (p.level % 2 == 0) p.weaponCores += 1 else p.armorPlates += 1
        session.lastLog = "Treasure chest opened. Supplies gained: crystal shard + forge component."
        return true
    }

    fun exploreSecret(session: GameSession): Boolean {
        val p = session.player
        if (p.stage < 6) {
            session.lastLog = "Mira: \"We can trace secret routes once you reach Stage 6.\""
            return false
        }
        if (p.relicShards <= 0) {
            session.lastLog = "Mira: \"Bring relic shards. The shrine paths answer to them.\""
            return false
        }
        p.relicShards -= 1
        session.discoveredSecrets += 1
        p.coins += 55
        p.affinityMira += 1
        p.wisdomArc += 1
        p.crystalShards += 1
        session.lastLog = "Secret shrine discovered. Mira smiles and shares old route maps."
        return true
    }

    private fun resolveWeaponTrait(p: PlayerProfile): WeaponTrait {
        return when (p.heroClass) {
            HeroClass.KNIGHT -> when {
                p.weaponTier >= 8 -> WeaponTrait.LIFESTEAL
                p.weaponTier >= 4 -> WeaponTrait.PIERCE
                else -> WeaponTrait.BALANCED
            }
            HeroClass.RANGER -> when {
                p.weaponTier >= 8 -> WeaponTrait.PIERCE
                else -> WeaponTrait.CRIT
            }
            HeroClass.MYSTIC -> when {
                p.weaponTier >= 7 -> WeaponTrait.LIFESTEAL
                else -> WeaponTrait.ARCANE
            }
        }
    }
}
