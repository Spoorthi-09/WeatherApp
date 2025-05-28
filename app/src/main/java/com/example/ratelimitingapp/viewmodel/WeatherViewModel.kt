package com.example.ratelimitingapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ratelimitingapp.model.WeatherResponse
import com.example.ratelimitingapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    var weatherData by mutableStateOf<WeatherResponse?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Rate limiting
    private val requestTimestamps = mutableListOf<Long>()

    private fun isRateLimitExceeded(): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneMinuteAgo = currentTime - 60_000

        // Remove timestamps older than 1 minute
        requestTimestamps.removeAll { it < oneMinuteAgo }

        return requestTimestamps.size >= 5
    }

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        if (isRateLimitExceeded()) {
            errorMessage = "Limit reached. Please try again after a minute."
            return
        }

        requestTimestamps.add(System.currentTimeMillis())

        viewModelScope.launch {
            try {
                val result = repository.getWeather(lat, lon, apiKey)
                weatherData = result
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to fetch weather: ${e.message}"
                weatherData = null
            }
        }
    }
}
