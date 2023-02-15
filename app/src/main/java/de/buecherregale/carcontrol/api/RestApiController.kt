package de.buecherregale.carcontrol.api

import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestApiController(private var url: String) {

    private lateinit var retrofit: Retrofit
    private lateinit var service: CarControlService

    private var flagUrlChanged = true

    companion object {
        fun buildURL(ip: String, port: Int): String {
            return "http://$ip:$port"
        }
    }

    private fun getRetrofit(): Retrofit {
        if(!flagUrlChanged) return retrofit

        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }

    fun getService() : CarControlService {
        if(!flagUrlChanged) return service

        service = getRetrofit().create(CarControlService::class.java)
        return service
    }

    fun handleCallError(ex: HttpException): String {
        val message = ex.message
        return when (val code = ex.code()) {
            400 -> "$code: bad request: $message"
            404 -> "$code: not found: $message"
            500 -> "$code: internal server error: $message"
            else -> "$code: unknown http return: $message"
        }
    }

    fun setUrl(url: String) {
        this.url = url
        flagUrlChanged = true
    }
}