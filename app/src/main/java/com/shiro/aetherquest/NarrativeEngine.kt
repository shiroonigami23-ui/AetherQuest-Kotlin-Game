package com.shiro.aetherquest

object NarrativeEngine {
    private const val EVT_STAGE5 = "stage5"
    private const val EVT_STAGE10 = "stage10"
    private const val EVT_STAGE15 = "stage15"

    fun maybeTriggerStory(session: GameSession) {
        if (session.gameOver || session.inBattle) return
        val stage = session.player.stage
        when {
            stage >= 15 && !seen(session, EVT_STAGE15) -> {
                setPrompt(
                    session,
                    EVT_STAGE15,
                    "At the Ashen Crown, Nyra asks you to spare the rebel scholars.",
                    "Spare Them",
                    "Purge Them"
                )
                session.player.chapter = 4
            }
            stage >= 10 && !seen(session, EVT_STAGE10) -> {
                setPrompt(
                    session,
                    EVT_STAGE10,
                    "The Crown offers power if you abandon Nyra's cause.",
                    "Stay With Nyra",
                    "Serve The Crown"
                )
                session.player.chapter = 3
            }
            stage >= 5 && !seen(session, EVT_STAGE5) -> {
                setPrompt(
                    session,
                    EVT_STAGE5,
                    "You find Nyra wounded in the Frostwild pass.",
                    "Protect Nyra",
                    "Chase The Enemy"
                )
                session.player.chapter = 2
            }
        }
    }

    fun applyChoice(session: GameSession, chooseA: Boolean) {
        when (session.pendingStoryEvent) {
            EVT_STAGE5 -> {
                if (chooseA) {
                    session.player.affinityNyra += 2
                    session.player.affinityCrown -= 1
                    session.lastLog = "You protected Nyra. Trust deepens."
                } else {
                    session.player.affinityCrown += 1
                    session.lastLog = "You chased the enemy and gained tactical favor."
                }
            }
            EVT_STAGE10 -> {
                if (chooseA) {
                    session.player.affinityNyra += 3
                    session.player.affinityCrown -= 1
                    session.lastLog = "You stood with Nyra against the court."
                } else {
                    session.player.affinityCrown += 3
                    session.lastLog = "You pledged service to the Crown."
                }
            }
            EVT_STAGE15 -> {
                if (chooseA) {
                    session.player.affinityNyra += 2
                    session.player.questsCompleted += 1
                    session.lastLog = "You chose mercy. The realm whispers your name."
                } else {
                    session.player.affinityCrown += 2
                    session.player.attack += 2
                    session.lastLog = "You chose fear. Power answered."
                }
            }
        }
        markSeen(session, session.pendingStoryEvent)
        clearPrompt(session)
    }

    fun resolveEnding(session: GameSession): EndingType {
        val p = session.player
        if (session.gameOver && p.lives <= 0) return EndingType.TRAGIC
        if (p.stage < 20) return EndingType.NONE
        return when {
            p.affinityNyra >= 6 && p.questsCompleted >= 4 -> EndingType.ROMANTIC
            p.affinityCrown >= 6 -> EndingType.RUTHLESS
            p.questsCompleted >= 5 && p.battlesWon >= 18 -> EndingType.HEROIC
            else -> EndingType.TRAGIC
        }
    }

    fun endingText(endingType: EndingType): String {
        return when (endingType) {
            EndingType.ROMANTIC -> "Dawn Oath: You and Nyra rebuild the sky-kingdom together."
            EndingType.HEROIC -> "Guardian Ascendant: You save all regions and become the realm's shield."
            EndingType.RUTHLESS -> "Iron Throne: You conquer the world under the Crown's banner."
            EndingType.TRAGIC -> "Fallen Ember: Your legend ends, but songs remember your fight."
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
