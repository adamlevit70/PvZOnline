package com.example.pvzonline

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameActivity : AppCompatActivity() {

    private val rows = 5
    private val cols = 9
    private lateinit var gameBoard: GridLayout
    private lateinit var zombie: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameBoard = findViewById<GridLayout>(R.id.gameBoard)
        zombie = findViewById<ImageView>(R.id.zombie)
        createBoard()
    }

    private fun createBoard() {
        gameBoard.post {

            val boardWidth = gameBoard.width
            val boardHeight = gameBoard.height

            val tileHeight = boardHeight / rows
            val tileWidth = boardWidth / cols - 10

            for (row in 0 until rows) {
                for (col in 0 until cols) {

                    // Inflate tile layout (with ImageView inside)
                    val tile = layoutInflater.inflate(
                        R.layout.tile,
                        gameBoard,
                        false
                    ) as FrameLayout

                    // Your original width/height + margins
                    tile.layoutParams = GridLayout.LayoutParams().apply {
                        width = tileWidth
                        height = tileHeight
                        setMargins(2, 2, 2, 2)
                    }

                    // Move tiles to be exactly as the board
                    tile.translationX = (100f - (row * 20) + row)

                    // The image that will show the sprite
                    val plantImage = tile.findViewById<ImageView>(R.id.plantImage)

                    tile.setOnClickListener {
                        placePlant(plantImage)
                    }

                    gameBoard.addView(tile)
                }
            }
        }

        startGameLoop()
    }

    private fun placePlant(plantImage: ImageView) {
        plantImage.setImageResource(R.drawable.plant_peashooter) // plant sprite
        plantImage.visibility = ImageView.VISIBLE
    }

    private fun startGameLoop() {
        // Move the ImageView horizontally
        val animator = ObjectAnimator.ofFloat(zombie, "translationX", 0f, -500f) // X-axis movement

        animator.duration = 5000 // Duration in milliseconds
        animator.interpolator = LinearInterpolator() // Move in constant speed (not smooth)

        //animator.interpolator = TimeInterpolator { input ->
            //(input * 50).toInt() / 50f   // 50 steps
        //}

        // Start the animation
        animator.start()
    }
}
