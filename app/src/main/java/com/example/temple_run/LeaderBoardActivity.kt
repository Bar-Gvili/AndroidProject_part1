package com.example.temple_run

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.temple_run.Fragments.FragmentHighScoreList
import com.example.temple_run.Fragments.FragmentMap
import com.example.temple_run.databinding.ActivityLeadboardBinding
import com.example.temple_run.model.Score
import com.example.temple_run.utilies.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log



class LeaderBoardActivity: AppCompatActivity(), FragmentHighScoreList.OnScoreSelectedListener {

    private lateinit var binding: ActivityLeadboardBinding
    private var allScores: List<Score> = listOf() // To hold loaded scores
    private val gson = Gson()
    private var scoresList: List<Score> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeadboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadScoresFromPrefs() // Load the scores
        setupFragments()      // Setup and display fragments

        binding.buttonBackToMenu.setOnClickListener {
            // Intent to go back to MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears the activity stack above MenuActivity
            startActivity(intent)
            finish()
        }
    }

    private fun loadScoresFromPrefs() {
        val sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val scoresJson = sharedPreferences.getString(Constants.SCORES_KEY, null)

        if (scoresJson != null) {
            val type = object : TypeToken<List<Score>>() {}.type
            scoresList = gson.fromJson(scoresJson, type)
            scoresList = scoresList.sortedByDescending { it.score }
        } else {
            scoresList = emptyList()
        }
    }

    private fun setupFragments() {
        // Create instances of your fragments
        // The best way to pass data to fragments is via newInstance pattern with arguments.

        val highScoreListFragment = FragmentHighScoreList.newInstance(ArrayList(scoresList)) // Pass scores as ArrayList
        val mapFragment = FragmentMap.newInstance(ArrayList(scoresList)) // Pass scores as ArrayList

        // Use FragmentManager to add fragments to their containers
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_high_score_list, highScoreListFragment)
            replace(R.id.frame_map, mapFragment,"FragmentMapTag")
            commit()
        }
    }

    override fun onScoreItemSelected(score: Score) {
        Log.d("LeaderBoardActivity", "onScoreItemSelected called with score: ${score.score} at Lng: ${score.longitude}, Lat: ${score.latitude}") // Add this log
        val mapFragment = supportFragmentManager.findFragmentByTag("FragmentMapTag") as? FragmentMap
        if (mapFragment == null) {
            Log.e("LeaderBoardActivity", "FragmentMapTag not found!") // Add this log
        }
        mapFragment?.focusOnLocation(score)
    }

}
