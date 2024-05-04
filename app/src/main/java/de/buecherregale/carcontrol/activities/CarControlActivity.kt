@file:Suppress("UNCHECKED_CAST")

package de.buecherregale.carcontrol.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import de.buecherregale.carcontrol.R
import de.buecherregale.carcontrol.api.*
import de.buecherregale.carcontrol.exception.ExceptionHandler
import de.buecherregale.carcontrol.listener.BarSlideListener
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.net.UnknownHostException
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1

class CarControlActivity : AppCompatActivity() {

    private lateinit var ip: String
    private var port: Int = 0

    private lateinit var url: String
    private lateinit var apiController: RestApiController
    private lateinit var constants: Constants

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_control)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        ip = intent.getStringExtra("ip")!!
        port = intent.getStringExtra("port")!!.toInt()

        url = RestApiController.buildURL(ip, port)
        apiController = RestApiController(url)

        // GET constants used
        CoroutineScope(Dispatchers.Main).launch {
            constants = get(apiController.getService()::getConstants) as Constants
            initGuiElements()
        }
    }

    private fun displayErrorMessage(message: String) {
        val errorLabel = findViewById<TextView>(R.id.errorLabel)
        errorLabel.text = message
    }

    private fun initGuiElements() {
        // update ui elements
        val talkingTo = findViewById<TextView>(R.id.talkingTo)
        talkingTo.text = getString(R.string.label_talkingTo, ip, port.toString())

        /*
         * use values between 0 to 100 because seekBar.min is not enforced
         */
        val speedBar = findViewById<SeekBar>(R.id.speedBar)
        val servoBar = findViewById<SeekBar>(R.id.servoBar)

        speedBar.progress = 50
        post(Motor(constants.motorCenter),
            apiController.getService()::postMotor as KSuspendFunction1<HttpBody, PostResponse>)

        servoBar.progress = 50
        post(Servo(constants.servoCenter), apiController.getService()::postServo as KSuspendFunction1<HttpBody, PostResponse>)

        // add listener
        val neutralBtn = findViewById<Button>(R.id.neutralBtn)
        neutralBtn.setOnClickListener {
            post(Motor(constants.motorCenter),
            apiController.getService()::postMotor as KSuspendFunction1<HttpBody, PostResponse>)
            speedBar.progress = mapTo0100(constants.motorCenter, constants.motorCenter, constants.motorOffset)
        }
        speedBar.setOnSeekBarChangeListener(object: BarSlideListener() {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                post(Motor(mapToMinMax(progress, constants.motorCenter, constants.motorOffset)),
                    apiController.getService()::postMotor as KSuspendFunction1<HttpBody, PostResponse>)
            }
        })
        servoBar.setOnSeekBarChangeListener(object: BarSlideListener() {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                post(Servo(mapToMinMax(progress, constants.servoCenter, constants.servoOffset)),
                    apiController.getService()::postServo as KSuspendFunction1<HttpBody, PostResponse>)

            }

            override fun onStopTrackingTouch(bar: SeekBar?) {
                CoroutineScope(Dispatchers.Main).launch {
                    post(Servo(mapToMinMax(bar!!.progress, constants.servoCenter, constants.servoOffset)),
                        apiController.getService()::postServo as KSuspendFunction1<HttpBody, PostResponse>
                    )
                    bar.progress = mapTo0100(constants.motorCenter, constants.motorCenter, constants.motorOffset)
                }
            }
        })
    }

    private fun mapToMinMax(value: Int, center: Int, offset: Int): Int {
        val min = center - offset
        val max = center + offset
        return value * (max - min) / 100 + min
    }

    private fun mapTo0100(value: Int, center: Int, offset:Int): Int {
        val min = center - offset
        val max = center + offset
        return ((value - min) * 100) / (max - min)
    }

    private fun post(body: HttpBody, apiCall: KSuspendFunction1<HttpBody, PostResponse>) {
        // update label
        if (body is Motor) {
            val speedLabel = findViewById<TextView>(R.id.speedText)
            speedLabel.text = getString(R.string.label_speed, body.motor.toString())
        } else if (body is Servo) {
            val servoLabel = findViewById<TextView>(R.id.servoText)
            servoLabel.text = getString(R.string.label_servo, body.servo.toString())
        }
        CoroutineScope(Dispatchers.Main).launch {
            println("doing api post on $url")

            try {
                apiCall(body)
            } catch(ex: HttpException) {
                println(apiController.handleCallError(ex))
                ex.message?.let { displayErrorMessage(it) }
            } catch(ex: UnknownHostException) {
                println(getString(R.string.err_unknown_host)  + ": " + ex.stackTrace)
                ex.message?.let { displayErrorMessage(it) }
            } catch(ex: Exception) {
                println(getString(R.string.err_general) + ": " + ex.stackTrace)
                ex.message?.let { displayErrorMessage(it) }
            }
        }
    }


    private suspend fun get(apiCall: KSuspendFunction0<HttpBody>): HttpBody {
        println("doing api get call on $url")

            return try {
                apiCall() as Constants
            } catch(ex: HttpException) {
                println(apiController.handleCallError(ex))
                throw ex
            } catch(ex: UnknownHostException) {
                println(getString(R.string.err_unknown_host) + ": " + ex.message)
                throw ex
            } catch(ex: Exception) {
                println(getString(R.string.err_general) + ": " + ex.stackTrace)
                throw ex
            }
    }
}