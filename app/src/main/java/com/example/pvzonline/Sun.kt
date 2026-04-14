package com.example.pvzonline

import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.UUID

class Sun(
    private val parent: FrameLayout,
    private val onCollected: (id: String) -> Unit,
    private val id: String = ""
) {

    val imageView = ImageView(parent.context)

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

    // Works for both singleplayer and multiplayer (but ID won't be relevant in singleplayer)
    private fun collect() {
        // When collected, animate a fade out for the sun View
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                parent.removeView(imageView)
                onCollected(id)
            }
            .start()
    }

    // Called from firebase and just animates (was not the one that collected the sun)
    fun collectedFromNetwork() {
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                parent.removeView(imageView)
            }
            .start()
    }

    // Despawn handler
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
