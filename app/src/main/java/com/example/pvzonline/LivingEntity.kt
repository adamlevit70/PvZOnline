package com.example.pvzonline

import android.widget.ImageView

open class LivingEntity(
    val image: ImageView,
    val cooldown: Float,
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
        println("DEAD")
    }
}
