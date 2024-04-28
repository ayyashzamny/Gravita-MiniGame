package com.example.gravita

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.gravita.R

class MainActivity : AppCompatActivity() {
    private lateinit var playButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        playButton = findViewById(R.id.btnStart)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // Initialize MediaPlayer with the gamesong.mp3
        mediaPlayer = MediaPlayer.create(this, R.raw.gamesong)
        mediaPlayer.isLooping = true // Loop the music

        playButton.setOnClickListener {
            // Show loading indicator
            loadingProgressBar.visibility = View.VISIBLE

            // Simulate some loading process (Replace with your actual loading logic)
            simulateLoading()
        }
    }

    // Simulate loading process
    private fun simulateLoading() {
        // You can replace this with your actual loading logic.
        // For example, making a network request, loading data from a database, etc.
        // Here, we're just delaying for demonstration purposes.
        Thread {
            try {
                Thread.sleep(3000) // Simulating a 3-second loading process
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            // Hide loading indicator after loading is complete
            runOnUiThread {
                loadingProgressBar.visibility = View.GONE
                // Start LaunchPage1 activity
                startActivity(Intent(this, LaunchPage1::class.java))
            }
        }.start()
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
