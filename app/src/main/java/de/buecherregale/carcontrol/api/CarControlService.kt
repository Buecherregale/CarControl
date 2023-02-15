package de.buecherregale.carcontrol.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CarControlService {
    @POST("drive/speed")
    suspend fun postSpeed(@Body speed: Speed) : PostResponse
    @POST("drive/servo")
    suspend fun postServo(@Body servo: Servo) : PostResponse

    @GET("drive/speed")
    suspend fun getSpeed() : Speed
    @GET("drive/servo")
    suspend fun getServo() : Servo

    @GET("drive/constants")
    suspend fun getConstants() : Constants
}