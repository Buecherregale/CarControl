package de.buecherregale.carcontrol.activities

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import de.buecherregale.carcontrol.R
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.exception.ExceptionHandler
import de.buecherregale.carcontrol.controller.MotorController
import de.buecherregale.carcontrol.controller.SteeringController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TiltControl : AppCompatActivity() {


    private lateinit var motorController: MotorController
    private lateinit var steeringController: SteeringController

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tilt_control)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        Log.d("Tilt", "changed activity to TiltControl")

        val ip = intent.getStringExtra("ip")!!
        val port = intent.getStringExtra("port")!!.toInt()
        val url = RestApiController.buildURL(ip, port)

        Log.d("API", "connecting to url from intent: $url")

        val apiController = RestApiController(RestApiController.buildURL(ip, port))
        val service = apiController.getService()

        val gasBtn = findViewById<Button>(R.id.gas)
        val breakBtn = findViewById<Button>(R.id.breaking)
        val clutchBtn = findViewById<Button>(R.id.clutch)

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("API", "fetching constants")
            val constants = service.getConstants()

             motorController = MotorController(url,  constants,
                gas=gasBtn, breaking=breakBtn, clutch=clutchBtn,
                 findViewById(R.id.currentSpeedText), findViewById(R.id.currentSpeed),
                100, changePerDelay=constants.motorOffset / 40, breakPerDelay=constants.motorOffset / 20)

            steeringController = SteeringController(getSystemService(Context.SENSOR_SERVICE) as SensorManager,
                findViewById(R.id.currentServoText), findViewById(R.id.currentServo),
                apiController, constants, 50)
        }
        // disable components based on switches
        val tempomat = findViewById<Switch>(R.id.tempomat)
        tempomat.setOnCheckedChangeListener { _, checked ->
            motorController.setEnabled(!checked)
        }
        val spurhalten = findViewById<Switch>(R.id.spurhalten)
        spurhalten.setOnCheckedChangeListener { _, checked ->
            steeringController.setEnabled(!checked)
        }
    }
}