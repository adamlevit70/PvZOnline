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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class GameActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("rooms")
    private var roomCode: String = ""

    private var hostId: String? = null
    private var isHost: Boolean = false

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

        // Get the room code from the intent string extra
        roomCode = intent.getStringExtra("ROOM_CODE") ?: ""

        // Fetch room info to find out who is the host
        fetchRoomInfo(roomCode)

        gameBoardGrid = findViewById(R.id.gameBoardGrid)
        gameLayout = findViewById(R.id.gameLayout)
        sunCounterText = findViewById(R.id.sunCounterText)

        peashooterCard = findViewById(R.id.peashooterCard)
        sunflowerCard = findViewById(R.id.sunflowerCard)
        wallnutCard = findViewById(R.id.wallnutCard)
        pumpfistCard = findViewById(R.id.pumpfistCard)

        // Setup price text of plants
        findViewById<TextView>(R.id.peashooterPriceText)?.text = getCost(PlantType.PEASHOOTER).toString()
        findViewById<TextView>(R.id.sunflowerPriceText)?.text = getCost(PlantType.SUNFLOWER).toString()
        findViewById<TextView>(R.id.wallnutPriceText)?.text = getCost(PlantType.WALLNUT).toString()
        findViewById<TextView>(R.id.pumpfistPriceText)?.text = getCost(PlantType.PUMPFIST).toString()

        // Setup game
        setupPlantPicker()
        updateSunUI()
        createBoard()

        // Start listeners for the other player's actions
        if (roomCode.isNotEmpty()) {
            listenToEvents(roomCode)
        }
        else {
            Toast.makeText(this, "Cannot find room code", Toast.LENGTH_LONG).show()
            finish()
        }
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
        }
        else {
            changePlantCardAlpha(peashooterCard, false)
            changePlantCardAlpha(sunflowerCard, false)
            changePlantCardAlpha(wallnutCard, false)
            changePlantCardAlpha(pumpfistCard, false)
            
            selectedPlantType = type
            changePlantCardAlpha(card, true)
        }
    }

    private fun changePlantCardAlpha(card: ImageView, selected: Boolean) {
        if(selected) {
            card.alpha = 0.6f
        }
        else {
            card.alpha = 1f
        }
    }

    private fun updateSunUI() {
        sunCounterText.text = sunPoints.toString()
    }

    private fun createBoard() {
        gameBoardGrid.post {
            val boardWidth = gameBoardGrid.width
            val boardHeight = gameBoardGrid.height

            tileHeight = boardHeight / rows
            tileWidth = boardWidth / cols - 10

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

                    tile.translationX = (100f - (row * 20))

                    val plantImage = tile.findViewById<ImageView>(R.id.plantImage)
                    plantImage.elevation = row.toFloat()

                    tile.setOnClickListener {
                        onTileClicked(row, col, plantImage)
                    }

                    gameBoardGrid.addView(tile)
                }
            }

            // Start game loop with sun and zombie spawn only if host (the authority)
            if(isHost) {
                startZombieSpawnGeneration()
                startSunSpawnGeneration()
            }
        }
    }

    // Keep spawning zombies as long as the game is running (enters here only if host)
    private fun startZombieSpawnGeneration() {
        lifecycleScope.launch {
            while(!gameEnded) {
                val spawnDelay = (4000..5000).random()
                val spawnRow = (0..4).random()
                delay(spawnDelay.toLong())

                // Picks randomly IDs and zombie types
                val zombieId: String = UUID.randomUUID().toString()
                val zombieTypeName: String = ZombieType.entries.random().toString()

                sendSpawnZombieEvent(zombieId, zombieTypeName, spawnRow)
            }
        }
    }

    private fun startSunSpawnGeneration() {
        lifecycleScope.launch {
            delay((3000..6000).random().toLong())
            while (!gameEnded) {
                spawnSun()
                delay((3000..6000).random().toLong())
            }
        }
    }

    private fun spawnSun() {
        val sun = Sun(gameLayout, ::addSunPoints)
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

    // When clicking on one tile in the grid, place a plant if possible
    private fun onTileClicked(row: Int, col: Int, plantImage: ImageView) {
        // Checks if the tile is empty AND we selected a plant in the picker
        if (plantMatrix[row][col] == null && selectedPlantType != null) {

            // Check if we have enough sun points to afford the plant
            val cost = getCost(selectedPlantType!!)
            if (sunPoints >= cost) {
                if (roomCode.isNotEmpty()) {
                    sendRequestPlacePlantEvent(row, col, selectedPlantType!!.name)
                }
            }

            // Clean the picker select after click
            selectedPlantType = null
            changePlantCardAlpha(peashooterCard, false)
            changePlantCardAlpha(sunflowerCard, false)
            changePlantCardAlpha(wallnutCard, false)
            changePlantCardAlpha(pumpfistCard, false)
        }
    }


    private fun createPlantByType(plantImage: ImageView, plantTypeName: String) : Plant? {
        val type = PlantType.valueOf(plantTypeName)
        val newPlant = when(type) {
            PlantType.PEASHOOTER -> {
                plantImage.setImageResource(R.drawable.plant_peashooter)
                ShooterPlant(plantImage, 20, 2000, 100, gameLayout, ::getClosestZombieInFront, 0f)
            }
            PlantType.SUNFLOWER -> {
                plantImage.setImageResource(R.drawable.plant_sunflower)
                SunflowerPlant(plantImage, 5000, 100, gameLayout, ::addSunPoints)
            }
            PlantType.WALLNUT -> {
                plantImage.setImageResource(R.drawable.plant_wallnut)
                Plant(plantImage, 0, 1000, 4000, gameLayout)
            }
            PlantType.PUMPFIST -> {
                plantImage.setImageResource(R.drawable.plant_pumpfist)
                MeleePlant(plantImage, 100, 750, 100, gameLayout, ::getClosestZombieInFront, 1f)
            }
        }
        return newPlant
    }

    private fun getCost(type: PlantType): Int {
        return when (type) {
            PlantType.PEASHOOTER -> 100
            PlantType.SUNFLOWER -> 50
            PlantType.WALLNUT -> 50
            PlantType.PUMPFIST -> 200
        }
    }


    private fun createZombieByType(id: String, typeName: String, zombieImage: ImageView) : Zombie {
        val type = ZombieType.valueOf(typeName)

        return when(type) {
            ZombieType.REGULAR -> {
                zombieImage.setImageResource(R.drawable.zombie)
                Zombie(
                    id,
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
                    id,
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
                    id,
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
                    id,
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
                    id,
                    zombieImage,
                    90,
                    1.5f,
                    1500,
                    800
                )
            }
        }
    }

    private fun startZombieLoop(zombie: Zombie, row: Int) {
        val zombieImage = zombie.zombieImage
        val zombieWidth = zombieImage.width
        var speed = zombie.speed
        var isAttacking = false

        val runnable = object : Runnable {
            override fun run() {
                if(zombie.isDead()) {
                    zombiesByRow[row].remove(zombie)
                    return
                }
                if (gameEnded) return
                if (!isAttacking) {
                    zombieImage.x -= speed
                }
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
                                if(!zombie.isDead()) {
                                    if(isHost) {
                                        zombie.attack(plant)

                                        if (plant.hp <= 0) {
                                            plantMatrix[row][col] = null
                                            //sendPlantRemovedEvent(...)
                                        }
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

    private fun endGame() {
        gameEnded = true
        for (row in plantMatrix) {
            for (plant in row) {
                plant?.pause()
            }
        }
    }

    private fun getClosestZombieInFront(row: Int, posX: Float): Zombie? {
        return zombiesByRow[row]
            .filter { it.zombieImage.x + it.zombieImage.width > posX }
            .minByOrNull { it.zombieImage.x }
    }


    // --- Firebase Multiplayer Integration ---

    // Listener to events
    // Each time new event appears, it will send it to handler
    private fun listenToEvents(roomCode: String) {
        roomsRef.document(roomCode)
            .collection("events")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    for (change in snapshots.documentChanges) {
                        // Listens to new events only
                        if (change.type == DocumentChange.Type.ADDED) {
                            val event = change.document
                            handleEvent(event)
                        }
                    }
                }
            }
    }


    // Handle new events
    private fun handleEvent(event: DocumentSnapshot) {
        val type = event.getString("type")

        if (type == "REQUEST_PLACE_PLANT") {
            // HOST AUTHORITY (the host approves the placement)
            if (isHost) {
                // Event must have a sender
                if (event.getString("senderId") == null) {
                    return
                }

                val row = event.getLong("row")!!.toInt()
                val col = event.getLong("col")!!.toInt()
                val plantTypeName = event.getString("plantType")!!

                if (plantMatrix[row][col] == null) {
                    sendApprovedPlantEvent(row, col, plantTypeName)
                }
            }
        }

        if (type == "PLACE_PLANT") {
            val row = event.getLong("row")!!.toInt()
            val col = event.getLong("col")!!.toInt()
            val plantTypeName = event.getString("plantType")!!

            placePlantFromNetwork(row, col, plantTypeName)
        }

        if(type == "SPAWN_ZOMBIE") {
            val row = event.getLong("row")!!.toInt()
            val zombieId = event.getString("zombieId")!!
            val zombieTypeName = event.getString("zombieType")!!

            spawnZombieFromNetwork(zombieId, zombieTypeName, row)
        }
    }


    private fun placePlantFromNetwork(row: Int, col: Int, plantTypeName: String) {
        if (plantMatrix[row][col] != null) return

        val tileIndex = row * cols + col
        val tile = gameBoardGrid.getChildAt(tileIndex) as FrameLayout
        val plantImage = tile.findViewById<ImageView>(R.id.plantImage)

        val newPlant = createPlantByType(plantImage, plantTypeName)
        if (newPlant != null) {

            // TEMP: Both players lose suns
            //val cost = getCost(selectedPlantType!!)
            //addSunPoints(-cost)

            plantMatrix[row][col] = newPlant
            plantImage.visibility = ImageView.VISIBLE
            newPlant.start(row)
        }
    }

    private fun spawnZombieFromNetwork(zombieId: String, zombieTypeName: String, row: Int) {
        val zombieImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER

            // TODO: change zombie's scale if the zombie is indeed big
            layoutParams = FrameLayout.LayoutParams(tileWidth * 2, tileHeight * 2)

            elevation = row.toFloat()
        }

        // Avoids duplicate by checking if a zombie with the same ID already exists in the row
        if (zombiesByRow[row].any { it.id == zombieId }) return

        val zombie = createZombieByType(zombieId, zombieTypeName, zombieImage)
        zombiesByRow[row].add(zombie)
        gameLayout.addView(zombieImage)

        zombieImage.post {
            val tileIndex = row * cols
            val tile = gameBoardGrid.getChildAt(tileIndex)
            zombieImage.y = tile.y + tileHeight + (zombieImage.height / 5)
            zombieImage.x = gameBoardGrid.x + gameBoardGrid.width.toFloat()
            startZombieLoop(zombie, row)
        }
    }


    // -- Event senders --

    private fun sendRequestPlacePlantEvent(row: Int, col: Int, plantTypeName: String) {
        val event = hashMapOf(
            "type" to "REQUEST_PLACE_PLANT",
            "row" to row,
            "col" to col,
            "plantType" to plantTypeName,
            "senderId" to FirebaseAuth.getInstance().currentUser!!.uid,
            "timestamp" to FieldValue.serverTimestamp()
        )

        addEvent(event)
    }

    private fun sendApprovedPlantEvent(row: Int, col: Int, plantTypeName: String) {
        val event = hashMapOf(
            "type" to "PLACE_PLANT",
            "row" to row,
            "col" to col,
            "plantType" to plantTypeName,
            "timestamp" to FieldValue.serverTimestamp()
        )

        addEvent(event)
    }

    private fun sendSpawnZombieEvent(zombieId: String, zombieTypeName: String, row: Int) {
        val event = hashMapOf(
            "type" to "SPAWN_ZOMBIE",
            "zombieId" to zombieId,
            "zombieType" to zombieTypeName,
            "row" to row,
            "timestamp" to FieldValue.serverTimestamp()
        )

        addEvent(event)
    }

    fun addEvent(event: HashMap<String, Any>) {
        roomsRef.document(roomCode)
            .collection("events")
            .add(event)
    }



    // Fetches if the client is also the host at game setup
    fun fetchRoomInfo(roomCode: String) {
        roomsRef.document(roomCode)
            .get()
            .addOnSuccessListener { document ->
                hostId = document.getString("hostId")

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                isHost = (currentUserId == hostId)
            }
    }
}
