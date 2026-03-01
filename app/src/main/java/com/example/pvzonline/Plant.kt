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
                // Get absolute X of the firing point (front of the plant)
                val plantLocation = IntArray(2)
                val parentLocation = IntArray(2)
                image.getLocationOnScreen(plantLocation)
                parent.getLocationOnScreen(parentLocation)
                
                val xInParent = plantLocation[0] - parentLocation[0]
                val firingPointX = xInParent + image.width

                // Pass the absolute X to findZombie
                val zombie = findZombie(plantRow, firingPointX.toFloat())

                if (zombie != null) {
                    shoot()
                }

                delay(cooldownMs)
            }
        }
    }

    private fun shoot() {
        // Convert the plant's pos to FrameLayout coords system
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)

        image.getLocationOnScreen(plantLocation)
        parent.getLocationOnScreen(parentLocation)

        val xInParent = plantLocation[0] - parentLocation[0] + image.width / 2
        val yInParent = plantLocation[1] - parentLocation[1] + image.height / 8

        PeaBullet(
            parent,
            plantRow,
            xInParent.toFloat(),
            yInParent.toFloat(),
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