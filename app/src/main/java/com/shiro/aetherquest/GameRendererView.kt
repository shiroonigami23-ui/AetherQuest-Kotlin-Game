package com.shiro.aetherquest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameRendererView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var session: GameSession? = null
        set(value) {
            field = value
            if (value != null && !value.inBattle) {
                ensureWorldState(value)
            }
            invalidate()
        }

    var onEncounterRequested: ((String) -> Unit)? = null
    var onPoiClaimed: ((String, Int, Int) -> Unit)? = null
    var onPotBroken: ((String) -> Unit)? = null

    private enum class PoiType { LOOT, POT }

    private data class Roamer(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var name: String,
        var sprite: Bitmap?
    )

    private data class Poi(
        var x: Float,
        var y: Float,
        val label: String,
        val coins: Int,
        val relics: Int,
        val type: PoiType = PoiType.LOOT,
        var claimed: Boolean = false
    )

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var alpha: Int
    )

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
    }

    private val heroBitmap: Bitmap? get() = AssetRepository.heroBitmap
    private val regionPlains: Bitmap? get() = AssetRepository.regionPlains
    private val regionFrost: Bitmap? get() = AssetRepository.regionFrost
    private val regionSanctum: Bitmap? get() = AssetRepository.regionSanctum
    private val regionAshen: Bitmap? get() = AssetRepository.regionAshen
    private val regionSkyforge: Bitmap? get() = AssetRepository.regionSkyforge
    private val worldVariantDrone: Bitmap? get() = AssetRepository.worldVariantDrone
    private val worldVariantObserver: Bitmap? get() = AssetRepository.worldVariantObserver
    private val worldVariantSentinel: Bitmap? get() = AssetRepository.worldVariantSentinel
    private val worldVariantSlug: Bitmap? get() = AssetRepository.worldVariantSlug
    private val worldVariantEagle: Bitmap? get() = AssetRepository.worldVariantEagle
    private val enemyDrone: Bitmap? get() = AssetRepository.enemyDrone
    private val enemySentinel: Bitmap? get() = AssetRepository.enemySentinel
    private val enemyObserver: Bitmap? get() = AssetRepository.enemyObserver
    private val enemySteelEagle: Bitmap? get() = AssetRepository.enemySteelEagle
    private val enemyMetalSlug: Bitmap? get() = AssetRepository.enemyMetalSlug
    private val bgDrone: Bitmap? get() = AssetRepository.bgDrone
    private val bgSentinel: Bitmap? get() = AssetRepository.bgSentinel
    private val bgObserver: Bitmap? get() = AssetRepository.bgObserver
    private val bgSlug: Bitmap? get() = AssetRepository.bgSlug
    private val bgSteel: Bitmap? get() = AssetRepository.bgSteel

    private val worldWidth = 3200f
    private val worldHeight = 2200f
    private var moveInputX = 0f
    private var moveInputY = 0f
    private var roamers = mutableListOf<Roamer>()
    private var pois = mutableListOf<Poi>()
    private var generatedStage = -1
    private var lastTickMs = 0L
    private var lastEncounterMs = 0L
    private var particles = mutableListOf<Particle>()

    fun setMoveDirection(x: Float, y: Float) {
        moveInputX = x
        moveInputY = y
    }

    fun cycleCameraMode(): CameraMode {
        val s = session ?: return CameraMode.THIRD_PERSON
        s.cameraMode = when (s.cameraMode) {
            CameraMode.TOP_DOWN -> CameraMode.THIRD_PERSON
            CameraMode.THIRD_PERSON -> CameraMode.FIRST_PERSON
            CameraMode.FIRST_PERSON -> CameraMode.TOP_DOWN
        }
        invalidate()
        return s.cameraMode
    }

    fun interact() {
        val s = session ?: return
        if (s.inBattle || pois.isEmpty()) return
        val nearest = pois.filter { !it.claimed }.minByOrNull { distance(s.player.worldX, s.player.worldY, it.x, it.y) } ?: run {
            s.lastLog = "No nearby point of interest."
            return
        }
        val dist = distance(s.player.worldX, s.player.worldY, nearest.x, nearest.y)
        if (dist > 160f) {
            s.lastLog = "Move closer to interact."
            return
        }
        nearest.claimed = true
        if (nearest.type == PoiType.POT) {
            val outcome = applyPotLoot(s)
            onPotBroken?.invoke(outcome)
        } else {
            onPoiClaimed?.invoke(nearest.label, nearest.coins, nearest.relics)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = session ?: return
        drawBackground(canvas)
        if (s.inBattle) {
            drawBattle(canvas, s)
            return
        }
        ensureWorldState(s)
        updateWorld(s)
        drawExplorationWorld(canvas, s)
        postInvalidateOnAnimation()
    }

    private fun drawBackground(canvas: Canvas) {
        val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.parseColor("#0D1A2B"), Color.parseColor("#122943"), Shader.TileMode.CLAMP)
        bgPaint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }

    private fun updateWorld(s: GameSession) {
        val now = SystemClock.elapsedRealtime()
        if (lastTickMs == 0L) {
            lastTickMs = now
            return
        }
        val dt = ((now - lastTickMs).coerceAtMost(32L)) / 1000f
        lastTickMs = now
        val speed = 320f
        val p = s.player

        if (abs(moveInputX) > 0.01f || abs(moveInputY) > 0.01f) {
            p.worldX = clamp(p.worldX + moveInputX * speed * dt, 40f, worldWidth - 40f)
            p.worldY = clamp(p.worldY + moveInputY * speed * dt, 40f, worldHeight - 40f)
        }

        roamers.forEach {
            it.x += it.vx * dt
            it.y += it.vy * dt
            if (it.x < 30f || it.x > worldWidth - 30f) it.vx *= -1f
            if (it.y < 30f || it.y > worldHeight - 30f) it.vy *= -1f
        }

        particles.forEach {
            it.x += it.vx * dt
            it.y += it.vy * dt
            if (it.x < -40f) it.x = worldWidth + 40f
            if (it.x > worldWidth + 40f) it.x = -40f
            if (it.y < -40f) it.y = worldHeight + 40f
            if (it.y > worldHeight + 40f) it.y = -40f
        }

        val closeEnemy = roamers.minByOrNull { distance(p.worldX, p.worldY, it.x, it.y) }
        val d = closeEnemy?.let { distance(p.worldX, p.worldY, it.x, it.y) } ?: Float.MAX_VALUE
        if (d < 110f && now - lastEncounterMs > 1800) {
            lastEncounterMs = now
            onEncounterRequested?.invoke(closeEnemy?.name ?: "Wilderness Foe")
        }
    }

    private fun drawExplorationWorld(canvas: Canvas, s: GameSession) {
        val p = s.player
        val camX = when (s.cameraMode) {
            CameraMode.TOP_DOWN -> clamp(p.worldX - width / 2f, 0f, worldWidth - width)
            CameraMode.THIRD_PERSON -> clamp(p.worldX - width / 2f, 0f, worldWidth - width)
            CameraMode.FIRST_PERSON -> clamp(p.worldX - width / 2f, 0f, worldWidth - width)
        }
        val camY = when (s.cameraMode) {
            CameraMode.TOP_DOWN -> clamp(p.worldY - height / 2f, 0f, worldHeight - height)
            CameraMode.THIRD_PERSON -> clamp(p.worldY - height * 0.68f, 0f, worldHeight - height)
            CameraMode.FIRST_PERSON -> clamp(p.worldY - height * 0.8f, 0f, worldHeight - height)
        }

        drawRegionBackground(canvas, s.player.stage, camX, camY)
        drawGrid(canvas, camX, camY)
        drawAtmosphere(canvas, camX, camY)

        pois.forEach { poi ->
            if (poi.claimed) return@forEach
            val sx = poi.x - camX
            val sy = poi.y - camY
            shapePaint.style = Paint.Style.FILL
            if (poi.type == PoiType.POT) {
                shapePaint.color = Color.parseColor("#B45309")
                canvas.drawRoundRect(RectF(sx - 14f, sy - 18f, sx + 14f, sy + 20f), 8f, 8f, shapePaint)
                shapePaint.color = Color.parseColor("#F59E0B")
                canvas.drawCircle(sx, sy - 20f, 9f, shapePaint)
            } else {
                shapePaint.color = if (poi.relics > 0) Color.parseColor("#A855F7") else Color.parseColor("#10B981")
                canvas.drawCircle(sx, sy, 22f, shapePaint)
            }
            textPaint.textSize = 18f
            canvas.drawText(poi.label, sx + 28f, sy + 5f, textPaint)
        }

        roamers.forEach { enemy ->
            val sx = enemy.x - camX
            val sy = enemy.y - camY
            val dst = RectF(sx - 36f, sy - 36f, sx + 36f, sy + 36f)
            enemy.sprite?.let {
                canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), dst, null)
            } ?: run {
                shapePaint.color = Color.RED
                canvas.drawCircle(sx, sy, 26f, shapePaint)
            }
        }

        drawHeroByCameraMode(canvas, s)

        drawWorldHud(canvas, s)
    }

    private fun drawHeroByCameraMode(canvas: Canvas, s: GameSession) {
        val moving = abs(moveInputX) > 0.01f || abs(moveInputY) > 0.01f
        val frame = if (moving) ((SystemClock.elapsedRealtime() / 120L) % 3L).toInt() else 1
        val src = Rect(2 + frame * 16, 2, 16 + frame * 16, 18)
        val bob = if (moving) ((SystemClock.elapsedRealtime() % 300L) / 300f) * 6f else 0f
        val hero = heroBitmap
        when (s.cameraMode) {
            CameraMode.TOP_DOWN -> {
                val heroDst = RectF(width / 2f - 40f, height / 2f - 58f - bob, width / 2f + 40f, height / 2f + 42f - bob)
                if (hero != null) {
                    canvas.drawBitmap(hero, src, heroDst, null)
                } else {
                    shapePaint.color = Color.parseColor("#22D3EE")
                    canvas.drawCircle(width / 2f, height / 2f, 22f, shapePaint)
                }
            }
            CameraMode.THIRD_PERSON -> {
                val heroDst = RectF(width / 2f - 64f, height * 0.68f - 72f - bob, width / 2f + 64f, height * 0.68f + 88f - bob)
                if (hero != null) {
                    canvas.drawBitmap(hero, src, heroDst, null)
                } else {
                    shapePaint.color = Color.parseColor("#22D3EE")
                    canvas.drawCircle(width / 2f, height * 0.68f, 28f, shapePaint)
                }
            }
            CameraMode.FIRST_PERSON -> {
                shapePaint.color = Color.parseColor("#334155")
                canvas.drawRect(width * 0.46f, height * 0.78f, width * 0.56f, height * 0.94f, shapePaint)
                shapePaint.color = Color.parseColor("#94A3B8")
                canvas.drawCircle(width / 2f, height / 2f, 6f, shapePaint)
                canvas.drawCircle(width / 2f, height / 2f, 2f, Paint().apply { color = Color.WHITE })
            }
        }
    }

    private fun drawWorldHud(canvas: Canvas, s: GameSession) {
        shapePaint.color = Color.parseColor("#CC0F172A")
        canvas.drawRoundRect(RectF(20f, 16f, width - 20f, 126f), 16f, 16f, shapePaint)
        textPaint.textSize = 24f
        canvas.drawText("Zone: ${NarrativeEngine.regionName(s.player.stage)} | Explore, collect POIs, avoid or engage roaming foes", 34f, 52f, textPaint)
        textPaint.textSize = 21f
        val lootPoiLeft = pois.count { !it.claimed && it.type == PoiType.LOOT }
        val potsLeft = pois.count { !it.claimed && it.type == PoiType.POT }
        canvas.drawText("POIs left: $lootPoiLeft | Pots: $potsLeft | Roamers: ${roamers.size} | Cam: ${s.cameraMode} | Position: ${s.player.worldX.toInt()},${s.player.worldY.toInt()}", 34f, 88f, textPaint)

        val miniW = 220f
        val miniH = 140f
        val miniX = width - miniW - 22f
        val miniY = 140f
        shapePaint.color = Color.parseColor("#BB020617")
        canvas.drawRoundRect(RectF(miniX, miniY, miniX + miniW, miniY + miniH), 12f, 12f, shapePaint)
        val px = miniX + (s.player.worldX / worldWidth) * miniW
        val py = miniY + (s.player.worldY / worldHeight) * miniH
        shapePaint.color = Color.parseColor("#22D3EE")
        canvas.drawCircle(px, py, 6f, shapePaint)
        shapePaint.color = Color.parseColor("#F87171")
        roamers.forEach {
            val ex = miniX + (it.x / worldWidth) * miniW
            val ey = miniY + (it.y / worldHeight) * miniH
            canvas.drawCircle(ex, ey, 4f, shapePaint)
        }
    }

    private fun drawAtmosphere(canvas: Canvas, camX: Float, camY: Float) {
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        particles.forEach {
            val sx = it.x - camX
            val sy = it.y - camY
            if (sx < -60f || sy < -60f || sx > width + 60f || sy > height + 60f) return@forEach
            glow.color = Color.argb(it.alpha, 184, 230, 255)
            canvas.drawCircle(sx, sy, it.radius, glow)
        }

        // Subtle foreground mist ribbon for depth.
        val t = SystemClock.elapsedRealtime() / 1000f
        val mistY = height * 0.72f + kotlin.math.sin(t).toFloat() * 8f
        val mistPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mistPaint.shader = LinearGradient(
            0f,
            mistY,
            0f,
            mistY + 120f,
            Color.argb(70, 125, 211, 252),
            Color.argb(0, 125, 211, 252),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, mistY, width.toFloat(), mistY + 120f, mistPaint)
    }

    private fun drawRegionBackground(canvas: Canvas, stage: Int, camX: Float, camY: Float) {
        val bitmap = currentRegionBitmap(stage) ?: return
        val maxSrcX = max(0f, bitmap.width - width.toFloat())
        val maxSrcY = max(0f, bitmap.height - height.toFloat())
        val srcX = ((camX / max(1f, worldWidth - width)) * maxSrcX).toInt()
        val srcY = ((camY / max(1f, worldHeight - height)) * maxSrcY).toInt()
        val src = Rect(srcX, srcY, min(bitmap.width, srcX + width), min(bitmap.height, srcY + height))
        val dst = RectF(0f, 0f, width.toFloat(), height.toFloat())
        shapePaint.alpha = 155
        canvas.drawBitmap(bitmap, src, dst, shapePaint)
        val variant = currentWorldVariant(stage)
        if (variant != null) {
            val src2 = Rect(0, 0, variant.width, variant.height)
            shapePaint.alpha = 48
            canvas.drawBitmap(variant, src2, dst, shapePaint)
        }
        shapePaint.alpha = 255
    }

    private fun drawGrid(canvas: Canvas, camX: Float, camY: Float) {
        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 1.2f
        shapePaint.color = Color.parseColor("#203B4D")
        val gap = 120f
        var x = -(camX % gap)
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), shapePaint)
            x += gap
        }
        var y = -(camY % gap)
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, shapePaint)
            y += gap
        }
        shapePaint.style = Paint.Style.FILL
    }

    private fun drawBattle(canvas: Canvas, s: GameSession) {
        val enemy = s.enemy ?: return
        val battleBg = selectEnemyBackdrop(enemy.name) ?: currentRegionBitmap(s.player.stage)
        if (battleBg != null) {
            val src = Rect(0, 0, battleBg.width, battleBg.height)
            val dst = RectF(0f, 0f, width.toFloat(), height.toFloat())
            shapePaint.alpha = 110
            canvas.drawBitmap(battleBg, src, dst, shapePaint)
            shapePaint.alpha = 255
        }
        drawBattleAura(canvas, enemy)
        textPaint.textSize = 42f
        canvas.drawText(if (enemy.isBoss) "Boss Encounter" else "Battle", 42f, 72f, textPaint)

        val t = SystemClock.elapsedRealtime()
        val bob = (t % 700L) / 700f * 10f
        val groundY = height * 0.74f
        shapePaint.color = Color.parseColor("#0B1220")
        canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), shapePaint)

        val heroDst = RectF(width * 0.12f, groundY - 210f - bob, width * 0.32f, groundY - bob)
        val heroFrame = ((t / 140L) % 3L).toInt()
        val heroSrc = Rect(2 + heroFrame * 16, 2, 16 + heroFrame * 16, 18)
        val hero = heroBitmap
        if (hero != null) {
            canvas.drawBitmap(hero, heroSrc, heroDst, null)
        } else {
            shapePaint.color = Color.parseColor("#38BDF8")
            canvas.drawRoundRect(heroDst, 18f, 18f, shapePaint)
        }

        val enemyDst = RectF(width * 0.67f, groundY - 230f + bob, width * 0.88f, groundY + bob)
        val enemyBitmap = selectEnemyBitmap(enemy.name)
        enemyBitmap?.let {
            val src = Rect(0, 0, it.width, it.height)
            canvas.drawBitmap(it, src, enemyDst, null)
        }

        drawHealthBar(canvas, 42f, 110f, width * 0.38f, s.player.hp, s.player.maxHp, "Hero")
        drawHealthBar(canvas, width * 0.55f, 110f, width * 0.38f, enemy.hp, enemy.maxHp, enemy.name)
        textPaint.textSize = 26f
        canvas.drawText("Enemy Intent: ${s.enemyIntent}", 42f, groundY + 44f, textPaint)
        canvas.drawText("Combo: ${s.comboCount} | Bleed: ${s.playerBleedTurns}", 42f, groundY + 78f, textPaint)
    }

    private fun drawBattleAura(canvas: Canvas, enemy: Enemy) {
        val pulse = ((SystemClock.elapsedRealtime() % 900L) / 900f)
        val alpha = if (enemy.isBoss) (95 + pulse * 80).toInt() else (50 + pulse * 40).toInt()
        val aura = Paint(Paint.ANTI_ALIAS_FLAG)
        aura.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.argb(alpha, 239, 68, 68),
            Color.argb(0, 15, 23, 42),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), aura)
    }

    private fun drawHealthBar(canvas: Canvas, x: Float, y: Float, widthBar: Float, hp: Int, maxHp: Int, label: String) {
        val ratio = if (maxHp <= 0) 0f else hp.toFloat() / maxHp
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = Color.parseColor("#334155")
        canvas.drawRoundRect(RectF(x, y, x + widthBar, y + 24f), 8f, 8f, shapePaint)
        shapePaint.color = Color.parseColor("#22C55E")
        canvas.drawRoundRect(RectF(x, y, x + widthBar * min(1f, ratio), y + 24f), 8f, 8f, shapePaint)
        textPaint.textSize = 24f
        canvas.drawText("$label $hp/$maxHp", x, y - 10f, textPaint)
    }

    private fun ensureWorldState(s: GameSession) {
        val p = s.player
        if (p.worldX < 0f || p.worldY < 0f) {
            p.worldX = worldWidth * 0.5f
            p.worldY = worldHeight * 0.5f
        }
        if (generatedStage == p.stage) return
        generatedStage = p.stage
        val r = Random(p.stage * 991)
        roamers = mutableListOf()
        val enemyCount = min(10, 4 + p.stage / 2)
        repeat(enemyCount) { idx ->
            roamers += Roamer(
                x = 120f + r.nextFloat() * (worldWidth - 240f),
                y = 120f + r.nextFloat() * (worldHeight - 240f),
                vx = (if (r.nextBoolean()) 1 else -1) * (45f + r.nextFloat() * 120f),
                vy = (if (r.nextBoolean()) 1 else -1) * (45f + r.nextFloat() * 120f),
                name = "Wild ${idx + 1}",
                sprite = listOf(enemyDrone, enemySentinel, enemyObserver, enemySteelEagle, enemyMetalSlug).random(r)
            )
        }
        pois = mutableListOf(
            Poi(360f, 320f, "Camp Cache", 90, 0, PoiType.LOOT),
            Poi(worldWidth - 450f, 360f, "Sky Relic", 35, 1, PoiType.LOOT),
            Poi(620f, worldHeight - 380f, "Forgotten Shrine", 45, 1, PoiType.LOOT),
            Poi(worldWidth - 720f, worldHeight - 440f, "Bandit Chest", 120, 0, PoiType.LOOT),
            Poi(worldWidth * 0.5f, worldHeight * 0.25f, "Lore Obelisk", 60, 0, PoiType.LOOT)
        )
        repeat(8) { idx ->
            pois += Poi(
                x = 160f + r.nextFloat() * (worldWidth - 320f),
                y = 180f + r.nextFloat() * (worldHeight - 360f),
                label = "Breakable Pot ${idx + 1}",
                coins = 0,
                relics = 0,
                type = PoiType.POT
            )
        }
        particles = mutableListOf()
        repeat(90) {
            particles += Particle(
                x = r.nextFloat() * worldWidth,
                y = r.nextFloat() * worldHeight,
                vx = -8f + r.nextFloat() * 16f,
                vy = -5f + r.nextFloat() * 10f,
                radius = 1.5f + r.nextFloat() * 4.5f,
                alpha = 40 + r.nextInt(120)
            )
        }
    }

    private fun selectEnemyBitmap(name: String): Bitmap? {
        val n = name.lowercase()
        return when {
            n.contains("ashfang") -> enemyDrone
            n.contains("warden") -> enemySentinel
            n.contains("raider") -> enemyObserver
            n.contains("hound") -> enemySteelEagle
            n.contains("acolyte") -> enemyMetalSlug
            n.contains("titan") -> enemySentinel
            else -> listOf(enemyDrone, enemySentinel, enemyObserver, enemySteelEagle, enemyMetalSlug).random()
        }
    }

    private fun currentRegionBitmap(stage: Int): Bitmap? {
        return when {
            stage < 5 -> regionPlains
            stage < 10 -> regionFrost
            stage < 15 -> regionSanctum
            stage < 20 -> regionAshen
            else -> regionSkyforge
        }
    }

    private fun selectEnemyBackdrop(name: String): Bitmap? {
        val n = name.lowercase()
        return when {
            n.contains("ashfang") || n.contains("drone") -> bgDrone
            n.contains("warden") || n.contains("sentinel") -> bgSentinel
            n.contains("raider") || n.contains("observer") -> bgObserver
            n.contains("hound") || n.contains("steel") -> bgSteel
            n.contains("acolyte") || n.contains("slug") -> bgSlug
            n.contains("titan") -> bgSentinel
            else -> null
        }
    }

    private fun currentWorldVariant(stage: Int): Bitmap? {
        return when (stage % 5) {
            0 -> worldVariantDrone
            1 -> worldVariantObserver
            2 -> worldVariantSentinel
            3 -> worldVariantSlug
            else -> worldVariantEagle
        }
    }

    private fun clamp(v: Float, lo: Float, hi: Float): Float = min(hi, max(lo, v))

    private fun distance(ax: Float, ay: Float, bx: Float, by: Float): Float = hypot(ax - bx, ay - by)

    private fun applyPotLoot(s: GameSession): String {
        val p = s.player
        val stageBoost = (p.stage / 3).coerceAtLeast(0)
        val roll = Random.nextInt(100)
        return when {
            roll < 38 -> {
                val coins = 12 + Random.nextInt(10, 36) + stageBoost * 2
                p.coins += coins
                "Pot shattered: +$coins coins."
            }
            roll < 63 -> {
                p.potions += 1
                "Pot shattered: +1 potion."
            }
            roll < 76 -> {
                p.elixirs += 1
                "Pot shattered: +1 elixir."
            }
            roll < 84 -> {
                p.bombs += 1
                "Pot shattered: +1 bomb."
            }
            roll < 90 -> {
                p.crystalShards += 1
                "Pot shattered: +1 crystal shard."
            }
            roll < 94 -> {
                p.weaponCores += 1
                "Pot shattered: +1 weapon core."
            }
            roll < 97 -> {
                p.armorPlates += 1
                "Pot shattered: +1 armor plate."
            }
            else -> {
                when (Random.nextInt(3)) {
                    0 -> {
                        s.furyBuffTurns = max(s.furyBuffTurns, 3)
                        "Pot shattered: Fury Brew buff active (3 turns, +attack)."
                    }
                    1 -> {
                        s.stoneSkinBuffTurns = max(s.stoneSkinBuffTurns, 3)
                        "Pot shattered: Stone Skin buff active (3 turns, +defense)."
                    }
                    else -> {
                        s.fortuneBuffTurns = max(s.fortuneBuffTurns, 3)
                        "Pot shattered: Fortune buff active (3 turns, +loot)."
                    }
                }
            }
        }
    }
}
