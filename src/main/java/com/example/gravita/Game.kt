package com.example.gravita

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import java.util.*
import android.media.MediaPlayer
import android.widget.Toast
import com.example.gravita.R

class Game : AppCompatActivity() {

    private lateinit var newtonImg: ImageView
    private lateinit var apple: ImageView
    private lateinit var coconut: ImageView

    private var initialX = 0f
    private val random = Random()
    private var score = 0 // Variable to keep track of the score
    private var gameSpeedMultiplier = 1.0 // Default game speed multiplier

    private var gamePaused = false
    private lateinit var dropRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())

    // Declare a MediaPlayer variable
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize MediaPlayer with the gamesong.mp3
        mediaPlayer = MediaPlayer.create(this, R.raw.gamesong)
        mediaPlayer.isLooping = true // Loop the music
        // Initialize views and variables

        mediaPlayer.start()

        newtonImg = findViewById(R.id.imgNewton)
        apple = findViewById(R.id.imgApple)
        coconut = findViewById(R.id.imgCoconut)


        // Set initial visibility of ball and shot to invisible
        apple.visibility = View.INVISIBLE
        coconut.visibility = View.INVISIBLE

        // Retrieve game speed multiplier from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedGameSpeed = sharedPreferences.getString("gameSpeed", "1X")
        gameSpeedMultiplier = when (savedGameSpeed) {
            "1X" -> 1.0
            "2X" -> 2.0
            else -> 1.0 // Default to 1X if not recognized
        }

        // Check if the game was paused before
        gamePaused = sharedPreferences.getBoolean("isGamePaused", false)

        // Set touch listener to the goalie ImageView
        newtonImg.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Save the initial X position when touched
                    initialX = newtonImg.x - event.rawX
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate new X position based on touch movement
                    val newX = event.rawX + initialX
                    // Ensure the goalie stays within the bounds of the screen
                    if (newX >= 0 && newX <= (newtonImg.parent as View).width - newtonImg.width) {
                        // Update goalie position
                        newtonImg.x = newX
                    }
                    true
                }

                else -> false
            }
            // Start countdown animation and random dropping

        }


        // Set click listener for the pause button
         lateinit var pauseButton : ImageView
         var isPaused = false

        pauseButton = findViewById(R.id.pauseButton)

        pauseButton.setOnClickListener {
            if (isPaused) {
                // Resume the game
                resumeGame()
//                pauseButton.setImageResource(R.drawable.resume_icon)

            } else {
                // Pause the game
                pauseGame()
//                pauseButton.setImageResource(R.drawable.pause_icon)

            }
            isPaused = !isPaused // Toggle the pause state
        }


        // Start countdown animation and random dropping
        startCountdownAnimation()
        startRandomDropping()
    }
    override fun onPause() {
        super.onPause()
        // Pause the music when the activity is paused
        mediaPlayer.pause()

    }

    override fun onResume() {
        super.onResume()
        // Resume the music when the activity resumes
        mediaPlayer.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when the activity is destroyed
        mediaPlayer.release()
    }

    private fun pauseGame() {
        // Pause the game
        showPauseDialog()
        gamePaused = true
        // Store the game state in SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isGamePaused", true)
        editor.apply()
    }

    private fun resumeGame() {
        dismissPauseDialog()
        // Remove the game state from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("isGamePaused")
        editor.apply()
        // Resume the game
        gamePaused = false
        // Restart dropping balls and shots randomly
        startRandomDropping()
    }

    private fun startRandomDropping() {
        // Runnable to drop a ball or shot randomly
        dropRunnable = object : Runnable {
            override fun run() {
                if (!gamePaused) {
                    // Randomly decide whether to drop a ball or a shot
                    val dropBall = random.nextBoolean()
                    if (dropBall) {
                        dropAppleFromTop()
                    } else {
                        dropCoconutFromTop()
                    }

                    // Schedule the next drop after a random delay
                    val nextDelay = (random.nextInt(3000) + 1000) / gameSpeedMultiplier.toInt() // Adjust delay based on game speed multiplier
                    handler.postDelayed(this, nextDelay.toLong())
                }
            }
        }

        // Start dropping balls and shots randomly
        handler.post(dropRunnable)
    }

    private fun dropAppleFromTop() {
        // Create a new instance of the ball ImageView
        val newApple = ImageView(this)
        newApple.setImageResource(R.drawable.apple)
        newApple.layoutParams = apple.layoutParams

        // Add the ball to the layout
        (findViewById<View>(android.R.id.content) as? ViewGroup)?.addView(newApple)

        // Set random initial position of the ball along the X-axis
        val screenWidth = (findViewById<View>(android.R.id.content) as ViewGroup).width
        val randomX = random.nextInt(screenWidth - newApple.width)

        newApple.translationX = randomX.toFloat()
        newApple.translationY = -newApple.height.toFloat()

        // Animate ball to move from top to bottom
        val appleAnimator = ObjectAnimator.ofFloat(
            newApple,
            "translationY",
            0f,
            (findViewById<View>(android.R.id.content) as ViewGroup).height.toFloat()
        )
        appleAnimator.apply {
            duration = (1500 / gameSpeedMultiplier).toLong() // Adjust duration based on game speed multiplier
            interpolator = AccelerateInterpolator() // Apply acceleration to the animation
            start() // Start the animation
        }

        // Set visibility of ball to visible during animation
        appleAnimator.doOnStart {
            newApple.visibility = View.VISIBLE
        }

        // Remove the ball from the layout after animation ends
        appleAnimator.doOnEnd {
            (findViewById<View>(android.R.id.content) as? ViewGroup)?.removeView(newApple)
        }

        // Check if the goalie catches the ball
        appleAnimator.doOnEnd {
            if (isNewtonCatchApple(newApple)) {
                // Increase the score if the goalie catches the ball
                score++
                updateScore()
            }
        }
    }

    private fun dropCoconutFromTop() {
        // Create a new instance of the shot ImageView
        val newCoconut = ImageView(this)
        newCoconut.setImageResource(R.drawable.coconut)
        newCoconut.layoutParams = coconut.layoutParams

        // Add the shot to the layout
        (findViewById<View>(android.R.id.content) as? ViewGroup)?.addView(newCoconut)

        // Set random initial position of the shot along the X-axis
        val screenWidth = (findViewById<View>(android.R.id.content) as ViewGroup).width
        val randomX = random.nextInt(screenWidth - newCoconut.width)

        newCoconut.translationX = randomX.toFloat()
        newCoconut.translationY = -newCoconut.height.toFloat()

        // Animate shot to move from top to bottom
        val shotAnimator = ObjectAnimator.ofFloat(
            newCoconut,
            "translationY",
            0f,
            (findViewById<View>(android.R.id.content) as ViewGroup).height.toFloat()
        )
        shotAnimator.apply {
            duration = (1500 / gameSpeedMultiplier).toLong() // Adjust duration based on game speed multiplier
            interpolator = AccelerateInterpolator() // Apply acceleration to the animation
            start() // Start the animation
        }

        // Set visibility of shot to visible during animation
        shotAnimator.doOnStart {
            newCoconut.visibility = View.VISIBLE
        }

        // Remove the shot from the layout after animation ends
        shotAnimator.doOnEnd {
            (findViewById<View>(android.R.id.content) as? ViewGroup)?.removeView(newCoconut)
        }

        // Check if the goalie catches the shot
        shotAnimator.doOnEnd {
            if (isNewtonCatchApple(newCoconut)) {
                // If the goalie catches the shot, end the game
                gameOver()
            }
        }
    }

    private fun isNewtonCatchApple(apple: ImageView): Boolean {
        // Check if the ball is within the horizontal bounds of the goalie
        val newtonX = newtonImg.x
        val newtonWidth = newtonImg.width
        val appleX = apple.translationX
        return appleX in newtonX..(newtonX + newtonWidth)
    }

    private fun gameOver() {
        // Create an AlertDialog Builder
        val builder = AlertDialog.Builder(this)
        // Set the title and message
        builder.setTitle("Game Over")
            .setMessage("You saved Gravity : $score Times")
            // Add a button to retry the game
            .setPositiveButton("Retry") { _, _ ->
                // Reset the game
                resetGame()
            }
            // Add a button to go to the Home screen
            .setNegativeButton("Go Home") { _, _ ->
                // Navigate to the Home screen
                val intent = Intent(this, LaunchPage1::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }

        // Create the dialog
        val dialog = builder.create()
        // Set custom animations
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        // Set custom button icons
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Set image buttons for positive and negative buttons
            val retryIcon = resources.getDrawable(R.drawable.ic_retry)
            retryIcon.setBounds(0, 0, 60, 60) // Set the size of the Retry button
            positiveButton.setCompoundDrawables(retryIcon, null, null, null)

            val goHomeIcon = resources.getDrawable(R.drawable.ic_go_home)
            goHomeIcon.setBounds(0, 0, 60, 60) // Set the size of the Go Home button
            negativeButton.setCompoundDrawables(goHomeIcon, null, null, null)
        }

        // Show the dialog
        dialog.show()
    }

    private fun resetGame() {
        // Reset the score
        score = 0
        updateScore()
        // Restart dropping balls and shots randomly
        startRandomDropping()
    }

    private fun updateScore() {
        // Display the score on the screen
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        scoreTextView.text = "Score: $score"

        // Update highest score in SharedPreferences if current score surpasses it
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val highestScore = sharedPreferences.getInt("highestScore", 0)
        if (score > highestScore) {
            val editor = sharedPreferences.edit()
            editor.putInt("highestScore", score)
            editor.apply()
        }
    }

    private fun startCountdownAnimation() {
        // Create a new TextView for countdown
        val countdownTextView = TextView(this)
        countdownTextView.text = "3"
        countdownTextView.textSize = 100f
        countdownTextView.setTextColor(Color.WHITE)
        countdownTextView.gravity = Gravity.CENTER

        // Add the TextView to the layout
        (findViewById<View>(android.R.id.content) as ViewGroup).addView(countdownTextView)

        // Set initial scale and alpha for the countdown TextView
        countdownTextView.scaleX = 0f
        countdownTextView.scaleY = 0f
        countdownTextView.alpha = 0f

        // Animate the countdown TextView
        val countDownAnimation = ObjectAnimator.ofPropertyValuesHolder(
            countdownTextView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
        )
        countDownAnimation.duration = 1000
        countDownAnimation.interpolator = DecelerateInterpolator()
        countDownAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Start the countdown after the animation ends
                startGameAfterCountdown(countdownTextView)
            }
        })
        countDownAnimation.start()
    }

    private fun startGameAfterCountdown(countdownTextView: TextView) {
        // Start a 1-second countdown
        Handler(Looper.getMainLooper()).postDelayed({
            countdownTextView.text = "2"
            // Scale down the TextView for the next number
            ObjectAnimator.ofPropertyValuesHolder(
                countdownTextView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)
            ).apply {
                duration = 500
                start()
            }
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            countdownTextView.text = "1"
            // Scale down the TextView for the next number
            ObjectAnimator.ofPropertyValuesHolder(
                countdownTextView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)
            ).apply {
                duration = 500
                start()
            }
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            countdownTextView.text = "Go!"
            // Scale down the TextView for the final "Go!" message
            ObjectAnimator.ofPropertyValuesHolder(
                countdownTextView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)
            ).apply {
                duration = 500
                start()
            }
        }, 3000)

        // Start the game after the countdown finishes
        Handler(Looper.getMainLooper()).postDelayed({
            // Remove the countdown TextView from the layout
            (findViewById<View>(android.R.id.content) as ViewGroup).removeView(countdownTextView)
            // Start dropping balls and shots randomly
            startRandomDropping()
        }, 4000)
    }

    private var pauseDialog: Dialog? = null

    private fun showPauseDialog() {
        // Create a dialog to show that the game is paused
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Paused")
            .setMessage("Tap 'Resume' to continue playing.")
            .setPositiveButton("Resume") { _, _ -> resumeGame() }
            .setNegativeButton("Go Home") { _, _ ->
                // Navigate to the Home screen
                val intent = Intent(this, LaunchPage1::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        pauseDialog = builder.create()
        pauseDialog?.setCancelable(false)
        pauseDialog?.show()
    }

    private fun dismissPauseDialog() {
        // Dismiss the pause dialog if it's showing
        pauseDialog?.dismiss()
    }

}
