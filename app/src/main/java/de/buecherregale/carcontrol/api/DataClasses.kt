package de.buecherregale.carcontrol.api

interface HttpBody

data class Constants(val speedCenter: Int, val speedOffset: Int, val servoCenter: Int, val servoOffset: Int): HttpBody
data class Speed(val speed: Int) : HttpBody
data class Servo(val servo: Int) : HttpBody
data class PostResponse(val message: String) : HttpBody
