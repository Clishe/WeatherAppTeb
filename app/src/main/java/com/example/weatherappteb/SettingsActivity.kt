package com.example.weatherappteb
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
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
import com.example.weatherappteb.base.BaseActivity

class SettingsActivity : BaseActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private var currentThemeId: Int = 0
    private var currentTheme: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = getSharedPreferences("appTheme", MODE_PRIVATE)
        val themeName = themePreferences.getString("selectedTheme", "Theme.Default")
        setTheme(resources.getIdentifier(themeName, "style", packageName))
        setContentView(R.layout.activity_settings)

        val buttonThemeOne: ImageButton = findViewById(R.id.buttonThemeOne)
        val buttonThemeTwo: ImageButton = findViewById(R.id.buttonThemeTwo)
        val buttonThemeThree: ImageButton = findViewById(R.id.buttonThemeThree)
        val buttonThemeFour: ImageButton = findViewById(R.id.buttonThemeFour)

        // Initialize views
        val switchTemperatureUnit: SwitchCompat = findViewById(R.id.switchTemperatureUnit)
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        val switchTheme: SwitchCompat = findViewById(R.id.switchTheme)
        val spinnerWindSpeed: Spinner = findViewById(R.id.spinnerWindSpeed)
        val spinnerPressureUnit: Spinner = findViewById(R.id.spinnerPressureUnit)

        // Initialize SharedPreferences to persist settings
        val settingsPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = settingsPreferences.edit()

        // Set initial state for Dark Mode
        val isDarkMode = settingsPreferences.getBoolean("isDarkMode", false)
        switchTheme.isChecked = isDarkMode
        applyDarkMode(isDarkMode)

        // Set initial state for temperature unit
        val isKelvin = settingsPreferences.getBoolean("isKelvin", false)
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
        val savedWindSpeedUnit = settingsPreferences.getString("selectedWindSpeedUnit", windSpeedUnits[0])
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
        val savedPressureUnit = settingsPreferences.getString("selectedPressureUnit", pressureUnits[0])
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

            // Apply the animation when returning to MainActivity
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        buttonThemeOne.setOnClickListener {
            saveTheme("Theme.CustomOne")
        }

        buttonThemeTwo.setOnClickListener {
            saveTheme("Theme.CustomTwo")
        }

        buttonThemeThree.setOnClickListener {
            saveTheme("Theme.CustomThree")
        }

        buttonThemeFour.setOnClickListener {
            saveTheme("Theme.Default")
        }



    }
    private fun saveTheme(themeName: String) {
        val themePreferences = getSharedPreferences("appTheme", MODE_PRIVATE)
        themePreferences.edit()
            .putString("selectedTheme", themeName)
            .putBoolean("isThemeChanged", true)
            .apply()

        // Apply the animation immediately
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        // Use a handler to delay the recreate() call for smoother transition
        Handler().postDelayed({
            recreate()
        }, 100) // Delay by 100ms or adjust based on your needs
    }




    override fun onBackPressed() {
        super.onBackPressed()

        // Apply the animation when returning to MainActivity via the back button
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


}
