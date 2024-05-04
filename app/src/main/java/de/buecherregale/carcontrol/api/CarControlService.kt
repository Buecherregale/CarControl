package de.buecherregale.carcontrol.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CarControlService {
    @POST("car_control/motor/{value}")
    suspend fun postMotor(@Path("value") motor: Motor) : PostResponse
    @POST("car_control/servo")
    suspend fun postServo(@Path("value") servo: Servo) : PostResponse

    @GET("car_control/constants")
    suspend fun getConstants() : Constants

    @POST("car_control/lka")
    suspend fun activateLKA() : Response<Unit>

    // no get methods for servo and speed exist
}