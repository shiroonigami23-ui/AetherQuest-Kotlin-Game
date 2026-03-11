package com.shiro.aetherquest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RpgCoreTest {
    @Test
    fun storyModeAutoResolvesBattle() {
        val session = GameFactory.newSession(HeroClass.KNIGHT, DifficultyMode.STORY)
        BattleEngine.startBattle(session)
        assertFalse(session.inBattle)
        assertTrue(session.player.stage >= 2)
    }

    @Test
    fun weaponUpgradeIncreasesTierAndMasteryWhenAffordable() {
        val session = GameFactory.newSession(HeroClass.RANGER, DifficultyMode.EASY)
        session.player.coins = 500
        session.player.gems = 10
        session.player.weaponCores = 3
        val upgraded = CampEngine.upgradeWeapon(session)
        assertTrue(upgraded)
        assertTrue(session.player.weaponTier >= 1)
        assertTrue(session.player.weaponMastery >= 2)
    }

    @Test
    fun narrativeTopRomancesIncludesNewCharacters() {
        val session = GameFactory.newSession(HeroClass.MYSTIC, DifficultyMode.HARD)
        session.player.affinityVeya = 7
        session.player.affinityIris = 5
        session.player.affinityNyra = 8

        val top = NarrativeEngine.topRomances(session.player)
        assertTrue(top.contains("Nyra"))
        assertTrue(top.contains("Veya"))
        assertFalse(top.contains("Iris"))
    }
}
