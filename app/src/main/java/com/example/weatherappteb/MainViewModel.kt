package com.example.weatherappteb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> get() = _weatherData

    private val _isKelvin = MutableLiveData<Boolean>()
    val isKelvin: LiveData<Boolean> get() = _isKelvin

    private val _selectedWindSpeedUnit = MutableLiveData<String>()
    val selectedWindSpeedUnit: LiveData<String> get() = _selectedWindSpeedUnit

    private val _selectedPressureUnit = MutableLiveData<String>()
    val selectedPressureUnit: LiveData<String> get() = _selectedPressureUnit

    init {
        _isKelvin.value = false
        _selectedWindSpeedUnit.value = "m/s"
        _selectedPressureUnit.value = "hPa"
    }

    fun setWeatherData(weatherResponse: WeatherResponse?) {
        _weatherData.value = weatherResponse
    }

    fun setKelvin(isKelvin: Boolean) {
        _isKelvin.value = isKelvin
    }

    fun setWindSpeedUnit(unit: String) {
        _selectedWindSpeedUnit.value = unit
    }

    fun setPressureUnit(unit: String) {
        _selectedPressureUnit.value = unit
    }
}
