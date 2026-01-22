package com.example.pvzonline

import android.widget.ImageView

class Zombie(
    val zombieImage: ImageView,
    val speed: Float,
    cooldown: Float,
    hp: Int
) : LivingEntity(zombieImage, cooldown, hp) {

    override fun dead() {
        println("ZOMBIE DEAD")
        //this.image.visibility = ImageView.GONE
    }
}
