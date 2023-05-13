package de.buecherregale.carcontrol.api

interface HttpBody

data class Constants(val motorCenter: Int, val motorOffset: Int, val servoCenter: Int, val servoOffset: Int): HttpBody
data class Motor(val motor: Int) : HttpBody
data class Servo(val servo: Int) : HttpBody
data class PostResponse(val message: String) : HttpBody
