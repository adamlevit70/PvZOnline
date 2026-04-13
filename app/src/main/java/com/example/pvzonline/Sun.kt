package com.example.pvzonline

import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.UUID

class Sun(
    private val parent: FrameLayout,
    private val onCollected: (amount: Int, id: String?) -> Unit
) {

    val imageView = ImageView(parent.context)
    private val value = 25
    private val id = UUID.randomUUID().toString()

    init {
        imageView.setImageResource(R.drawable.sun)

        val size = 200
        val params = FrameLayout.LayoutParams(size, size)
        imageView.layoutParams = params

        imageView.elevation = 6f  // sun over the plants and zombies

        parent.addView(imageView)

        // Setup listener
        imageView.setOnClickListener {
            collect()
        }
    }

    fun topSpawn(startX: Float, targetY: Float) {
        // Spawn the sun at the top of the screen
        imageView.x = startX
        imageView.y = 0f

        // Animate the fall of the sun
        imageView.animate()
            .translationY(targetY)
            .setDuration(3000)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                scheduleDespawn()  // Schedule despawn
            }
            .start()
    }

    fun spawn(startX: Float, startY: Float, targetY: Float) {
        // Spawn the sun at the top of the screen
        imageView.x = startX
        imageView.y = startY

        // Animate the fall of the sun
        imageView.animate()
            .translationY(targetY)
            .setDuration(3000)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                scheduleDespawn()
            }
            .start()
    }

    // Only for singleplayer
    private fun collect() {
        // When collected, animate a fade out for the sun View
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                parent.removeView(imageView)
                onCollected(value, null)
            }
            .start()
    }

    // Only for multiplayer (does the same as singleplayer, but not responsible here locally)
    private fun collectFromNetwork() {
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                parent.removeView(imageView)
                onCollected(value, id)
            }
            .start()
    }


    private fun scheduleDespawn() {
        val delay: Long = 5000

        imageView.postDelayed({
            // Check if Sun still exists (not collected yet)
            if (imageView.parent != null) {
                // Animate its despawn
                imageView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        parent.removeView(imageView)
                    }
                    .start()
            }
        }, delay)
    }
}
