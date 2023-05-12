package de.buecherregale.carcontrol.controller

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import de.buecherregale.carcontrol.api.Constants
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.api.Speed
import kotlinx.coroutines.*

@SuppressLint("ClickableViewAccessibility")
class SpeedController(url: String, constants: Constants,
                      gas: Button, breaking: Button, clutch: Button,
                      private val currentSpeedText: TextView,
                      delay: Long, changePerDelay: Int, breakPerDelay: Int) {

    private val apiController = RestApiController(url)

    private val speedCenter: Int = constants.speedCenter
    private val speedOffset: Int = constants.speedOffset

    private var currentSpeed = 0

    private var pressingBreak = false
    private var pressingGas = false
    private var clutchPressed = true

    private var updateJob: Job? = null

    init {
        Log.d("SpeedController", "requesting to: $url")
        Log.d("SpeedController", """using: 
            delay: $delay,
            changePerDelay: $changePerDelay,
            breakPerDelay: $breakPerDelay
        """.trimIndent())
        Log.d("SpeedController", "setting up listener...")
        gas.setOnTouchListener { view, motionEvent ->
             when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressingGas = true
                    clutchPressed = false
                    if(!pressingBreak) {
                        updateJob?.cancel()
                        updateJob = CoroutineScope(Dispatchers.Main).launch {
                            while(isActive) {
                                delay(delay)
                                Log.d("SpeedController", "iterate update job SPEED DOWN")
                                changeSpeed(getSpeedInBounds(currentSpeed + changePerDelay))
                            }
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    pressingGas = false
                    if(!pressingBreak) {
                        updateJob?.cancel()
                        updateJob = CoroutineScope(Dispatchers.Main).launch {
                            while(isActive) {
                                delay(delay)
                                Log.d("SpeedController", "iterate update job SPEED IP")
                                changeSpeed(getSpeedInBounds(currentSpeed - changePerDelay))
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
        breaking.setOnTouchListener {view, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressingBreak = true
                    updateJob?.cancel()

                    updateJob = CoroutineScope(Dispatchers.Main).launch {
                        while(isActive) {
                            delay(delay)
                            var target = currentSpeed - breakPerDelay
                            if (target < constants.speedCenter - constants.speedOffset) {
                                target = constants.speedCenter - constants.speedOffset
                            }

                            changeSpeed(target)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    pressingBreak = false
                    view.performClick()
                    updateJob?.cancel()
                    true
                }
                else -> false
            }
        }
        clutch.setOnClickListener {
            updateJob?.cancel()
            clutchPressed = true
            CoroutineScope(Dispatchers.Main).launch {
                changeSpeed(speedCenter)
            }
        }
        Log.d("SpeedController", "listener setup completed")
    }

    private fun getSpeedInBounds(newSpeed: Int) : Int {
        var target = newSpeed
        if(speedCenter > newSpeed) target = speedCenter
        if(newSpeed > speedCenter + speedOffset) target = speedCenter + speedOffset
        return target
    }

    private suspend fun changeSpeed(newSpeed: Int) {
        currentSpeed = newSpeed
        apiController.getService().postSpeed(Speed(newSpeed))
        currentSpeedText.text = currentSpeed.toString()
        Log.d("SpeedController", "changing speed to $newSpeed")
    }
}