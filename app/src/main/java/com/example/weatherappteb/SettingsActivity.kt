package com.example.weatherappteb
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer

class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_settings)

        // Initialize views
        val switchTemperatureUnit: SwitchCompat = findViewById(R.id.switchTemperatureUnit)
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        val switchTheme: SwitchCompat = findViewById(R.id.switchTheme)
        val spinnerWindSpeed: Spinner = findViewById(R.id.spinnerWindSpeed)
        val spinnerPressureUnit: Spinner = findViewById(R.id.spinnerPressureUnit)

        // Initialize SharedPreferences to persist settings
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Set initial state for Dark Mode
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        switchTheme.isChecked = isDarkMode
        applyDarkMode(isDarkMode)

        // Set initial state for temperature unit
        val isKelvin = sharedPreferences.getBoolean("isKelvin", false)
        switchTemperatureUnit.isChecked = isKelvin

        // Set Dark Mode
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setDarkMode(isChecked)
            editor.putBoolean("isDarkMode", isChecked).apply()

            // Apply Dark/Light mode immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreate() // Restart activity to apply the mode change
        }

        // Handle Temperature Unit switch
        switchTemperatureUnit.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setTemperatureUnit(isChecked)
            editor.putBoolean("isKelvin", isChecked).apply()
        }

        // Set up spinner for wind speed units
        val windSpeedUnits = resources.getStringArray(R.array.wind_speed_units)
        val windSpeedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, windSpeedUnits)
        windSpeedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWindSpeed.adapter = windSpeedAdapter

        // Set initial spinner value from SharedPreferences
        val savedWindSpeedUnit = sharedPreferences.getString("selectedWindSpeedUnit", windSpeedUnits[0])
        val windSpeedPosition = windSpeedUnits.indexOf(savedWindSpeedUnit)
        spinnerWindSpeed.setSelection(windSpeedPosition)

        // Handle wind speed unit selection
        spinnerWindSpeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUnit = parent.getItemAtPosition(position).toString()
                settingsViewModel.setWindSpeedUnit(selectedUnit)
                editor.putString("selectedWindSpeedUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Set up spinner for pressure units
        val pressureUnits = resources.getStringArray(R.array.pressure_units)
        val pressureAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pressureUnits)
        pressureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPressureUnit.adapter = pressureAdapter

        // Set initial spinner value from SharedPreferences
        val savedPressureUnit = sharedPreferences.getString("selectedPressureUnit", pressureUnits[0])
        val pressurePosition = pressureUnits.indexOf(savedPressureUnit)
        spinnerPressureUnit.setSelection(pressurePosition)

        // Handle pressure unit selection
        spinnerPressureUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUnit = parent.getItemAtPosition(position).toString()
                settingsViewModel.setPressureUnit(selectedUnit)
                editor.putString("selectedPressureUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Back button logic to send the selected values back to the MainActivity
        buttonBack.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("isKelvin", switchTemperatureUnit.isChecked)
                putExtra("selectedWindSpeedUnit", settingsViewModel.selectedWindSpeedUnit.value)
                putExtra("selectedPressureUnit", settingsViewModel.selectedPressureUnit.value)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
