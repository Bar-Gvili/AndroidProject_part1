package com.example.temple_run.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.temple_run.databinding.FragmentMapBinding // Assuming you have this binding
import com.example.temple_run.model.Score
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

//import com.google.android.gms.maps.model.MarkerOptions

class FragmentMap : Fragment() ,OnMapReadyCallback{

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var scores: ArrayList<Score> = ArrayList()
    private var googleMapInstance: GoogleMap? = null
    private var pendingScoreToFocus: Score? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            scores = it.getParcelableArrayList<Score>(ARG_SCORES) ?: ArrayList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (scores.isNotEmpty()) {
            scores.forEach { score ->
                Log.d("FragmentMap", "Score: ${score.score} at (${score.latitude}, ${score.longitude})")
                // Add markers to the map here using score.latitude and score.longitude
            }
        } else {
            Log.d("FragmentMap", "No scores with location data to display on map.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // binding.mapView?.onDestroy() // If using MapView directly
        _binding = null
    }

    fun focusOnLocation(score: Score) {
        Log.d("FragmentMap", "focusOnLocation CALLED for score: ${score.score} at Lng: ${score.longitude}, Lat: ${score.latitude}")

        if (googleMapInstance == null) {
            Log.w("FragmentMap", "Map not ready yet in focusOnLocation. Queuing focus request for score ${score.score}.")
            pendingScoreToFocus = score // Store the score to focus on later
            return // Exit, map is not ready to be manipulated
        }
        // If map is ready, proceed to focus immediately
        Log.d("FragmentMap", "Map is ready in focusOnLocation. Proceeding with animation for score ${score.score}.")
        performFocusAnimation(score)
        // If this call directly handles a score, it should probably clear any older pending score
        // that might have been set if multiple clicks happened rapidly before map was ready.
        pendingScoreToFocus = null
    }

    private fun isValidLatLng(lat: Double, lng: Double): Boolean {
        if (lat == 0.0 && lng == 0.0) return false
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0
    }

    companion object {
        private const val ARG_SCORES = "scores_list_arg_map"

        @JvmStatic
        fun newInstance(scores: ArrayList<Score>) =
            FragmentMap().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_SCORES, scores)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMapInstance = googleMap
        Log.d("FragmentMap", "Map IS READY.")

        // Display all markers initially
        displayAllMarkersAndSetBounds()

        // Check if there's a pending score to focus on
        pendingScoreToFocus?.let { scoreToFocus ->
            Log.d("FragmentMap", "Map is ready, now handling PENDING focus for score ${scoreToFocus.score}")
            performFocusAnimation(scoreToFocus) // Apply the pending focus
            pendingScoreToFocus = null // Clear the pending request
        }
    }

    private fun displayAllMarkersAndSetBounds() {
        googleMapInstance?.clear() // Clear previous markers before adding new ones
        if (scores.isEmpty()) {
            Log.d("FragmentMap", "No scores to display on map (in displayAllMarkersAndSetBounds).")
            googleMapInstance?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 2f))
            return
        }

        val boundsBuilder = LatLngBounds.Builder()
        var validLocationsFound = false

        scores.forEach { score ->
            if (isValidLatLng(score.latitude, score.longitude)) {
                val location = LatLng(score.latitude, score.longitude)
                googleMapInstance?.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Score: ${score.score}")
                        .snippet(score.getFormattedDate())
                )
                boundsBuilder.include(location)
                validLocationsFound = true
            }
        }

        if (validLocationsFound) {
            try {
                val bounds = boundsBuilder.build()
                googleMapInstance?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // 100 is padding
            } catch (e: IllegalStateException) {
                // This can happen if no valid locations were added to boundsBuilder
                Log.e("FragmentMap", "Error building bounds, likely no valid locations: ${e.message}")
                googleMapInstance?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0,0.0), 2f))
            }
        } else {
            Log.d("FragmentMap", "No valid locations found to set initial map bounds.")
            googleMapInstance?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 2f))
        }
    }

    private fun performFocusAnimation(score: Score) {
        googleMapInstance?.let { map ->
            Log.d("FragmentMap", "performFocusAnimation for score ${score.score}.")
            if (isValidLatLng(score.latitude, score.longitude)) {
                Log.d("FragmentMap", "Coordinates ARE valid in performFocusAnimation for score ${score.score}.")
                val location = LatLng(score.latitude, score.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                Log.d("FragmentMap", "Animating camera to: $location with zoom 15f.")
            } else {
                Log.w("FragmentMap", "Coordinates are INVALID for score ${score.score} in performFocusAnimation. Lat: ${score.latitude}, Lng: ${score.longitude}")
            }
        } ?: run {
            Log.e("FragmentMap", "performFocusAnimation called but googleMapInstance is still null. This shouldn't happen if called from onMapReady or a ready focusOnLocation.")
        }
    }

}