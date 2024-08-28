package com.example.weatherappteb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // XML dosyasını yükleyin

        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        val switchCompat: SwitchCompat = findViewById(R.id.switchTemperatureUnit)

        buttonBack.setOnClickListener {
            // Seçili duruma göre sonuç döndür
            val resultIntent = Intent()
            resultIntent.putExtra("isKelvin", switchCompat.isChecked)
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
