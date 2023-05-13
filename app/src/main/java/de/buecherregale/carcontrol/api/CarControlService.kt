package de.buecherregale.carcontrol.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CarControlService {
    @POST("car_control/motor")
    suspend fun postMotor(@Body motor: Motor) : PostResponse
    @POST("car_control/servo")
    suspend fun postServo(@Body servo: Servo) : PostResponse

    @GET("car_control/constants")
    suspend fun getConstants() : Constants

    // no get methods for servo and speed exist
}