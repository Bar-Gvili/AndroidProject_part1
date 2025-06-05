package com.example.temple_run.utilies

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import java.lang.ref.WeakReference

class SignalManager private constructor(context: Context) {
    private val contextRef = WeakReference(context)

    companion object {
        @Volatile
        private var instance: SignalManager? = null

        fun init(context: Context): SignalManager {
            return instance ?: synchronized(this) {
                instance ?: SignalManager(context).also { instance = it }
            }
        }

        fun getInstance(): SignalManager {
            return instance ?: throw IllegalStateException(
                "SignalManager must be initialized by calling init(context) before use."
            )
        }
    }

    fun toast(text: String) {
        contextRef.get()?.let { context: Context ->
            Toast
                .makeText(
                    context,
                    text,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    fun vibrate() {
        contextRef.get()?.let { context: Context ->
            val vibrator: Vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    context.getSystemService(VIBRATOR_SERVICE) as Vibrator
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val oneShotVibrationEffect =
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )

//                vibrator.vibrate(oneShotVibrationEffect)
                vibrator.vibrate(oneShotVibrationEffect)
            } else {
                vibrator.vibrate(500)
            }
        }
    }

    fun playSound() {
        contextRef.get()?.let { context: Context ->
            try {
                // Get the URI for the default notification sound
                val notificationSoundUri: Uri? =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                notificationSoundUri?.let { uri ->
                    // Get the Ringtone object
                    val ringtone = RingtoneManager.getRingtone(context, uri)
                    ringtone?.play()
                } ?: run {
                    // Fallback or error logging if no default notification sound is found
                    toast("No default notification sound found.")
                }
            } catch (e: SecurityException) {
                // Catch potential SecurityException, though rare for default sounds
                toast("Permission denied for playing sound.")
                e.printStackTrace() // Log the exception for debugging
            } catch (e: Exception) {
                // Catch any other exceptions during sound playback
                toast("Error playing sound.")
                e.printStackTrace() // Log the exception for debugging
            }
        }
    }
}