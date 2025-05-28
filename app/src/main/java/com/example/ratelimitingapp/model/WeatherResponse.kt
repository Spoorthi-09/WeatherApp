package com.example.ratelimitingapp.model

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: Current
)

data class Current(
    val temp: Double,
    val weather: List<WeatherDescription>
)

data class WeatherDescription(
    val description: String,
    val icon: String
)


