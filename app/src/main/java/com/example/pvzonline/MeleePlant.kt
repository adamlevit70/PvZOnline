package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A plant that attacks zombies in a specific distance in its row without bullets.
 */
class MeleePlant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    parent: FrameLayout,
    private val findZombie: (row: Int, plantX: Float) -> Zombie?,
    private val attackRadius: Float
) : Plant(plantImage, dmg, cooldownMs, hp, parent) {

    override fun start(row: Int) {
        super.start(row)
        job = scope.launch {
            while (isActive && hp > 0) {
                performMeleeAttack()
                delay(cooldownMs)
            }
        }
    }

    private fun performMeleeAttack() {
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)
        image.getLocationOnScreen(plantLocation)
        parent.getLocationOnScreen(parentLocation)

        val xInParent = (plantLocation[0] - parentLocation[0]).toFloat()
        val firingPointX = xInParent + image.width

        val zombie = findZombie(plantRow, firingPointX)
        if (zombie != null) {
            val zombieX = zombie.zombieImage.x
            val distance = zombieX - firingPointX

            if (distance <= attackRadius) {
                zombie.takeDmg(dmg)
            }
        }
    }
}