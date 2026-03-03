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
}

/**
 * A plant that shoots bullets at zombies in its row.
 */
class ShooterPlant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    parent: FrameLayout,
    private val findZombie: (row: Int, plantX: Float) -> Zombie?,
    private val attackRadius: Float = 0f // 0 means infinite range in the row
) : Plant(plantImage, dmg, cooldownMs, hp, parent) {

    override fun start(row: Int) {
        super.start(row)
        job = scope.launch {
            while (isActive && hp > 0) {
                performAttack()
                delay(cooldownMs)
            }
        }
    }

    private fun performAttack() {
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)
        image.getLocationOnScreen(plantLocation)
        parent.getLocationOnScreen(parentLocation)

        val xInParent = (plantLocation[0] - parentLocation[0]).toFloat()
        val yInParent = (plantLocation[1] - parentLocation[1]).toFloat()
        val firingPointX = xInParent + image.width

        val zombie = findZombie(plantRow, firingPointX)
        if (zombie != null) {
            val zombieX = zombie.zombieImage.x
            val distance = zombieX - firingPointX

            // If attackRadius is 0, it means infinite range
            if (attackRadius <= 0f || distance <= attackRadius) {
                shoot(xInParent, yInParent)
            }
        }
    }

    private fun shoot(xInParent: Float, yInParent: Float) {
        val bulletX = xInParent + image.width / 2f
        val bulletY = yInParent + image.height / 8f

        PeaBullet(parent, plantRow, bulletX, bulletY, 15f, dmg, findZombie)
    }
}

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

/**
 * A plant that generates sun points over time.
 */
class SunflowerPlant(
    plantImage: ImageView,
    cooldownMs: Long,
    hp: Int,
    parent: FrameLayout,
    private val onSunCollected: (Int) -> Unit
) : Plant(plantImage, 0, cooldownMs, hp, parent) {

    override fun start(row: Int) {
        super.start(row)
        job = scope.launch {
            while (isActive && hp > 0) {
                delay(cooldownMs)
                generateSun()
            }
        }
    }

    private fun generateSun() {
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)
        image.getLocationOnScreen(plantLocation)
        parent.getLocationOnScreen(parentLocation)

        val xInParent = (plantLocation[0] - parentLocation[0]).toFloat()
        val yInParent = (plantLocation[1] - parentLocation[1]).toFloat()

        // 2. Initialize the Sun object
        val sun = Sun(parent, onSunCollected)

        // 3. Set starting position
        val startX = xInParent + (image.width / 2f)
        val startY = yInParent
        val targetY = yInParent + (image.height * 0.6f) // Drops slightly in front of the plant

        // 4. Trigger the sun's internal spawn logic
        sun.spawn(startX, startY, targetY)
    }
}
