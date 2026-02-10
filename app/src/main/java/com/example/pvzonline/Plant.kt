package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Plant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    private val findZombie: (row: Int, plantX: Float) -> Zombie?
) : LivingEntity(plantImage, dmg, cooldownMs, hp) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var attackingJob: Job? = null

    fun startAttacking(row : Int) {
        if (attackingJob != null) return // prevent duplicates

        attackingJob = scope.launch {
            while (hp > 0) {
                val zombie = findZombie(row, image.x)
                if (zombie != null) {
                    attack(zombie)
                }
                delay(cooldownMs)
            }
        }
    }

    fun attack(zombie: Zombie) {
        zombie.takeDmg(dmg)
    }

    override fun dead() {
        attackingJob?.cancel()
        image.visibility = ImageView.GONE  // Hide it rather than delete
    }
}
