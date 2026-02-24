import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.pvzonline.R

class Sun(
    private val parent: FrameLayout,
    private val onCollected: (amount: Int) -> Unit
) {

    val imageView = ImageView(parent.context)
    private val value = 25

    init {
        imageView.setImageResource(R.drawable.sun)

        val size = 150
        val params = FrameLayout.LayoutParams(size, size)
        imageView.layoutParams = params

        parent.addView(imageView)

        // Setup listener
        imageView.setOnClickListener {
            collect()
        }
    }

    fun spawn(startX: Float, targetY: Float) {
        // Spawn the sun at the top of the screen
        imageView.x = startX
        imageView.y = 0f

        // Animate the fall of the sun
        imageView.animate()
            .translationY(targetY)
            .setDuration(3000)
            .setInterpolator(LinearInterpolator())
            .start()
    }

    private fun collect() {
        // When collected, animate a fade out for the sun View
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                parent.removeView(imageView)
                onCollected(value)
            }
            .start()
    }
}