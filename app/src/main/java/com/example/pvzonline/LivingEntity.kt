package com.example.pvzonline

import android.widget.FrameLayout
import android.widget.ImageView

open class LivingEntity(
    val image: ImageView,
    val dmg: Int,
    val cooldownMs: Long,
    var hp: Int
)
{
    open fun takeDmg(dmg: Int) {
        hp -= dmg
        if (hp <= 0) {
            hp = 0
            // Do not call dead() func yet (for multiplayer sync purposes)
        }
    }

    open fun dead() {
        // Delete the ImageView (destroy the visibility)
        val parent = image.parent as? FrameLayout
        parent?.removeView(image)
    }

    open fun isDead() : Boolean {
        return hp <= 0
    }
}
