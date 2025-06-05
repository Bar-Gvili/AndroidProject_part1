package com.example.temple_run

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.temple_run.databinding.ActivityMenuBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.temple_run.utilies.Constants


class MenuActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to your XML layout file
        // Make sure your XML file is named appropriately, e.g., activity_game_settings.xml
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root) // Replace your_layout_file_name

        val GAME_MODE = "com.Temple_Run.MenuActivity.GAME_MODE"
        val SPEED = "com.Temple_Run.MenuActivity.SPEED"


        // Set default states based on XML (or override here if needed)
        // switchGameMode is unchecked (false) by default in XML -> Buttons
        // switchSpeed is checked (true) by default in XML -> Fast

        // Set click listener for the start button
        binding.buttonStart.setOnClickListener {
            // Determine the selected game mode
            val gameModeValue = if (binding.switchGameMode.isChecked) {
                Constants.GAME_MODE_BUTTONS// Switch is ON (checked)
            } else {
                Constants.GAME_MODE_SENSOR  // Switch is OFF (unchecked)
            }

            // Determine the selected speed
            val speedValue = if (binding.switchSpeed.isChecked) {
                Constants.Timer.FAST // Switch is ON (checked)
            } else {
                Constants.Timer.SLOW // Switch is OFF (unchecked)
            }

            // Create an Intent to start MainActivity
            // Replace MainActivity::class.java with your actual MainActivity class
            val intent = Intent(this, MainActivity::class.java)

            // Add the game mode and speed as extras to the Intent
            intent.putExtra(GAME_MODE, gameModeValue)
            intent.putExtra(SPEED, speedValue)

            // Start MainActivity
            startActivity(intent)

            // Optional: Finish this activity if you don't want users to return to it
            finish()
        }
        binding.buttonLeaderboard.setOnClickListener {
            val intent = Intent(this, LeaderBoardActivity::class.java)
            startActivity(intent)
        }
    }

}