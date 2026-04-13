import android.widget.FrameLayout
import android.widget.ImageView
import com.example.pvzonline.Plant
import com.example.pvzonline.Sun
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * A plant that generates sun points over time.
 */
class SunflowerPlant(
    plantImage: ImageView,
    cooldownMs: Long,
    hp: Int,
    parent: FrameLayout,
    private val onSunCollected: (Int, String) -> Unit
) : Plant(plantImage, 0, cooldownMs, hp, parent) {

    override fun start(row: Int) {
        super.start(row)
        job = scope.launch {
            delay(cooldownMs)

            while (isActive && hp > 0) {
                generateSun()
                delay(cooldownMs)
            }
        }
    }

    private fun generateSun() {
        val plantLocation = IntArray(2)
        val parentLocation = IntArray(2)
        image.getLocationOnScreen(plantLocation)

        val xInParent = (plantLocation[0] - parentLocation[0]).toFloat()
        val yInParent = (plantLocation[1] - parentLocation[1]).toFloat()

        // Setup Sun object
        val sunId = UUID.randomUUID().toString()
        val startX = xInParent + (image.width / 2f)
        val startY = yInParent
        val targetY = yInParent + (image.height * 0.6f) // Drops slightly in front of the plant

        // Trigger the sun's internal spawn logic
        // TBC....
    }
}