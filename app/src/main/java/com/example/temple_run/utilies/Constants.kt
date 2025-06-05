package com.example.temple_run.utilies

class Constants {
    object Timer {
        const val SLOW :Long = 1_000L
        const val FAST :Long = 5_00L
    }

    object obstacleDimensions {
        const val obstacle_row_count: Int = 8
        const val obstacle_col_count: Int = 5
    }
    companion object {
        // Possible values for game mode
        const val GAME_MODE_SENSOR = "sensor"
        const val GAME_MODE_BUTTONS = "buttons"
    }

}