package com.example.weatherappteb

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    // MutableLiveData for Dark Mode
    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> get() = _isDarkMode
    // MutableLiveData for Temperature Unit
    private val _isKelvin = MutableLiveData<Boolean>()
    val isKelvin: LiveData<Boolean> get() = _isKelvin

    // MutableLiveData for Wind Speed Unit
    private val _selectedWindSpeedUnit = MutableLiveData<String>()
    val selectedWindSpeedUnit: LiveData<String> get() = _selectedWindSpeedUnit

    // MutableLiveData for Pressure Unit
    private val _selectedPressureUnit = MutableLiveData<String>()
    val selectedPressureUnit: LiveData<String> get() = _selectedPressureUnit

    init {
        _isDarkMode.value = preferences.getBoolean("dark_mode", false)
        _isKelvin.value = preferences.getBoolean("temperature_unit_kelvin", false)
        _selectedWindSpeedUnit.value = "m/s"  // Varsayılan birim m/s
        _selectedPressureUnit.value = "hPa"   // Varsayılan basınç birimi hPa
    }

    // Function to set Dark Mode state
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        // Save the value in SharedPreferences
        preferences.edit().putBoolean("dark_mode", isDark).apply()
    }

    // Function to set the Temperature Unit
    fun setTemperatureUnit(isKelvin: Boolean) {
        _isKelvin.value = isKelvin
        // Sıcaklık birimini SharedPreferences'te sakla
        preferences.edit().putBoolean("temperature_unit_kelvin", isKelvin).apply()
    }

    // Function to set the selected wind speed unit
    fun setWindSpeedUnit(unit: String) {
        _selectedWindSpeedUnit.value = unit
    }

    // Function to set the selected pressure unit
    fun setPressureUnit(unit: String) {
        _selectedPressureUnit.value = unit
    }
}
