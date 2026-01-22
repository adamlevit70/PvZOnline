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
import java.util.Queue

class GameActivity : AppCompatActivity() {

    private val rows = 5
    private val cols = 9
    private lateinit var gameBoardGrid: GridLayout
    private lateinit var mainLayout: FrameLayout
    private val plantMatrix = Array(rows) { arrayOfNulls<Plant>(cols) }
    val zombiesByRow = Array(rows) { mutableListOf<Zombie>() }
    private var tileHeight : Int = 0
    private var tileWidth : Int = 0

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

            tileHeight = boardHeight / rows
            tileWidth = boardWidth / cols - 10

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
            spawnZombie(row = 2)
            spawnZombie(row = 0)
            spawnZombie(row = 1)
        }
    }


    private fun placePlant(plantImage: ImageView, row: Int, col: Int) {
        plantImage.setImageResource(R.drawable.plant_peashooter)
        plantImage.visibility = ImageView.VISIBLE

        val newPlant = Plant(plantImage, 2, 2f, 100)

        // Save the plant in the matrix
        plantMatrix[row][col] = newPlant
    }


    private fun spawnZombie(row: Int) {
        val zombieImage = ImageView(this).apply {
            setImageResource(R.drawable.regular_zombie)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                tileWidth * 2,
                tileHeight * 2
            )
        }

        mainLayout.addView(zombieImage)

        val zombie = Zombie(zombieImage, 3f, 1f, 100)
        zombiesByRow[row].add(zombie)

        // Wait for layout only to position the zombie
        zombieImage.post {
            val tileIndex = (row + 1) * cols
            val tile = gameBoardGrid.getChildAt(tileIndex)

            // Align zombie to row
            zombieImage.y = tile.y + tileHeight - (zombieImage.height / 3)
            zombieImage.x = gameBoardGrid.x + gameBoardGrid.width.toFloat()

            // Start movement + attack loop
            startZombieLoop(zombie, row)
        }
    }

    private fun startZombieLoop(
        zombie: Zombie,
        row: Int,
    ) {
        val zombieImage = zombie.zombieImage
        val zombieWidth = zombieImage.width

        var speed = zombie.speed
        var isAttacking = false

        val runnable = object : Runnable {
            override fun run() {
                if (!isAttacking) {
                    zombieImage.x -= speed
                }

                val attackX = zombieImage.x + zombieWidth * 0.25f
                val gridX = attackX - gameBoardGrid.x

                if (gridX in 0f..gameBoardGrid.width.toFloat()) {
                    val col = (gridX / tileWidth).toInt()

                    if (col in 0 until cols) {
                        val plant = plantMatrix[row][col]

                        if (plant != null && !isAttacking) {
                            isAttacking = true
                            speed = 0f

                            zombieImage.postDelayed({
                                plant.takeDmg(50)

                                if (plant.hp <= 0) {
                                    plantMatrix[row][col] = null
                                }

                                speed = zombie.speed
                                isAttacking = false
                            }, 500)
                        }
                    }
                }

                zombieImage.postDelayed(this, 16)
            }
        }

        zombieImage.post(runnable)
    }

    private fun getClosestZombieInFront(
        row: Int,
        plantX: Float
    ): Zombie? {
        return zombiesByRow[row]
            .filter { it.zombieImage.x > plantX }
            .minByOrNull { it.zombieImage.x }
    }
}