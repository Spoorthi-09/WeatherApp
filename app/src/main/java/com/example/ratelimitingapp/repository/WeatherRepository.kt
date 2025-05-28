package com.example.ratelimitingapp.repository

import com.example.ratelimitingapp.model.WeatherResponse
import com.example.ratelimitingapp.network.RetrofitInstance

class WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return RetrofitInstance.api.getWeatherByLocation(lat, lon, "hourly,daily", apiKey)
    }
}
