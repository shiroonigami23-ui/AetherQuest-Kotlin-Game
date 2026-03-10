package com.shiro.aetherquest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class GameRendererView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var session: GameSession? = null
        set(value) {
            field = value
            invalidate()
        }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = session ?: return
        drawBackground(canvas)
        if (s.inBattle) drawBattle(canvas, s) else drawMap(canvas, s)
    }

    private fun drawBackground(canvas: Canvas) {
        val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.parseColor("#0D1A2B"), Color.parseColor("#122943"), Shader.TileMode.CLAMP)
        bgPaint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }

    private fun drawMap(canvas: Canvas, s: GameSession) {
        textPaint.textSize = 46f
        canvas.drawText("Expedition Map", 42f, 72f, textPaint)
        val totalNodes = 10
        val left = 70f
        val right = width - 70f
        val top = height * 0.38f
        val step = (right - left) / (totalNodes - 1)

        shapePaint.style = Paint.Style.STROKE
        shapePaint.strokeWidth = 7f
        shapePaint.color = Color.parseColor("#5EAEE6")
        canvas.drawLine(left, top, right, top, shapePaint)

        shapePaint.style = Paint.Style.FILL
        for (i in 1..totalNodes) {
            val x = left + (i - 1) * step
            val stageNum = s.player.stage + i - 1
            shapePaint.color = when {
                i == 1 -> Color.parseColor("#34D399")
                stageNum % 5 == 0 -> Color.parseColor("#EF4444")
                else -> Color.parseColor("#93C5FD")
            }
            canvas.drawCircle(x, top, 22f, shapePaint)
        }

        textPaint.textSize = 30f
        canvas.drawText("Current Stage: ${s.player.stage}", 42f, top + 90f, textPaint)
        canvas.drawText("Tap NEXT to continue your quest", 42f, top + 138f, textPaint)

        shapePaint.color = Color.parseColor("#0B1220")
        val hero = RectF(120f, top + 170f, 220f, top + 340f)
        canvas.drawRoundRect(hero, 14f, 14f, shapePaint)

        shapePaint.color = Color.parseColor("#EAB308")
        canvas.drawRect(145f, top + 130f, 170f, top + 250f, shapePaint)
        textPaint.textSize = 26f
        canvas.drawText("You", 128f, top + 370f, textPaint)
    }

    private fun drawBattle(canvas: Canvas, s: GameSession) {
        val enemy = s.enemy ?: return
        textPaint.textSize = 42f
        canvas.drawText(if (enemy.isBoss) "Boss Encounter" else "Battle", 42f, 72f, textPaint)

        val groundY = height * 0.74f
        shapePaint.style = Paint.Style.FILL
        shapePaint.color = Color.parseColor("#0B1220")
        canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), shapePaint)

        val heroRect = RectF(width * 0.12f, groundY - 210f, width * 0.32f, groundY)
        shapePaint.color = Color.parseColor("#111827")
        canvas.drawRoundRect(heroRect, 18f, 18f, shapePaint)
        shapePaint.color = Color.parseColor("#38BDF8")
        canvas.drawRect(heroRect.right - 18f, heroRect.top - 80f, heroRect.right + 10f, heroRect.top + 20f, shapePaint)

        val enemyRect = RectF(width * 0.67f, groundY - 230f, width * 0.88f, groundY)
        shapePaint.color = if (enemy.isBoss) Color.parseColor("#7F1D1D") else Color.parseColor("#1F2937")
        canvas.drawRoundRect(enemyRect, 18f, 18f, shapePaint)
        shapePaint.color = Color.parseColor("#F87171")
        canvas.drawRect(enemyRect.left - 10f, enemyRect.top - 70f, enemyRect.left + 14f, enemyRect.top + 40f, shapePaint)

        drawHealthBar(canvas, 42f, 110f, width * 0.38f, s.player.hp, s.player.maxHp, "Hero")
        drawHealthBar(canvas, width * 0.55f, 110f, width * 0.38f, enemy.hp, enemy.maxHp, enemy.name)

        textPaint.textSize = 28f
        canvas.drawText("Class: ${s.player.heroClass}  Lvl ${s.player.level}", 42f, groundY + 46f, textPaint)
        canvas.drawText("Potions: ${s.player.potions}  Skills: ${s.player.skillCharges}", 42f, groundY + 84f, textPaint)
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
}
