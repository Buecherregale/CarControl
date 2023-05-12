package de.buecherregale.carcontrol.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import de.buecherregale.carcontrol.R
import de.buecherregale.carcontrol.api.CarControlService
import de.buecherregale.carcontrol.api.Constants
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.exception.ExceptionHandler
import de.buecherregale.carcontrol.controller.SpeedController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TiltControl : AppCompatActivity() {

    private lateinit var apiController: RestApiController
    private lateinit var service: CarControlService

    private lateinit var constants: Constants

    private lateinit var speedController: SpeedController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tilt_control)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        Log.d("Tilt", "changed activity to TiltControl")

        val ip = intent.getStringExtra("ip")!!
        val port = intent.getStringExtra("port")!!.toInt()
        val url = RestApiController.buildURL(ip, port)

        Log.d("API", "connecting to url from intent: $url")

        apiController = RestApiController(RestApiController.buildURL(ip, port))
        service = apiController.getService()

        val gasBtn = findViewById<Button>(R.id.gas)
        val breakBtn = findViewById<Button>(R.id.breaking)
        val clutchBtn = findViewById<Button>(R.id.clutch)

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("API", "fetching constants")
            constants = service.getConstants()
            Log.d("Tilt", "initialising speed controller")
            speedController = SpeedController(url,  constants,
                gas=gasBtn, breaking=breakBtn, clutch=clutchBtn, findViewById(R.id.currentSpeedText),
                100, changePerDelay=constants.speedOffset / 4, breakPerDelay=constants.speedOffset / 2)
        }
    }
}