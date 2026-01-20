package com.example.pvzonline

import android.animation.ObjectAnimator
import android.content.pm.ActivityInfo
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
    private val plantMatrix = Array(rows) { arrayOfNulls<Plant>(cols) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
            spawnZombie(row = 0, tileWidth = tileWidth, tileHeight = tileHeight)
            spawnZombie(row = 1, tileWidth = tileWidth, tileHeight = tileHeight)
        }
    }


    private fun placePlant(plantImage: ImageView, row: Int, col: Int) {
        plantImage.setImageResource(R.drawable.plant_peashooter)
        plantImage.visibility = ImageView.VISIBLE

        val newPlant = Plant(plantImage, 2, 2f, 100)

        // Save the plant in the matrix
        plantMatrix[row][col] = newPlant
    }


    private fun spawnZombie(row: Int, tileWidth: Int, tileHeight: Int) {
        val zombieImage = ImageView(this)
        zombieImage.setImageResource(R.drawable.regular_zombie)
        zombieImage.scaleType = ImageView.ScaleType.FIT_CENTER

        // Make zombie 2x bigger than tile
        val zombieWidth = tileWidth * 2
        val zombieHeight = tileHeight * 2
        zombieImage.layoutParams = FrameLayout.LayoutParams(zombieWidth, zombieHeight)

        mainLayout.addView(zombieImage)

        val zombie : Zombie = Zombie(zombieImage, 3f, 1f, 100)

        zombieImage.post {
            val tileIndex = (row+1) * cols
            val tile = gameBoardGrid.getChildAt(tileIndex)

            // Align zombie’s feet with bottom of tile row
            zombieImage.y = tile.y + tileHeight - (zombieHeight / 3)

            // Start off-screen right
            zombieImage.x = gameBoardGrid.x + gameBoardGrid.width.toFloat()

            var lastTileCol = -1

            var speed = zombie.speed
            var isAttacking = false

            zombieImage.post(object : Runnable {
                override fun run() {
                    if(!isAttacking) {
                        // Move zombie
                        zombieImage.x -= speed
                    }

                    val attackX = zombieImage.x + zombieWidth * 0.25f
                    val gridX = attackX - gameBoardGrid.x

                    if (gridX in 0f..gameBoardGrid.width.toFloat()) {
                        val col = (gridX / tileWidth).toInt()

                        if (col in 0 until cols) {
                            val plant = plantMatrix[row][col]

                            if (plant != null) {
                                speed = 0f

                                if (!isAttacking) {
                                    isAttacking = true

                                    zombieImage.postDelayed({
                                        plant.takeDmg(50)

                                        if (plant.hp <= 0) {
                                            plantMatrix[row][col] = null
                                        }
                                        isAttacking = false
                                        speed = zombie.speed
                                    }, 500)
                                }
                            }
                        }
                    }

                    zombieImage.postDelayed(this, 16)
                }
            })


            /*
            We can do this instead of the nested post:
            zombieImage.post { // only once, for layout positioning
    // set zombieImage.x/y
    startZombieLoop()
}

private fun startZombieLoop() {
    val runnable = object : Runnable {
        override fun run() {
            // move zombie, check collisions
            zombieImage.postDelayed(this, 16)
        }
    }
    zombieImage.post(runnable)
}
             */
        }
    }
}