package com.shiro.aetherquest

object NarrativeEngine {
    private val storyEvents = listOf(
        "evt_nyra_intro" to 4,
        "evt_lyra_archive" to 6,
        "evt_sera_vanguard" to 8,
        "evt_mira_shrine" to 10,
        "evt_kaela_court" to 12,
        "evt_rel_style" to 14,
        "evt_final_oath" to 18
    )

    fun maybeTriggerStory(session: GameSession) {
        if (session.gameOver || session.inBattle || session.pendingStoryEvent.isNotBlank()) return
        val stage = session.player.stage
        val next = storyEvents.firstOrNull { stage >= it.second && !seen(session, it.first) } ?: return
        when (next.first) {
            "evt_nyra_intro" -> setPrompt(session, next.first, "Nyra asks for help rebuilding villages after war.", "Help Nyra", "March Forward")
            "evt_lyra_archive" -> setPrompt(session, next.first, "Scholar Lyra reveals lost lore of your bloodline.", "Study With Lyra", "Ignore The Lore")
            "evt_sera_vanguard" -> setPrompt(session, next.first, "Captain Sera wants you to drill with the Vanguard.", "Train With Sera", "Travel Alone")
            "evt_mira_shrine" -> setPrompt(session, next.first, "Mira guides you to forbidden ruins and secret paths.", "Explore With Mira", "Decline")
            "evt_kaela_court" -> setPrompt(session, next.first, "Lady Kaela offers influence at the royal court.", "Accept Court Pact", "Reject The Crown")
            "evt_rel_style" -> setPrompt(session, next.first, "Choose your heart's path before fate closes in.", "Commit Single/Throuple", "Pursue Open Harem Route")
            "evt_final_oath" -> setPrompt(session, next.first, "Final Oath: rule by fear or protect all realms?", "Protect All Realms", "Rule Through Fear")
        }
        session.player.chapter = when {
            stage < 6 -> 2
            stage < 10 -> 3
            stage < 14 -> 4
            else -> 5
        }
    }

    fun applyChoice(session: GameSession, chooseA: Boolean) {
        val p = session.player
        when (session.pendingStoryEvent) {
            "evt_nyra_intro" -> if (chooseA) {
                p.affinityNyra += 2
                p.empathyArc += 1
                session.lastLog = "Nyra smiles. You protect the weak."
            } else {
                p.strengthArc += 1
                session.lastLog = "You push onward, hardened by duty."
            }
            "evt_lyra_archive" -> if (chooseA) {
                p.affinityLyra += 2
                p.wisdomArc += 2
                p.relicShards += 1
                session.lastLog = "Lyra deciphers your past. Wisdom grows."
            } else {
                p.affinityLyra -= 1
                p.strengthArc += 1
                session.lastLog = "You discard prophecy for steel."
            }
            "evt_sera_vanguard" -> if (chooseA) {
                p.affinitySera += 2
                p.strengthArc += 2
                p.attack += 1
                session.lastLog = "Sera drills you into a battlefield legend."
            } else {
                p.affinitySera -= 1
                p.wisdomArc += 1
                session.lastLog = "You avoid the Vanguard and rely on instinct."
            }
            "evt_mira_shrine" -> if (chooseA) {
                p.affinityMira += 2
                p.wisdomArc += 1
                p.empathyArc += 1
                session.discoveredSecrets += 1
                session.lastLog = "Mira reveals hidden sanctums and old vows."
            } else {
                p.affinityMira -= 1
                session.lastLog = "You close the shrine door and keep moving."
            }
            "evt_kaela_court" -> if (chooseA) {
                p.affinityKaela += 2
                p.affinityCrown += 2
                p.coins += 120
                session.lastLog = "Kaela grants influence and wealth."
            } else {
                p.affinityKaela -= 1
                p.affinityCrown -= 1
                p.empathyArc += 1
                session.lastLog = "You reject court power and stand with the people."
            }
            "evt_rel_style" -> if (chooseA) {
                p.relationshipStyle = if (topRomances(p).size >= 2) RelationshipStyle.THROUPLE else RelationshipStyle.SINGLE
                session.lastLog = "You choose devotion over excess."
            } else {
                p.relationshipStyle = RelationshipStyle.HAREM
                p.affinityCrown += 1
                session.lastLog = "You walk an open, dangerous heart path."
            }
            "evt_final_oath" -> if (chooseA) {
                p.empathyArc += 2
                p.wisdomArc += 1
                p.questsCompleted += 1
                session.lastLog = "You swear to defend every realm."
            } else {
                p.strengthArc += 2
                p.affinityCrown += 3
                session.lastLog = "You claim power with iron resolve."
            }
        }
        markSeen(session, session.pendingStoryEvent)
        clearPrompt(session)
    }

    fun resolveEnding(session: GameSession): EndingType {
        val p = session.player
        if (session.gameOver && p.lives <= 0) return EndingType.TRAGIC
        if (p.stage < 22) return EndingType.NONE

        val romanceCount = romanceCountHigh(p)
        val top = topRomances(p)
        if (p.relationshipStyle == RelationshipStyle.HAREM && romanceCount >= 4) return EndingType.HAREM
        if (p.relationshipStyle == RelationshipStyle.THROUPLE && top.size >= 2) return EndingType.THROUPLE
        if (p.relationshipStyle == RelationshipStyle.SINGLE && top.isNotEmpty()) return EndingType.ROMANTIC
        if (p.affinityCrown >= 10 && p.strengthArc > p.empathyArc) return EndingType.RUTHLESS
        if (p.empathyArc + p.wisdomArc >= 10 && p.questsCompleted >= 6) return EndingType.HEROIC
        return EndingType.LONE_WOLF
    }

    fun endingText(endingType: EndingType, p: PlayerProfile? = null): String {
        return when (endingType) {
            EndingType.ROMANTIC -> {
                val name = p?.let { topRomanceName(it) } ?: "your chosen partner"
                "Heartbound Crown: You and $name forge a new era together."
            }
            EndingType.THROUPLE -> {
                val names = p?.let { topRomances(it).take(2).joinToString(" & ") } ?: "two companions"
                "Twin Flame Pact: You rule in balance with $names."
            }
            EndingType.HAREM -> "Radiant Court: You unite all five hearts under one vow."
            EndingType.HEROIC -> "Guardian Ascendant: Every realm survives under your protection."
            EndingType.RUTHLESS -> "Iron Dominion: The world kneels beneath your command."
            EndingType.LONE_WOLF -> "Wanderer King: You save the realm but walk alone."
            EndingType.TRAGIC -> "Fallen Ember: Your journey ends before dawn."
            EndingType.NONE -> "Your journey is unfinished."
        }
    }

    fun regionName(stage: Int): String {
        return when {
            stage < 5 -> "Whispering Plains"
            stage < 10 -> "Frostwild Pass"
            stage < 15 -> "Sunken Sanctum"
            stage < 20 -> "Ashen Crown"
            else -> "Skyforge Citadel"
        }
    }

    fun topRomances(p: PlayerProfile): List<String> {
        return listOf(
            "Nyra" to p.affinityNyra,
            "Lyra" to p.affinityLyra,
            "Sera" to p.affinitySera,
            "Mira" to p.affinityMira,
            "Kaela" to p.affinityKaela
        ).sortedByDescending { it.second }
            .filter { it.second >= 6 }
            .map { it.first }
    }

    private fun topRomanceName(p: PlayerProfile): String {
        return listOf(
            "Nyra" to p.affinityNyra,
            "Lyra" to p.affinityLyra,
            "Sera" to p.affinitySera,
            "Mira" to p.affinityMira,
            "Kaela" to p.affinityKaela
        ).maxBy { it.second }.first
    }

    private fun romanceCountHigh(p: PlayerProfile): Int {
        return listOf(p.affinityNyra, p.affinityLyra, p.affinitySera, p.affinityMira, p.affinityKaela).count { it >= 8 }
    }

    private fun seen(session: GameSession, key: String): Boolean {
        return session.storyEventsSeen.split(",").any { it == key }
    }

    private fun markSeen(session: GameSession, key: String) {
        if (key.isBlank()) return
        val current = session.storyEventsSeen.split(",").filter { it.isNotBlank() }.toMutableSet()
        current.add(key)
        session.storyEventsSeen = current.joinToString(",")
    }

    private fun setPrompt(session: GameSession, key: String, prompt: String, a: String, b: String) {
        session.pendingStoryEvent = key
        session.storyPrompt = prompt
        session.choiceA = a
        session.choiceB = b
    }

    private fun clearPrompt(session: GameSession) {
        session.pendingStoryEvent = ""
        session.storyPrompt = ""
        session.choiceA = ""
        session.choiceB = ""
    }
}
