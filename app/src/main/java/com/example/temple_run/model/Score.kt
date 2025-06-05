// In Temple_Run/app/src/main/java/com/example/temple_run/model/Score.kt
package com.example.temple_run.model

import android.os.Parcelable // Import Parcelable
import kotlinx.parcelize.Parcelize // Import Parcelize

@Parcelize // Add Parcelize annotation
class Score(
    val score: Int,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
) : Parcelable { // Implement Parcelable
    fun getFormattedDate(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}