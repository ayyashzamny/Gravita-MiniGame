package com.example.gravita

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gravita.R

class LaunchPage1 : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_page1)

        // Initialize MediaPlayer with the gamesong.mp3
        mediaPlayer = MediaPlayer.create(this, R.raw.gamesong)
        mediaPlayer.isLooping = true // Loop the music

        val newGameButton = findViewById<Button>(R.id.newGameButton)
        val exitButton = findViewById<Button>(R.id.exitButton)
        val settingButton = findViewById<Button>(R.id.settingsButton)



        newGameButton.setOnClickListener {
            // Start a new game
            startActivity(Intent(this, Game::class.java))
        }

        settingButton.setOnClickListener {
            // Open settings activity
            startActivity(Intent(this, Settings::class.java))
        }

        exitButton.setOnClickListener {
            // Exit the app
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        // Start playing the music when LaunchPage1 resumes
        mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        // Pause the music when LaunchPage1 is paused
        mediaPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when LaunchPage1 is destroyed
        mediaPlayer.release()
    }
}
