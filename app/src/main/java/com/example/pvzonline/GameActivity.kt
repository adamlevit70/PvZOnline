package com.example.pvzonline

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
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
        gameBoard.post { // wait until layout is measured
            val boardWidth = gameBoard.width
            val boardHeight = gameBoard.height

            val tileHeight = boardHeight / rows
            val tileWidth = boardWidth / cols - 10

            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val tile = View(this).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = tileWidth
                            height = tileHeight
                            translationX = (90f - (row * 18))
                            setMargins(2, 2, 2, 2) // optional spacing between tiles
                        }

                        setBackgroundColor(Color.parseColor("#88AAAAAA")) // placeholder color
                        setOnClickListener {
                            setBackgroundColor(Color.parseColor("#33FF33")) // temporary plant
                        }
                    }
                    gameBoard.addView(tile)
                }
            }
        }
    }

}
