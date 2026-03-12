package com.shiro.aetherquest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object AssetRepository {
    @Volatile
    private var loaded = false

    var heroBitmap: Bitmap? = null
        private set
    var regionPlains: Bitmap? = null
        private set
    var regionFrost: Bitmap? = null
        private set
    var regionSanctum: Bitmap? = null
        private set
    var regionAshen: Bitmap? = null
        private set
    var regionSkyforge: Bitmap? = null
        private set
    var worldVariantDrone: Bitmap? = null
        private set
    var worldVariantObserver: Bitmap? = null
        private set
    var worldVariantSentinel: Bitmap? = null
        private set
    var worldVariantSlug: Bitmap? = null
        private set
    var worldVariantEagle: Bitmap? = null
        private set
    var enemyDrone: Bitmap? = null
        private set
    var enemySentinel: Bitmap? = null
        private set
    var enemyObserver: Bitmap? = null
        private set
    var enemySteelEagle: Bitmap? = null
        private set
    var enemyMetalSlug: Bitmap? = null
        private set
    var bgDrone: Bitmap? = null
        private set
    var bgSentinel: Bitmap? = null
        private set
    var bgObserver: Bitmap? = null
        private set
    var bgSlug: Bitmap? = null
        private set
    var bgSteel: Bitmap? = null
        private set

    fun isLoaded(): Boolean = loaded

    fun preload(context: Context, onProgress: ((Int, String) -> Unit)? = null) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val appCtx = context.applicationContext
            val steps = listOf(
                "hero" to { heroBitmap = decodeBitmapSafe(appCtx, R.drawable.hero_sheet, 1) },
                "region plains" to { regionPlains = decodeBitmapSafe(appCtx, R.drawable.region_whispering_plains, 2) },
                "region frost" to { regionFrost = decodeBitmapSafe(appCtx, R.drawable.region_frostwild_pass, 2) },
                "region sanctum" to { regionSanctum = decodeBitmapSafe(appCtx, R.drawable.region_sunken_sanctum, 2) },
                "region ashen" to { regionAshen = decodeBitmapSafe(appCtx, R.drawable.region_ashen_crown, 2) },
                "region skyforge" to { regionSkyforge = decodeBitmapSafe(appCtx, R.drawable.region_skyforge_citadel, 2) },
                "world drone" to { worldVariantDrone = decodeBitmapSafe(appCtx, R.drawable.world_variant_drone, 2) },
                "world observer" to { worldVariantObserver = decodeBitmapSafe(appCtx, R.drawable.world_variant_observer, 2) },
                "world sentinel" to { worldVariantSentinel = decodeBitmapSafe(appCtx, R.drawable.world_variant_sentinel, 2) },
                "world slug" to { worldVariantSlug = decodeBitmapSafe(appCtx, R.drawable.world_variant_slug, 2) },
                "world eagle" to { worldVariantEagle = decodeBitmapSafe(appCtx, R.drawable.world_variant_eagle, 2) },
                "enemy drone" to { enemyDrone = decodeBitmapSafe(appCtx, R.drawable.enemy_drone, 1) },
                "enemy sentinel" to { enemySentinel = decodeBitmapSafe(appCtx, R.drawable.enemy_sentinel, 1) },
                "enemy observer" to { enemyObserver = decodeBitmapSafe(appCtx, R.drawable.enemy_observer, 1) },
                "enemy steel eagle" to { enemySteelEagle = decodeBitmapSafe(appCtx, R.drawable.enemy_steel_eagle, 1) },
                "enemy metal slug" to { enemyMetalSlug = decodeBitmapSafe(appCtx, R.drawable.enemy_metal_slug, 1) },
                "battle bg drone" to { bgDrone = decodeBitmapSafe(appCtx, R.drawable.enemy_bg_drone, 2) },
                "battle bg sentinel" to { bgSentinel = decodeBitmapSafe(appCtx, R.drawable.enemy_bg_sentinel, 2) },
                "battle bg observer" to { bgObserver = decodeBitmapSafe(appCtx, R.drawable.enemy_bg_observer, 2) },
                "battle bg slug" to { bgSlug = decodeBitmapSafe(appCtx, R.drawable.enemy_bg_slug, 2) },
                "battle bg steel" to { bgSteel = decodeBitmapSafe(appCtx, R.drawable.enemy_bg_steel_eagle, 2) }
            )
            steps.forEachIndexed { idx, entry ->
                entry.second.invoke()
                onProgress?.invoke(((idx + 1) * 100) / steps.size, entry.first)
            }
            loaded = true
        }
    }

    private fun decodeBitmapSafe(context: Context, resId: Int, sample: Int): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inScaled = true
                inSampleSize = sample.coerceAtLeast(1)
            }
            BitmapFactory.decodeResource(context.resources, resId, opts)
        } catch (_: Throwable) {
            null
        }
    }
}
