package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.*

/**
 * Base class for all plants.
 * Plants that don't do anything (like Wall-nuts) can use this class directly
 */
open class Plant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    protected val parent: FrameLayout
) : LivingEntity(plantImage, dmg, cooldownMs, hp) {

    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    protected var plantRow: Int = -1
    protected var job: Job? = null

    open fun start(row: Int) {
        this.plantRow = row
    }

    override fun dead() {
        job?.cancel()
        scope.cancel()
        image.visibility = ImageView.GONE
        super.dead()
    }

    fun pause() {
        job?.cancel()
    }
}