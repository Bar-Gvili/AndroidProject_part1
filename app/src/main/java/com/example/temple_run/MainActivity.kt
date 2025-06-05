package com.example.temple_run

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import com.example.temple_run.logic.gameManager
import com.example.temple_run.utilies.Constants
import com.example.temple_run.databinding.ActivityMainBinding
import com.example.temple_run.model.Score
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.content.edit
import android.Manifest // Add this
import android.content.pm.PackageManager // Add this
import androidx.core.app.ActivityCompat // Add this
import com.google.android.gms.location.FusedLocationProviderClient // Add this
import com.google.android.gms.location.LocationServices // Add this

class MainActivity : AppCompatActivity(),SensorEventListener {

    private lateinit var mainHeartsLayout: Array<ImageView>
    private lateinit var mainGridImages: Array<Array<ImageView>>
    private lateinit var carLayout: Array<ImageView>
    private lateinit var mainGridPositions: MutableList<MutableList<Int>>
    private lateinit var gameManager: gameManager
    private lateinit var gameMode: String
    private var speed: Long =0
    private var currentLane: Int = 2
    private var startTime: Long = 0
    private var timerOn: Boolean = false
    private var timerJob: Job?=null
    private var score: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val gson = Gson() // For converting Score objects to/from JSON


    // --- Sensor Related Properties ---
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastSensorUpdateTime: Long = 0 // To control update frequency
    private val sensorUpdateDelay: Long = 100 // Milliseconds, adjust for responsiveness
    private val tiltThreshold: Float = 3f // Tilt sensitivity, adjust this value (m/s^2)

    private lateinit var binding: ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityMainBinding.inflate(layoutInflater)
            enableEdgeToEdge()
            setContentView(binding.root)
            //setContentView(R.layout.activity_main)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // Add this
            findViews()
            //gameManager = gameManager(main_hearts_layout.size)
            gameManager = gameManager(binding.mainHeartsLayout.size)
            mainGridPositions =
                MutableList(Constants.obstacleDimensions.obstacle_row_count){MutableList(Constants.obstacleDimensions.obstacle_col_count){0}}
            initViewsAndGameSettings()
            refreshUI()

        //gameTimer = GameTimer(lifecycleScope)
    }
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume - Starting timer")
        // Start the timer when the activity comes into the foreground
        startTimer()
        // Register sensor listener only if in sensor mode and sensor is available
        if (gameMode == Constants.GAME_MODE_SENSOR && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            Log.d("MainActivity", "Accelerometer listener registered.")
        }
    }
    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause - Stopping timer")
        // Stop the timer when the activity goes into the background
        stopTimer()
        // Unregister sensor listener to save power
        if (gameMode == Constants.GAME_MODE_SENSOR && accelerometer != null) {
            sensorManager.unregisterListener(this)
            Log.d("MainActivity", "Accelerometer listener unregistered.")
        }
    }


    private fun findViews() {
        //connect between each imageView to an array so we can access the images
    carLayout = arrayOf(
        binding.mainCar0,
        binding.mainCar1,
        binding.mainCar2,
        binding.mainCar3,
        binding.mainCar4
    )
        //connect between each imageView to an array so we can access the images
    mainHeartsLayout = arrayOf(
        binding.mainHeart0,
        binding.mainHeart1,
        binding.mainHeart2)


        //connect between each imageView to a matrix so we can access the images
    mainGridImages = arrayOf(
            arrayOf(
                binding.mainObstacle00,
                binding.mainObstacle01,
                binding.mainObstacle02,
                binding.mainObstacle03,
                binding.mainObstacle04,
            ),
            arrayOf(
                binding.mainObstacle10,
                binding.mainObstacle11,
                binding.mainObstacle12,
                binding.mainObstacle13,
                binding.mainObstacle14,
            ),
            arrayOf(
                binding.mainObstacle20,
                binding.mainObstacle21,
                binding.mainObstacle22,
                binding.mainObstacle23,
                binding.mainObstacle24,
            ),
            arrayOf(
                binding.mainObstacle30,
                binding.mainObstacle31,
                binding.mainObstacle32,
                binding.mainObstacle33,
                binding.mainObstacle34,
            ),
            arrayOf(
                binding.mainObstacle40,
                binding.mainObstacle41,
                binding.mainObstacle42,
                binding.mainObstacle43,
                binding.mainObstacle44,
            ),
            arrayOf(
                binding.mainObstacle50,
                binding.mainObstacle51,
                binding.mainObstacle52,
                binding.mainObstacle53,
                binding.mainObstacle54,
            ),
            arrayOf(
                binding.mainObstacle60,
                binding.mainObstacle61,
                binding.mainObstacle62,
                binding.mainObstacle63,
                binding.mainObstacle64,
            ),
            arrayOf(
                binding.mainObstacle70,
                binding.mainObstacle71,
                binding.mainObstacle72,
                binding.mainObstacle73,
                binding.mainObstacle74,
            ))

    }



    private fun initViewsAndGameSettings() {
        gameMode = intent.getStringExtra("com.Temple_Run.MenuActivity.GAME_MODE").toString()
        if(gameMode==Constants.GAME_MODE_SENSOR) {
            //turn sensors on and hide buttons
            binding.mainFABLeft.visibility = View.INVISIBLE
            binding.mainFABRight.visibility = View.INVISIBLE
            setupSensors()
        }

        binding.mainFABLeft.setOnClickListener { view: View -> movementOccurred(-1) }
        binding.mainFABRight.setOnClickListener { view: View -> movementOccurred(1) }
        speed = intent.getLongExtra("com.Temple_Run.MenuActivity.SPEED", Constants.Timer.SLOW)

        // Request location permissions when game settings are initialized
        requestLocationPermissionsIfNeeded()
    }

    private fun movementOccurred(direction: Int) {//direction=0 left, direction=1 right
            currentLane = gameManager.playerMovement(currentLane, direction)
            refreshCarLayout()

    }

    private fun startTimer() {
        if (!timerOn && (timerJob == null || timerJob?.isActive == false)) {
            timerOn = true
            startTime = System.currentTimeMillis()
            timerJob = lifecycleScope.launch {
                Log.d("MainActivity", "Timer coroutine started")
                while (timerOn && isActive ) {
                    try {
                        // 1. Advance Game State based on time
                        val coinCollision = gameManager.moveObstacleGrid(currentLane, mainGridPositions, mainHeartsLayout.size)
                        score = score+ 10 + coinCollision
                        // 2. Update the entire UI
                        refreshUI()
                        // 3. Wait for the next interval
                        delay(speed)
                        // 4. check if game ended
                        if(gameManager.hits==mainHeartsLayout.size)
                        {
                            stopTimer()
                            endGame()
                        }

                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error in game loop: ${e.message}")
                        stopTimer()
                    }
                }
                timerOn = false
            }
        }
    }

    // Function to stop the timer coroutine
    private fun stopTimer() {
        timerOn = false // Signal the loop to stop
        timerJob?.cancel() // Cancel the coroutine
        timerJob = null // Clear the job reference
    }

    private fun refreshUI() {
        changeGridImage()
        refreshObstacleLayout()
        refreshCarLayout()
        refreshHeartLayout()
        binding.MAINScore.text = score.toString()

    }
    private fun requestLocationPermissionsIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                Constants.Location.LOCATION_PERMISSION_REQUEST_CODE)
            Log.d("MainActivity", "Requesting location permissions.")
        } else {
            Log.d("MainActivity", "Location permissions already granted.")
            // Permissions are already granted. You could try to get an initial location here if needed,
            // but it's mainly important for when the game ends.
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.Location.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Location permission GRANTED by user.")
                // Permission was granted. If a game just ended and was waiting for this,
                // you might re-trigger the save process. For now, this is fine.
            } else {
                Log.w("MainActivity", "Location permission DENIED by user.")
                // Permission denied. The game will save scores with default 0.0, 0.0 coordinates.
            }
        }
    }
    private fun refreshObstacleLayout() {
        for(rowIndex in 0 until Constants.obstacleDimensions.obstacle_row_count)
            for(colIndex in 0 until Constants.obstacleDimensions.obstacle_col_count)
            {
                if(mainGridPositions[rowIndex][colIndex]!=0)
                    mainGridImages[rowIndex][colIndex].visibility=View.VISIBLE
                else
                 mainGridImages[rowIndex][colIndex].visibility=View.INVISIBLE
            }
    }
    private fun refreshCarLayout() {
        for(i in 0 until Constants.obstacleDimensions.obstacle_col_count)
            if(i==currentLane)
                binding.mainLAYOUTCarRow[i].visibility=View.VISIBLE//carLayout[i].visibility=View.VISIBLE
            else
                binding.mainLAYOUTCarRow[i].visibility=View.INVISIBLE//carLayout[i].visibility=View.INVISIBLE

    }
    private fun refreshHeartLayout() {
       val index: Int = mainHeartsLayout.size - 1
       for (i in 0..index)
       {
           if(i<gameManager.hits)
               binding.mainHeartsLayout[i].visibility=View.INVISIBLE//main_hearts_layout[i].visibility=View.INVISIBLE
           else
               binding.mainHeartsLayout[i].visibility=View.VISIBLE//main_hearts_layout[i].visibility=View.VISIBLE
       }

   }
    private fun changeGridImage(){

        for (rowIndex in 0 until Constants.obstacleDimensions.obstacle_row_count) {
            for (colIndex in 0 until Constants.obstacleDimensions.obstacle_col_count)
            {
                if (mainGridPositions[rowIndex][colIndex] == 1)
                {
                    mainGridImages[rowIndex][colIndex].apply { //change the image of cell in the grid
                        setImageResource(R.drawable.obstacle)
                    }
                }
                else if(mainGridPositions[rowIndex][colIndex] == 2)
                {
                    mainGridImages[rowIndex][colIndex].apply {
                        setImageResource(R.drawable.coin) //
                    }
                }
            }
        }
    }
    private fun endGame(){
        fetchLocationAndSaveScore()

    }
    private fun fetchLocationAndSaveScore() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // save with default coordinates.
            saveScoreToList(0.0, 0.0)
            proceedToLeaderboard()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                val currentLatitude = location?.latitude ?: 0.0
                val currentLongitude = location?.longitude ?: 0.0
                Log.d("MainActivity", "Location obtained at game end: Lat $currentLatitude, Lon $currentLongitude")
                saveScoreToList(currentLatitude, currentLongitude)
                proceedToLeaderboard()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to get location at game end: ${e.message}. Saving with default location.")
                saveScoreToList(0.0, 0.0) // Save with default location on failure
                proceedToLeaderboard()
            }
    }

    private fun saveScoreToList(latitude: Double, longitude: Double) {
        val sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        val newScoreRecord = Score(
            score = this.score, // Assuming 'this.score' is your current game score variable
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )

        // 1. Retrieve existing scores
        val scoresJson = sharedPreferences.getString(Constants.SCORES_KEY, null)
        val type = object : TypeToken<MutableList<Score>>() {}.type
        val scoresList: MutableList<Score> = if (scoresJson != null) {
            gson.fromJson(scoresJson, type)
        } else {
            mutableListOf()
        }

        // 2. Add new score
        scoresList.add(newScoreRecord)

        // 3. Sort by score descending
        scoresList.sortByDescending { it.score }

        // 4. Keep only the top N scores (e.g., top 10)
        val topScores = scoresList

        // 5. Save updated list
        val updatedScoresJson = gson.toJson(topScores)
        sharedPreferences.edit { // Using the ktx extension function
            putString(Constants.SCORES_KEY, updatedScoresJson)
            // apply() is called automatically by the ktx edit {} block
        }
        Log.d("MainActivity", "Score saved: ${newScoreRecord.score} at ($latitude, $longitude). Total scores in list: ${topScores.size}")
    }

    private fun proceedToLeaderboard() {
        val intent = Intent(this, LeaderBoardActivity::class.java)
        // No need to pass individual score details if LeaderboardActivity loads the whole list
        startActivity(intent)
        finish() // Finish MainActivity after game ends
    }
    private fun saveScore(newScore: Score) {
        val sharedPreferences = getSharedPreferences("GameScores", Context.MODE_PRIVATE)
        sharedPreferences.edit() {
            val gson = Gson()

            // Retrieves existing scores
            val scoresJson = sharedPreferences.getString("scores_list", null)
            val type = object : TypeToken<MutableList<Score>>() {}.type
            val scoresList: MutableList<Score> = gson.fromJson(scoresJson, type) ?: mutableListOf()

            // Add new score and sort (optional, or sort when displaying)
            scoresList.add(newScore)
            scoresList.sortByDescending { it.score } // Example: sort by score descending

            // Save updated list
            val updatedScoresJson = gson.toJson(scoresList)
            putString("scores_list", updatedScoresJson)
        }
    }
    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.e("MainActivity", "Accelerometer sensor not available!")
        }
    }
    // --- SensorEventListener Methods ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (gameMode != Constants.GAME_MODE_SENSOR || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastSensorUpdateTime) > sensorUpdateDelay) {
            lastSensorUpdateTime = currentTime

            val xAcceleration = event?.values?.get(0) ?: 0.0f // X-axis acceleration (includes gravity)
            // Positive xAcceleration typically means device is tilted to its left (player moves right in portrait)
            // Negative xAcceleration typically means device is tilted to its right (player moves left in portrait)
            // This might need to be inverted based on your game's coordinate system or preference.

            //Log.d("SensorDebug", "X: $xAcceleration") // For debugging tilt values

            var direction = 0 // 0 for no move, -1 for left, 1 for right
            if (xAcceleration > tiltThreshold) { // Tilt to device's left -> player moves right
                direction = -1
            } else if (xAcceleration < -tiltThreshold) { // Tilt to device's right -> player moves left
                direction = 1
            }
            movementOccurred(direction)
            /*if (direction != 0) {
                // Attempt to move the player.
                // The gameManager should handle lane boundaries.
                val newLane = gameManager.movePlayerWithSensor(currentLane, direction, Constants.obstacleDimensions.obstacle_col_count)
                if (newLane != currentLane) {
                    currentLane = newLane
                    refreshCarLayout()
                }
            }*/
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}