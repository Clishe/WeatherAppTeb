package com.example.weatherappteb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        val switchCompat: SwitchCompat = findViewById(R.id.switchTemperatureUnit)

        // Wind speed spinner
        val spinnerWindSpeed: Spinner = findViewById(R.id.spinnerWindSpeed)
        val windSpeedUnits = resources.getStringArray(R.array.wind_speed_units)
        var selectedWindSpeedUnit = "m/s"

        val windSpeedAdapter = ArrayAdapter(this, R.layout.spinner_item, windSpeedUnits)
        windSpeedAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerWindSpeed.adapter = windSpeedAdapter

        spinnerWindSpeed.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedWindSpeedUnit = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Pressure unit spinner
        val spinnerPressureUnit: Spinner = findViewById(R.id.spinnerPressureUnit)
        val pressureUnits = resources.getStringArray(R.array.pressure_units)
        var selectedPressureUnit = "hPa"

        val pressureAdapter = ArrayAdapter(this, R.layout.spinner_item, pressureUnits)
        pressureAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerPressureUnit.adapter = pressureAdapter

        spinnerPressureUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedPressureUnit = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        buttonBack.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("isKelvin", switchCompat.isChecked)
            resultIntent.putExtra("selectedWindSpeedUnit", selectedWindSpeedUnit)
            resultIntent.putExtra("selectedPressureUnit", selectedPressureUnit)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}