package com.example.pvzonline

import android.graphics.Color
import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameBoard = findViewById<GridLayout>(R.id.gameBoard)
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
                    tile.translationX = (100f - (row * 20))

                    // The image that will show the sprite
                    val plantImage = tile.findViewById<ImageView>(R.id.plantImage)

                    tile.setOnClickListener {
                        placePlant(plantImage)
                    }

                    gameBoard.addView(tile)
                }
            }
        }
    }

    private fun placePlant(plantImage: ImageView) {
        plantImage.setImageResource(R.drawable.peashooter_plant) // plant sprite
        plantImage.visibility = ImageView.VISIBLE
    }
}
