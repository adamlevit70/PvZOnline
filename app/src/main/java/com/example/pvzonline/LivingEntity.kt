package com.example.pvzonline

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
            dead()
        }
    }

    open fun dead() {
        image.visibility = ImageView.GONE
    }

    open fun isDead() : Boolean {
        return hp <= 0
    }
}
