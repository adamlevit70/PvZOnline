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

                // Destroy if out of screen
                if (bulletImage.x > parent.width) {
                    destroy()
                    return
                }

                bulletImage.postDelayed(this, 16)
            }
        })
    }

    private fun checkCollision() {
        // Use actual width or fallback
        val bulletWidth = if (bulletImage.width > 0) bulletImage.width else 60
        
        // Define a tighter hitbox for the bullet
        val bulletHitboxLeft = bulletImage.x + bulletWidth * 0.2f
        val bulletHitboxRight = bulletImage.x + bulletWidth * 0.8f

        // Get the target zombie
        // We use the bullet's center for the initial search to avoid picking a zombie behind the bullet
        val searchX = bulletImage.x + bulletWidth / 2
        val zombie = findZombie(row, searchX) ?: return

        val zombieWidth = zombie.zombieImage.width
        
        // Adjust the zombie's hitbox
        // The hitbox = the middle 40% of the image
        val zombieHitboxLeft = zombie.zombieImage.x + zombieWidth * 0.4f
        val zombieHitboxRight = zombie.zombieImage.x + zombieWidth * 0.8f


        val isColliding = bulletHitboxRight >= zombieHitboxLeft &&
                bulletHitboxLeft <= zombieHitboxRight

        if (isColliding) {
            zombie.takeDmg(damage)
            destroy()
        }
    }

    private fun destroy() {
        isActive = false
        parent.removeView(bulletImage)
    }
}