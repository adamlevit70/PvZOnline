package com.example.pvzonline

import android.widget.ImageView

class Zombie(
    val id: String,
    val zombieImage: ImageView,
    dmg: Int,
    val speed: Float,
    cooldownMs: Long,
    hp: Int,
    val sendEventOnDamaged: ((zombieId: String, newHp: Int) -> Unit)? = null
) : LivingEntity(zombieImage, dmg, cooldownMs, hp) {

    fun attack(plant: Plant) {
        plant.takeDmg(dmg)
    }

    override fun takeDmg(dmg: Int) {
        if (dmg <= 0) return

        // If the function is available, send the event to the Firebase
        if(sendEventOnDamaged != null) {
            val newHp = hp - dmg
            sendEventOnDamaged.invoke(id, newHp)
        }
        // If not, change only locally (solo mode)
        else {
            super.takeDmg(dmg)
        }
    }
}
