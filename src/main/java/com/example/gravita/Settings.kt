package com.example.gravita


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import com.example.gravita.R

@Suppress("DEPRECATION")
class Settings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var highestScoreTextView: TextView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // Initialize MediaPlayer with the gamesong.mp3
        mediaPlayer = MediaPlayer.create(this, R.raw.gamesong)
        mediaPlayer.isLooping = true // Loop the music
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Retrieve and display the highest score
        highestScoreTextView = findViewById<TextView>(R.id.highestScoreTextView)
        displayHighestScore()

        // Initialize the spinner
        val gameSpeedSpinner = findViewById<Spinner>(R.id.gameSpeedSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.game_level_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gameSpeedSpinner.adapter = adapter
        }

        // Set listener for spinner item selection
        gameSpeedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Save the selected game speed
                val selectedSpeed = gameSpeedSpinner.selectedItem.toString()
                saveGameSpeed(selectedSpeed)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Button to save changes
        val saveChangesButton = findViewById<Button>(R.id.saveChangesButton)
        saveChangesButton.setOnClickListener {
            // Show a toast message indicating that changes are saved
            Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
            // Direct to the LaunchPage1 activity
            startActivity(Intent(this, LaunchPage1::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        // Back button
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // Navigate back to the previous activity
            onBackPressed()
        }
    }

    private fun saveGameSpeed(speed: String) {
        // Save the selected speed in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("gameSpeed", speed)
        editor.apply()
    }

    private fun displayHighestScore() {
        // Retrieve and display the highest score
        val highestScore = sharedPreferences.getInt("highestScore", 0)
        highestScoreTextView.text = "Highest Score: $highestScore"
    }
    override fun onResume() {
        super.onResume()
        // Start playing the music when MainActivity resumes
        mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        // Pause the music when MainActivity is paused
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when MainActivity is destroyed
        mediaPlayer.release()
    }
}

