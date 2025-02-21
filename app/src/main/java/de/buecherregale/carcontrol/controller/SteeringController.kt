package de.buecherregale.carcontrol.controller

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import de.buecherregale.carcontrol.api.Constants
import de.buecherregale.carcontrol.api.RestApiController
import de.buecherregale.carcontrol.views.SemiCircleProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class SteeringController(sensorManager: SensorManager, private val progress: SemiCircleProgressBar, private val apiController: RestApiController, private val constants: Constants, private val tolerance: Int): SensorEventListener {

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var currentServo: Int = constants.servoCenter

    private var enabled = true

    init {
        Log.d("Steering Controller", "Init SteeringController")
        Log.d("Steering Controller", "Using tolerance $tolerance")

        progress.min = constants.servoMin
        progress.max = constants.servoMax
        progress.progress = constants.servoCenter

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(!enabled) return
        if(event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
        // nutze accelerometerReading[1] für links rechts tilt max: 9.809989 min: -9.809989

        val servo: Int = mapOrientationToServo(accelerometerReading[1])
        if(abs(servo - currentServo) > tolerance) {
            currentServo = servo
            CoroutineScope(Dispatchers.Main).launch {
                apiController.getService().postServo(servo)
            }
            progress.progress = servo
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun setEnabled(enabled: Boolean) {
        currentServo = constants.servoCenter
        CoroutineScope(Dispatchers.Main).launch {
            apiController.getService().postServo(constants.servoCenter)
        }
        progress.progress = constants.servoCenter
        this.enabled = enabled
    }

    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.
    }

    private fun mapOrientationToServo(orientation: Float): Int {
        val min = -9.809989
        val max = 9.809989

        if (orientation < min) return constants.servoMin
        if (orientation > max) return constants.servoMax

        val ratio: Double = orientation / max
        val servo = constants.servoOffset * ratio

        return constants.servoCenter + servo.toInt()
    }
}