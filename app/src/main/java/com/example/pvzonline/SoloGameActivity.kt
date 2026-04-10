package com.example.pvzonline

import MeleePlant
import ShooterPlant
import SunflowerPlant
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoloGameActivity : AppCompatActivity() {

    enum class PlantType {
        PEASHOOTER, SUNFLOWER, WALLNUT, PUMPFIST
    }
    enum class ZombieType {
        REGULAR, FOOTBALL, JACKSON, YETI, GARGANTUAR
    }

    private lateinit var peashooterCard: ImageView

    private lateinit var sunflowerCard: ImageView
    private lateinit var wallnutCard: ImageView

    private lateinit var pumpfistCard: ImageView

    private var selectedPlantType: PlantType? = null

    private val rows = 5
    private val cols = 9
    private lateinit var gameBoardGrid: GridLayout
    private lateinit var gameLayout: FrameLayout
    private val plantMatrix = Array(rows) { arrayOfNulls<Plant>(cols) }
    val zombiesByRow = Array(rows) { mutableListOf<Zombie>() }
    private var sunPoints = 50
    private lateinit var sunCounterText: TextView
    private var tileHeight : Int = 0
    private var tileWidth : Int = 0
    private var gameEnded : Boolean = false

    /*
        *
        IMPORTANT: When switching to online, REMOVE ALL THE RANDOM
        *
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_game)

        gameBoardGrid = findViewById(R.id.gameBoardGrid)
        gameLayout = findViewById(R.id.gameLayout)
        sunCounterText = findViewById(R.id.sunCounterText)

        peashooterCard = findViewById(R.id.peashooterCard)
        sunflowerCard = findViewById(R.id.sunflowerCard)
        wallnutCard = findViewById(R.id.wallnutCard)
        pumpfistCard = findViewById(R.id.pumpfistCard)

        // Setup price text of plants
        findViewById<TextView>(R.id.peashooterPriceText).text = getCost(PlantType.PEASHOOTER).toString()
        findViewById<TextView>(R.id.sunflowerPriceText).text = getCost(PlantType.SUNFLOWER).toString()
        findViewById<TextView>(R.id.wallnutPriceText).text = getCost(PlantType.WALLNUT).toString()
        findViewById<TextView>(R.id.pumpfistPriceText).text = getCost(PlantType.PUMPFIST).toString()


        setupPlantPicker()

        updateSunUI()

        createBoard()
    }

    private fun setupPlantPicker() {
        peashooterCard.setOnClickListener {
            selectPlant(PlantType.PEASHOOTER, peashooterCard)
        }
        sunflowerCard.setOnClickListener {
            selectPlant(PlantType.SUNFLOWER, sunflowerCard)
        }
        wallnutCard.setOnClickListener {
            selectPlant(PlantType.WALLNUT, wallnutCard)
        }
        pumpfistCard.setOnClickListener {
            selectPlant(PlantType.PUMPFIST, pumpfistCard)
        }
    }

    private fun selectPlant(type: PlantType, card: ImageView) {
        if (selectedPlantType == type) {
            selectedPlantType = null
            changePlantCardAlpha(card, false)
        } else {
            // Deselect previous
            changePlantCardAlpha(peashooterCard, false)
            changePlantCardAlpha(sunflowerCard, false)
            changePlantCardAlpha(wallnutCard, false)
            changePlantCardAlpha(pumpfistCard, false)
            
            selectedPlantType = type
            changePlantCardAlpha(card, true)
        }
    }

    private fun changePlantCardAlpha(card: ImageView, selected: Boolean) {
        // Visually show selected
        if(selected) {
            card.alpha = 0.6f
        }
        else {
            card.alpha = 1f
        }
    }

    // Every time the sun value is updated, we will call this function
    private fun updateSunUI() {
        // Change UI text to the updated sun value
        sunCounterText.text = sunPoints.toString()
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
                    plantImage.elevation = row.toFloat()

                    tile.setOnClickListener {
                        placePlant(plantImage, row, col)
                    }

                    gameBoardGrid.addView(tile)
                }
            }

            // --- START GAME LOOP AFTER BOARD IS READY ---
            startZombieSpawnGeneration()
            startSunSpawnGeneration()
        }
    }

    private fun startZombieSpawnGeneration() {
        // As long as the game runs, keep spawning zombies
        lifecycleScope.launch {
            while(!gameEnded) {
                val spawnDelay = (4000..5000).random()
                val spawnRow = (0..4).random()
                delay(spawnDelay.toLong())
                spawnZombie(spawnRow)
            }
        }
    }

    private fun startSunSpawnGeneration() {
        // As long as the game runs, keep spawning suns
        lifecycleScope.launch {
            delay((3000..6000).random().toLong())

            while (!gameEnded) {
                spawnSun()
                delay((3000..6000).random().toLong())
            }
        }
    }

    private fun spawnSun() {
        // Create Sun class which will add to the total sun points when obtained
        val sun = Sun(gameLayout, ::addSunPoints)

        // Spawn sun at random X pos and set its target when falls
        gameLayout.post {
            val randomX = (0..(gameLayout.width - 150)).random().toFloat()
            val targetY = gameBoardGrid.y + gameBoardGrid.height - 200f

            sun.topSpawn(randomX, targetY)
        }
    }

    private fun addSunPoints(amount: Int) {
        sunPoints += amount
        updateSunUI()
    }

    private fun placePlant(plantImage: ImageView, row: Int, col: Int) {
        // Place plant on tile only if available
        if(plantMatrix[row][col] == null && selectedPlantType != null) {
            val newPlant = createPlantByType(plantImage)

            // Trying to place the plant on the tile, if could create
            if(newPlant != null) {
                // Save the plant in the matrix
                plantMatrix[row][col] = newPlant
                // Show the plant on tile
                plantImage.visibility = ImageView.VISIBLE

                newPlant.start(row)
            }
        }
    }

    private fun createPlantByType(plantImage: ImageView) : Plant? {
        val type = selectedPlantType ?: return null
        val cost = getCost(type)

        if(sunPoints < cost)  {
            return null
        }
        addSunPoints(-cost)

        val newPlant = when(type) {
            PlantType.PEASHOOTER -> {
                plantImage.setImageResource(R.drawable.plant_peashooter)
                ShooterPlant(
                    plantImage,
                    20,
                    2000,
                    100,
                    gameLayout,
                    ::getClosestZombieInFront,
                    0f
                )
            }
            PlantType.SUNFLOWER -> {
                plantImage.setImageResource(R.drawable.plant_sunflower)
                SunflowerPlant(
                    plantImage,
                    5000,
                    100,
                    gameLayout,
                    ::addSunPoints
                )
            }
            PlantType.WALLNUT -> {
                plantImage.setImageResource(R.drawable.plant_wallnut)
                Plant(
                    plantImage,
                    0,
                    1000,
                    4000,
                    gameLayout
                )
            }
            PlantType.PUMPFIST -> {
                plantImage.setImageResource(R.drawable.plant_pumpfist)
                MeleePlant(
                    plantImage,
                    100,
                    750,
                    100,
                    gameLayout,
                    ::getClosestZombieInFront,
                    1f
                )
            }
        }

        changePlantCardAlpha(peashooterCard, false)
        changePlantCardAlpha(sunflowerCard, false)
        changePlantCardAlpha(wallnutCard, false)
        changePlantCardAlpha(pumpfistCard, false)
        selectedPlantType = null
        
        return newPlant
    }

    fun getCost(type: PlantType): Int {
        return when (type) {
            PlantType.PEASHOOTER -> 100
            PlantType.SUNFLOWER -> 50
            PlantType.WALLNUT -> 50
            PlantType.PUMPFIST -> 200
        }
    }


    private fun spawnZombie(row: Int) {
        val zombieImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER

            // TODO: change zombie's scale if the zombie is indeed big
            layoutParams = FrameLayout.LayoutParams(
                tileWidth * 2,
                tileHeight * 2
            )

            elevation = row.toFloat()  // lower the y pos, higher the layer order
        }


        val zombie = createZombieByType(zombieImage)
        zombiesByRow[row].add(zombie)

        gameLayout.addView(zombieImage)

        // Wait for layout only to position the zombie
        zombieImage.post {
            val tileIndex = row * cols
            val tile = gameBoardGrid.getChildAt(tileIndex)

            // Align zombie to row
            zombieImage.y = tile.y + tileHeight + (zombieImage.height / 5)
            zombieImage.x = gameBoardGrid.x + gameBoardGrid.width.toFloat()

            // Start movement + attack loop
            startZombieLoop(zombie, row)
        }
    }

    // Sets the image of the zombie and creates an object according to the chosen zombie
    private fun createZombieByType(zombieImage: ImageView) : Zombie {
        // FOR NOW, RANDOMLY SELECT THE ZOMBIE
        val type = ZombieType.entries.random()

        val newZombie = when(type) {
            ZombieType.REGULAR -> {
                zombieImage.setImageResource(R.drawable.zombie)
                Zombie(
                    zombieImage,
                    50,
                    2f,
                    1000,
                    100
                )
            }
            ZombieType.FOOTBALL -> {
                zombieImage.setImageResource(R.drawable.zombie_football)
                Zombie(
                    zombieImage,
                    60,
                    3f,
                    1000,
                    400
                )
            }
            ZombieType.JACKSON -> {
                zombieImage.setImageResource(R.drawable.zombie_jackson)
                Zombie(
                    zombieImage,
                    50,
                    4f,
                    800,
                    300
                )
            }
            ZombieType.YETI -> {
                zombieImage.setImageResource(R.drawable.zombie_yeti)
                Zombie(
                    zombieImage,
                    80,
                    2f,
                    1200,
                    500
                )
            }
            ZombieType.GARGANTUAR -> {
                zombieImage.setImageResource(R.drawable.zombie_gargantuar)
                Zombie(
                    zombieImage,
                    90,
                    1.5f,
                    1500,
                    800
                )
            }
        }

        return newZombie
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
                if(zombie.isDead()) {
                    // After the zombie died, stop the loop and remove it from the list
                    zombiesByRow[row].remove(zombie)
                    return
                }

                if (gameEnded) return

                // Zombie moves as long as no plant in front of it
                if (!isAttacking) {
                    zombieImage.x -= speed
                }

                // Ends game when one zombie reaches to finish line (after the last plant)
                if((zombieImage.x) < gameBoardGrid.x - 10) {
                    endGame()
                    return
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
                                // Attack plant if didn't die during the delay
                                if(!zombie.isDead()) {
                                    zombie.attack(plant);

                                    // When plant died, remove from the array
                                    if (plant.hp <= 0) {
                                        plantMatrix[row][col] = null
                                    }

                                    speed = zombie.speed
                                    isAttacking = false
                                }
                            }, zombie.cooldownMs)
                        }
                    }
                }

                zombieImage.postDelayed(this, 16)
            }
        }

        zombieImage.post(runnable)
    }


    // Handles the DEAD screen at the end of the game
    private fun endGame() {
        gameEnded = true

        // Pauses every plant so it will stop attacking zombies
        for (row in plantMatrix) {
            for (plant in row) {
                plant?.pause()
            }
        }
    }


    private fun getClosestZombieInFront(
        row: Int,
        posX: Float
    ): Zombie? {
        return zombiesByRow[row]
            .filter { it.zombieImage.x + it.zombieImage.width > posX }
            .minByOrNull { it.zombieImage.x }
    }
}
