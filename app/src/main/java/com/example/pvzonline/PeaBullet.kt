package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView

class PeaBullet(
    private val parent: FrameLayout,
    private val row: Int,
    startX: Float,
    startY: Float,
    private val speed: Float = 12f,
    private val damage: Int = 20,
    private val findZombie: (row: Int, posX: Float) -> Zombie?
) {

    private val bulletImage = ImageView(parent.context).apply {
        setImageResource(R.drawable.peashooter_bullet)
        layoutParams = FrameLayout.LayoutParams(60, 60)
        x = startX
        y = startY
    }

    private var isActive = true

    init {
        parent.addView(bulletImage)
        checkCollision()
        startMoving()
    }

    private fun startMoving() {
        bulletImage.post(object : Runnable {
            override fun run() {
                if (!isActive) return

                bulletImage.x += speed

                checkCollision()

                // Despawn if out of screen
                if (bulletImage.x > parent.width) {
                    destroy()
                    return
                }

                bulletImage.postDelayed(this, 16)
            }
        })
    }

    private fun checkCollision() {
        val bulletLeft = bulletImage.x
        val bulletRight = bulletImage.x + bulletImage.width

        val zombie = findZombie(row, bulletLeft) ?: return

        val zombieLeft = zombie.zombieImage.x
        val zombieRight = zombie.zombieImage.x + zombie.zombieImage.width


        val isColliding = bulletRight >= zombieLeft &&
                bulletLeft <= zombieRight

        if (isColliding) {
            println("HIT ZOMBIE")
            zombie.takeDmg(damage)
            destroy()
        }
    }

    private fun destroy() {
        isActive = false
        parent.removeView(bulletImage)
    }
}