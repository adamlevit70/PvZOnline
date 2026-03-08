import android.widget.FrameLayout
import android.widget.ImageView
import com.example.pvzonline.PeaBullet
import com.example.pvzonline.Plant
import com.example.pvzonline.Zombie
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A plant that shoots bullets at zombies in its row.
 */
class ShooterPlant(
    plantImage: ImageView,
    dmg: Int,
    cooldownMs: Long,
    hp: Int,
    parent: FrameLayout,
    private val findZombie: (row: Int, plantX: Float) -> Zombie?,
    private val attackRadius: Float = 0f // 0 means infinite range in the row
) : Plant(plantImage, dmg, cooldownMs, hp, parent) {

    override fun start(row: Int) {
        super.start(row)
        job = scope.launch {
            while (isActive && hp > 0) {
                performAttack()
                delay(cooldownMs)
            }
        }
    }

    private fun performAttack() {
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)
        image.getLocationOnScreen(plantLocation)
        parent.getLocationOnScreen(parentLocation)

        val xInParent = (plantLocation[0] - parentLocation[0]).toFloat()
        val yInParent = (plantLocation[1] - parentLocation[1]).toFloat()
        val firingPointX = xInParent + image.width

        val zombie = findZombie(plantRow, firingPointX)
        if (zombie != null) {
            val zombieX = zombie.zombieImage.x
            val distance = zombieX - firingPointX

            // If attackRadius is 0, it means infinite range
            if (attackRadius <= 0f || distance <= attackRadius) {
                shoot(xInParent, yInParent)
            }
        }
    }

    private fun shoot(xInParent: Float, yInParent: Float) {
        val bulletX = xInParent + image.width / 2f
        val bulletY = yInParent + image.height / 8f

        PeaBullet(parent, plantRow, bulletX, bulletY, 15f, dmg, findZombie)
    }
}

