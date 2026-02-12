package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.*

class Plant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    private val parent: FrameLayout,
    private val findZombie: (row: Int, plantX: Float) -> Zombie?
) : LivingEntity(plantImage, dmg, cooldownMs, hp) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var attackingJob: Job? = null

    private var plantRow: Int = -1

    fun startAttacking(row: Int) {
        if (attackingJob != null) return
        plantRow = row

        attackingJob = scope.launch {
            while (hp > 0) {

                val zombie = findZombie(plantRow, image.x)

                if (zombie != null) {
                    shoot()
                }

                delay(cooldownMs)
            }
        }
    }

    private fun shoot() {
        PeaBullet(
            parent,
            plantRow,
            image.x + image.width,
            image.y,
            15f,
            dmg,
            findZombie
        )
    }

    override fun dead() {
        attackingJob?.cancel()
        scope.cancel()
        image.visibility = ImageView.GONE
        parent.removeView(image)
    }
}