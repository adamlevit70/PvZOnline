package com.example.pvzonline

import android.widget.ImageView

class Zombie(
    val zombieImage: ImageView,
    dmg: Int,
    val speed: Float,
    cooldownMs: Long,
    hp: Int
) : LivingEntity(zombieImage, dmg, cooldownMs, hp) {

    fun attack(plant: Plant) {
        plant.takeDmg(dmg)
    }
}
