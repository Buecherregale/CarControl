package de.buecherregale.carcontrol.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import de.buecherregale.carcontrol.R
import de.buecherregale.carcontrol.api.CarControlService
import de.buecherregale.carcontrol.api.Constants
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.api.Speed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TiltControl : AppCompatActivity() {

    private lateinit var apiController: RestApiController
    private lateinit var service: CarControlService

    private lateinit var constants: Constants

    private var currentSpeed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tilt_control)

        apiController = RestApiController(RestApiController.buildURL("", 3))
        service = apiController.getService()

        val gasBtn = findViewById<Button>(R.id.gas)
        val breakBtn = findViewById<Button>(R.id.breaking)
        val clutchBtn = findViewById<Button>(R.id.clutch)

        CoroutineScope(Dispatchers.Main).launch {
            constants = service.getConstants()
            currentSpeed = constants.speedCenter
        }
    }

    private suspend fun whileGasDown(changePerDelay: Int) {
        var target = currentSpeed + changePerDelay
        if(target > constants.speedCenter + constants.speedOffset) {
            target = constants.speedCenter + constants.speedOffset
        }
        updateSpeed(target)
    }

    private suspend fun whileGasUp(changePerDelay: Int) {

    }

    private suspend fun updateSpeed(newValue: Int) {
        service.postSpeed(Speed(newValue))
    }

    @SuppressLint("ClickableViewAccessibility") // keiner von uns ist blind
            /**
             * @param button the button to register and setup the listener for
             * @param delay amount of milliseconds between each update and call of repeat()
             * @param whileButtonDown the function to call while the button is hold down, suspend to allow api calls
             * @param whileButtonUp the function to call while the button is up
             */
    fun setupButtonTouchListener(button: Button, delay: Long, whileButtonDown: suspend () -> Unit, whileButtonUp: suspend() -> Unit) {
        var buttonDownJob: Job? = null
        var buttonUpJob: Job? = null
        button.setOnTouchListener {v, event ->
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    buttonUpJob?.cancel()
                    buttonDownJob = CoroutineScope(Dispatchers.Main).launch {
                        while(isActive) {
                            delay(delay)
                            whileButtonDown()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    buttonDownJob?.cancel()
                    buttonUpJob = CoroutineScope(Dispatchers.Main).launch {
                        while(isActive) {
                            delay(delay)
                            whileButtonUp()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
}