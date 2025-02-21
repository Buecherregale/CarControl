package de.buecherregale.carcontrol.api

interface HttpBody

data class Constants(val motorCenter: Int, val motorOffset: Int, val servoCenter: Int, val servoOffset: Int): HttpBody {
    /* gson (and other parsing libraries like jackson) create instances via reflection and set fields
       via reflection. this means the code below (as well as init{} blocks) are not executed with
       the values from the constructor.
       see GsonBuilder().disableJdkUnsafe()
       Solutions: Custom TypeAdapter (gson) might fix this
                  Use calculated properties
     */
    val motorMin get() = motorCenter - motorOffset
    val motorMax get() = motorCenter + motorOffset

    val servoMin get() = servoCenter - servoOffset
    val servoMax get() = servoCenter + servoOffset
}
data class Motor(val motor: Int) : HttpBody
data class Servo(val servo: Int) : HttpBody
data class PostResponse(val message: String) : HttpBody
