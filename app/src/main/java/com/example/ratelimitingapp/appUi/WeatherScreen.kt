package com.example.ratelimitingapp.appUi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ratelimitingapp.viewmodel.WeatherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    apiKey: String = "5df3bed1d8f7b5822959a89b0e9430ee"
) {
    val cameraPosition = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(33.44, -94.04), 4f)
    }

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Toast context
    val context = LocalContext.current

    // Observe error message for rate limit or API errors
    val errorMessage = viewModel.errorMessage

    // Show toast when errorMessage updates
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f),
            cameraPositionState = cameraPosition,
            onMapClick = { latLng ->
                selectedLatLng = latLng
                viewModel.fetchWeather(
                    lat = latLng.latitude,
                    lon = latLng.longitude,
                    apiKey = apiKey
                )
            }
        ) {
            // Show a marker at the selected location
            selectedLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Selected Location"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.weatherData?.let {
            Column {
                Text("Timezone: ${it.timezone}")
                Text("Temperature: ${it.current.temp}°C")
                Text("Condition: ${it.current.weather.firstOrNull()?.description ?: "N/A"}")
            }
        }

        viewModel.errorMessage?.let {
            Text("Error: $it", color = Color.Red)
        }
    }
}
