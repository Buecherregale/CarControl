package de.buecherregale.carcontrol.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CarControlService {
    @POST("drive/motor/{value}")
    suspend fun postMotor(@Path("value") motor: Int) : PostResponse

    @POST("drive/servo/{value}")
    suspend fun postServo(@Path("value") servo: Int) : PostResponse

    @GET("drive/constants")
    suspend fun getConstants() : Constants

    @POST("drive/lka")
    suspend fun activateLKA() : Response<Unit>

    @GET("drive/speed")
    suspend fun getSpeed() : Int
    @GET("drive/servo")
    suspend fun getServo() : Int

}