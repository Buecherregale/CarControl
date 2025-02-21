package de.buecherregale.carcontrol.controller

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import de.buecherregale.carcontrol.api.Constants
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.views.SemiCircleProgressBar
import kotlinx.coroutines.*

@SuppressLint("ClickableViewAccessibility")
class MotorController(url: String, constants: Constants,
                      gas: Button, breaking: Button, clutch: Button,
                      private val progress: SemiCircleProgressBar,
                      delay: Long, changePerDelay: Int, breakPerDelay: Int) {

    private val apiController = RestApiController(url)

    // shorthands
    private val motorCenter: Int = constants.motorCenter
    private val motorOffset: Int = constants.motorOffset

    private var currentSpeed = motorCenter


    private var pressingBreak = false
    private var pressingGas = false
    private var clutchPressed = true

    private var updateJob: Job? = null

    private var enabled = true

    init {
        Log.d("MotorController", "requesting to: $url")
        Log.d("MotorController", """using: 
            delay: $delay,
            changePerDelay: $changePerDelay,
            breakPerDelay: $breakPerDelay
        """.trimIndent())
        Log.d("MotorController", "setting up listener...")

        progress.min = constants.motorMin
        progress.max = constants.motorMax
        progress.progress = constants.motorCenter

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
                                if(currentSpeed < constants.motorMax) {
                                    changeSpeed(getSpeedInBounds(currentSpeed + changePerDelay))
                                }
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
                                if(currentSpeed > motorCenter) {
                                    changeSpeed(getSpeedInBounds(currentSpeed - changePerDelay / 2))
                                }
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
                    if(!clutchPressed && currentSpeed <= motorCenter) return@setOnTouchListener true
                    updateJob = CoroutineScope(Dispatchers.Main).launch {
                        while(isActive && !(!clutchPressed && currentSpeed <= motorCenter)) {
                            delay(delay)
                            if(currentSpeed > constants.motorMin) {
                                var target = currentSpeed - breakPerDelay
                                if (target < motorCenter - motorOffset) {
                                    target = motorCenter - motorOffset
                                }
                                changeSpeed(target)
                            }
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
                changeSpeed(motorCenter)
            }
        }
        Log.d("MotorController", "listener setup completed")
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private fun getSpeedInBounds(newSpeed: Int) : Int {
        var target = newSpeed

        if(newSpeed > motorCenter + motorOffset) target = motorCenter + motorOffset
        return target
    }

    private suspend fun changeSpeed(newSpeed: Int) {
        if(!enabled) return

        currentSpeed = newSpeed
        apiController.getService().postMotor(newSpeed)
        progress.progress = currentSpeed
        Log.d("MotorController", "changing speed to $newSpeed")
    }
}