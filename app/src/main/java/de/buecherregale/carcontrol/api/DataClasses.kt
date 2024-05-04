package de.buecherregale.carcontrol.api

interface HttpBody

data class Constants(val motorCenter: Int, val motorOffset: Int, val servoCenter: Int, val servoOffset: Int): HttpBody {
    val motorMin = motorCenter - motorOffset
    val motorMax = motorCenter + motorOffset

    val servoMin = servoCenter - servoOffset
    val servoMax = servoCenter + servoOffset
}
data class Motor(val motor: Int) : HttpBody
data class Servo(val servo: Int) : HttpBody
data class PostResponse(val message: String) : HttpBody
