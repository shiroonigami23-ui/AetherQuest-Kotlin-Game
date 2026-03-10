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
        session.lastLog = "Armor reinforced to Tier ${p.armorTier}."
        return true
    }
}
