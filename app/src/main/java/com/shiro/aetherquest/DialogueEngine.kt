package com.shiro.aetherquest

import kotlin.random.Random

object DialogueEngine {
    private fun pick(list: List<String>): String = list[Random.nextInt(list.size)]

    fun enemyEncounter(enemyName: String, stage: Int, isBoss: Boolean): String {
        val bossLines = listOf(
            "$enemyName: \"At last, a worthy challenger.\"",
            "$enemyName: \"Kneel, wanderer. This gate is mine.\"",
            "$enemyName: \"Your story ends in my shadow.\""
        )
        val normalLines = listOf(
            "$enemyName growls: \"Coin or blood. Choose.\"",
            "$enemyName hisses: \"You smell like campfire and fear.\"",
            "$enemyName sneers: \"Another hero to break.\""
        )
        val regionHint = when {
            stage < 5 -> "A shepherd whispers nearby, \"Bandits crossed the ridge at dawn.\""
            stage < 10 -> "A scout warns, \"Frostwild raiders strike from the white fog.\""
            stage < 15 -> "A relic hunter says, \"Sanctum spirits hate steel and lies.\""
            stage < 20 -> "A veteran mutters, \"Ashen mercs sell maps, then sell your grave.\""
            else -> "A sentinel priest says, \"Skyforge judges all oaths.\""
        }
        return "${if (isBoss) pick(bossLines) else pick(normalLines)} $regionHint"
    }

    fun enemyIntentLine(enemyName: String, intent: EnemyIntent): String {
        return when (intent) {
            EnemyIntent.ATTACK -> "$enemyName snarls and rushes in."
            EnemyIntent.HEAVY -> "$enemyName winds up a crushing blow."
            EnemyIntent.GUARD -> "$enemyName raises guard and watches your feet."
            EnemyIntent.DRAIN -> "$enemyName chants a siphon rite."
            EnemyIntent.ENRAGE -> "$enemyName roars and burns with fury."
        }
    }

    fun questGiverLine(stage: Int, completed: Int): String {
        val local = when {
            stage < 5 -> "Quartermaster Rook"
            stage < 10 -> "Captain Halla"
            stage < 15 -> "Archivist Lyra"
            stage < 20 -> "Marshal Sera"
            else -> "High Seer Kaela"
        }
        val loyalty = listOf(
            "Your squad salutes as one: \"For $local and for the Commander!\"",
            "A lieutenant bows: \"We'll hold the line until your return.\"",
            "Scouts report: \"People trust your banner more than the nobles now.\""
        )
        return "$local: \"Good work. That makes $completed contracts closed. Keep pushing the front.\" ${pick(loyalty)}"
    }

    fun courtPoliticsLine(stage: Int): String {
        val lines = listOf(
            "Court Clerk: \"Grain taxes rise while ministers feast. Greed wears silk here.\"",
            "Guard Captain: \"Three houses bribed judges this week. Truth is expensive in this hall.\"",
            "Commoner Petition: \"We need justice, not speeches and coin-purses.\""
        )
        return "${pick(lines)} Region-${NarrativeEngine.regionName(stage)} trembles under policy and ambition."
    }

    fun romanceDevotionLine(name: String): String {
        val lines = listOf(
            "$name: \"My heart is not a trophy. It is yours because you earned my trust.\"",
            "$name: \"I choose you, not your crown.\"",
            "$name smiles: \"Where you walk, I walk. Not behind you, beside you.\""
        )
        return pick(lines)
    }

    fun shopPurchase(item: String, cost: Int): String {
        val lines = listOf(
            "Merchant Pavo: \"$item, fresh stock. $cost coins.\"",
            "Trader Nyx: \"Smart buy. $item keeps heroes alive.\"",
            "Vendor Iri: \"$item packed and sealed. No refunds.\""
        )
        return pick(lines)
    }

    fun shopFail(item: String, cost: Int): String {
        val lines = listOf(
            "Merchant Pavo: \"Need $cost coins for $item.\"",
            "Trader Nyx: \"Come back richer, then we talk.\"",
            "Vendor Iri: \"You are short. Count again.\""
        )
        return pick(lines)
    }

    fun smithWeaponLine(weaponName: String, tier: Int, trait: WeaponTrait): String {
        val lines = listOf(
            "Blacksmith Toren: \"$weaponName is now Tier $tier. Trait tuned to $trait.\"",
            "Forge Master Rhea: \"Edge hardened. $weaponName will bite deeper.\"",
            "Smith Toren laughs: \"Don't swing this in a tavern unless you hate tables.\""
        )
        return pick(lines)
    }

    fun smithArmorLine(armorName: String, tier: Int): String {
        val lines = listOf(
            "Armorer Brunn: \"$armorName reinforced to Tier $tier.\"",
            "Armorer Brunn: \"Hit it with a hammer. If it sings, it's ready.\"",
            "Brunn grins: \"Now even ogres need permission to bruise you.\""
        )
        return pick(lines)
    }

    fun campRestLine(): String {
        val lines = listOf(
            "Campfire night: mugs clink, jokes fly, and the watch rotates under cold stars.",
            "You rest by the fire while a bard tells a terrible joke about dragons and taxes.",
            "The camp brewmaster hands you warm ale: \"One sip for courage, one for tomorrow.\""
        )
        return pick(lines)
    }

    fun npcJokeLine(): String {
        val jokes = listOf(
            "Joke: \"Why do mages never play cards? Too many cheaters with scry.\"",
            "Joke: \"A knight, ranger, and mystic walk into a forge... only one pays the bill.\"",
            "Joke: \"Bandits fear spreadsheets; every coin gets audited.\""
        )
        return pick(jokes)
    }

    fun arcProgressLine(p: PlayerProfile): String {
        val dominant = listOf(
            "Strength" to p.strengthArc,
            "Wisdom" to p.wisdomArc,
            "Empathy" to p.empathyArc
        ).maxBy { it.second }.first
        return when (dominant) {
            "Strength" -> "Arc: You are becoming a frontline war-leader."
            "Wisdom" -> "Arc: You are becoming a strategist and lore master."
            else -> "Arc: You are becoming a protector people trust."
        }
    }
}
