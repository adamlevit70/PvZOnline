package com.example.pvzonline

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private val rows = 5
    private val cols = 9
    private lateinit var gameBoard: GridLayout
    private lateinit var mainLayout: FrameLayout
    private val zombies = mutableListOf<ImageView>()
    private val plantMatrix = Array(rows) { arrayOfNulls<ImageView>(cols) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameBoard = findViewById(R.id.gameBoard)
        mainLayout = findViewById(R.id.mainLayout)

        createBoard()
    }

    private fun createBoard() {
        gameBoard.post {
            val boardWidth = gameBoard.width
            val boardHeight = gameBoard.height

            val tileHeight = boardHeight / rows
            val tileWidth = boardWidth / cols - 10

            // --- ADD PLANT TILES ---
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val tile = layoutInflater.inflate(
                        R.layout.tile,
                        gameBoard,
                        false
                    ) as FrameLayout

                    tile.layoutParams = GridLayout.LayoutParams().apply {
                        width = tileWidth
                        height = tileHeight
                        setMargins(2, 2, 2, 2)
                    }

                    // Move tiles to be exactly as the board
                    tile.translationX = (100f - (row * 20))

                    val plantImage = tile.findViewById<ImageView>(R.id.plantImage)
                    tile.setOnClickListener {
                        placePlant(plantImage, row, col)
                    }

                    gameBoard.addView(tile)
                }
            }

            // --- SPAWN ZOMBIE AFTER BOARD IS READY ---
            spawnZombie(row = 2, tileWidth = tileWidth, tileHeight = tileHeight)
        }
    }


    private fun placePlant(plantImage: ImageView, row: Int, col: Int) {
        plantImage.setImageResource(R.drawable.plant_peashooter)
        plantImage.visibility = ImageView.VISIBLE

        // Save the plant in the matrix
        plantMatrix[row][col] = plantImage
    }


    private fun spawnZombie(row: Int, tileWidth: Int, tileHeight: Int) {
        val zombie = ImageView(this)
        zombie.setImageResource(R.drawable.regular_zombie)
        zombie.scaleType = ImageView.ScaleType.FIT_CENTER

        // Make zombie 2x bigger than tile
        val zombieWidth = tileWidth * 2
        val zombieHeight = tileHeight * 2
        zombie.layoutParams = FrameLayout.LayoutParams(zombieWidth, zombieHeight)

        mainLayout.addView(zombie)
        zombies.add(zombie)

        zombie.post {
            val tileIndex = row * cols
            val tile = gameBoard.getChildAt(tileIndex)

            // Align zombie’s feet with bottom of tile row
            zombie.y = tile.y + tileHeight - (zombieHeight / 3)

            // Start off-screen right
            zombie.x = gameBoard.x + gameBoard.width.toFloat()

            // --- MOVE ZOMBIE MANUALLY FRAME-BY-FRAME ---
            val speed = 5f // pixels per frame
            zombie.post(object : Runnable {
                override fun run() {
                    if(zombie.x % gameBoard.width == 0f) {
                        println("A MATCH!!!!!!")
                    }

                    // Move zombie left
                    zombie.x -= speed

                    // Continue next frame
                    zombie.postDelayed(this, 16) // ~60 FPS
                }
            })
        }
    }
}