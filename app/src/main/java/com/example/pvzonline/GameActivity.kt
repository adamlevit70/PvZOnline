package com.example.pvzonline

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private val rows = 5
    private val cols = 9
    private lateinit var gameBoardGrid: GridLayout
    private lateinit var mainLayout: FrameLayout
    private val zombies = mutableListOf<ImageView>()
    private val plantMatrix = Array(rows) { arrayOfNulls<ImageView>(cols) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameBoardGrid = findViewById(R.id.gameBoardGrid)
        mainLayout = findViewById(R.id.mainLayout)

        createBoard()
    }

    private fun createBoard() {
        gameBoardGrid.post {
            val boardWidth = gameBoardGrid.width
            val boardHeight = gameBoardGrid.height

            val tileHeight = boardHeight / rows
            val tileWidth = boardWidth / cols - 10

            // --- ADD PLANT TILES ---
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val tile = layoutInflater.inflate(
                        R.layout.tile,
                        gameBoardGrid,
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

                    gameBoardGrid.addView(tile)
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
            val tile = gameBoardGrid.getChildAt(tileIndex)

            // Align zombie’s feet with bottom of tile row
            zombie.y = tile.y + tileHeight - (zombieHeight / 3)

            // Start off-screen right
            zombie.x = gameBoardGrid.x + gameBoardGrid.width.toFloat()

            var lastTileCol = -1

            // --- MOVE ZOMBIE MANUALLY FRAME-BY-FRAME ---
            var speed = 5f // pixels per frame
            zombie.post(object : Runnable {
                override fun run() {
                    // Move zombie left
                    zombie.x -= speed

                    // Convert to grid-local X
                    val zombieGridX = zombie.x - gameBoardGrid.x

                    if (zombieGridX >= 0) {
                        val currentCol = (zombieGridX / tileWidth).toInt()
                        println("currentCol: $currentCol, row: $row, speed: $speed")
                        if (currentCol in 0 until cols && currentCol != lastTileCol) {
                            lastTileCol = currentCol
                            println("Got to a plant")
                            if (plantMatrix[row-1][currentCol] != null) {
                                speed = 0f
                            }
                        }
                    }

                    // Continue next frame
                    zombie.postDelayed(this, 16) // ~60 FPS
                }
            })
        }
    }

    private fun kobi() {
        Toast.makeText(this, "MATCH", Toast.LENGTH_SHORT).show()
    }
}