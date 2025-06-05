package com.example.temple_run.logic

import kotlin.random.Random
import com.example.temple_run.utilies.SignalManager

class gameManager(private val lifeCount: Int) {

     var hits: Int = 0
        private set
    var cointSpawnCounter: Int = 0 //used to dictate when to spawn coins, so its random, but not spam

    fun playerMovement(currentLane: Int, direction: Int) :Int
    {
        if((currentLane==0&&direction==-1)||(currentLane==4&&direction==1))
            return currentLane
        return currentLane+direction
    }

    fun moveObstacleGrid(currentLane: Int, obstaclePosition: MutableList<MutableList<Int>>,hearts_layout_size:Int):Int {
        val num_rows = obstaclePosition.size
        val num_cols = obstaclePosition[0].size
        var coinCollision = 5
        //detect collision
        if (obstaclePosition[num_rows - 1][currentLane] == 1) {
            hits++
            SignalManager.getInstance().toast("you've hit an obstacle")
            SignalManager.getInstance().vibrate()
            SignalManager.getInstance().playSound()
        } else if (obstaclePosition[num_rows - 1][currentLane] == 2)
            coinCollision = 20

        for (rowIndex in num_rows - 2 downTo 0) //move each obstacle one step forward
            for (colIndex in 0 until num_cols) {
                obstaclePosition[rowIndex + 1][colIndex] = obstaclePosition[rowIndex][colIndex]
                obstaclePosition[rowIndex][colIndex] = 0

            }
        //randomly add a new obstacle

        var randomCol = Random.nextInt(0, num_cols)
        obstaclePosition[0][randomCol] = 1

        //coin addition logic here
        if (cointSpawnCounter == 0) {
            cointSpawnCounter = Random.nextInt(2, 5)
            randomCol = Random.nextInt(0, num_cols)
            obstaclePosition[0][randomCol] = 2
        }
        else {

            cointSpawnCounter--
        }

        return coinCollision
    }


}