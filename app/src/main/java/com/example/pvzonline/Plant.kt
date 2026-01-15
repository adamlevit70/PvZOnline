package com.example.pvzonline

import android.opengl.Visibility
import android.widget.ImageView

class Plant(
    plantImage: ImageView,
    val dmg: Int,
    cooldown: Float,
    hp: Int
) : LivingEntity(plantImage, cooldown, hp) {

    override fun dead() {
        println("PLANT DEAD")
        this.image.visibility = ImageView.GONE
    }
}
