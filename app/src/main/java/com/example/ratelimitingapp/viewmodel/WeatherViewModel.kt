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

    // Add a counter to force recomposition even if the message is the same
    var errorMessageCounter by mutableStateOf(0)
        private set

    //Caching
    private val weatherCache = mutableMapOf<Pair<Double, Double>, Pair<Long, WeatherResponse>>() // lat/lon -> (timestamp, data)
    private val cacheDuration = 5 * 60 * 1000L // 5 minutes in milliseconds

    private fun getRemainingTime(): Long {
        val currentTime = System.currentTimeMillis()
        val oneMinuteAgo = currentTime - 60_000

        // Remove timestamps older than 1 minute
        requestTimestamps.removeAll { it < oneMinuteAgo }

        if (requestTimestamps.size < 5) {
            return 0
        }

        val oldest = requestTimestamps.minOrNull() ?: currentTime
        val timeLeft = 60_000 - (currentTime - oldest)
        return timeLeft.coerceAtLeast(0) // Avoid negative times
    }

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        val currentTime = System.currentTimeMillis()
        val key = Pair(lat, lon)

        // Check cache
        val cached = weatherCache[key]
        if (cached != null && currentTime - cached.first <= cacheDuration) {
            weatherData = cached.second
            errorMessage = "Loaded from cache."
            errorMessageCounter++
            return
        }

        // Check rate limit
        val remainingTime = getRemainingTime()
        if (remainingTime > 0) {
            val secondsLeft = remainingTime / 1000
            errorMessage = "Rate limit exceeded. Try again in $secondsLeft seconds."
            errorMessageCounter++
            return
        }

        requestTimestamps.add(currentTime)

        viewModelScope.launch {
            try {
                val result = repository.getWeather(lat, lon, apiKey)
                weatherData = result
                weatherCache[key] = Pair(currentTime, result) // Save to cache
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to fetch weather: ${e.message}"
                errorMessageCounter++
                weatherData = null
            }
        }
    }

}