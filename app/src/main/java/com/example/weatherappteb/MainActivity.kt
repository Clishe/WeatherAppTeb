package com.example.weatherappteb

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import com.google.gson.Gson
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    private lateinit var temperaturetv: TextView
    private lateinit var feelslike: TextView
    private lateinit var tempMinMax : TextView
    private lateinit var cityInput: EditText
    private lateinit var winddegree : TextView
    private var isTextSizeLarge = false
    private var weatherdata: WeatherResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        winddegree = findViewById(R.id.textViewWind)
        temperaturetv = findViewById(R.id.textViewTemperature)
        feelslike  =  findViewById(R.id.textViewFeelsLike)
        tempMinMax = findViewById(R.id.textViewMinMaxTemp)
        cityInput = findViewById(R.id.plain_text_input)

        val imageView: ImageView = findViewById(R.id.yourImageViewId)

        val newButton = Button(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }
            text = "Yazı Boyutunu Değiştir"
        }

        val frameLayout = findViewById<FrameLayout>(R.id.frameLayoutMain)
        frameLayout.addView(newButton)

        newButton.setOnClickListener {
            toggleTextSize()
        }

        val imageUrl = "https://images.ctfassets.net/hrltx12pl8hq/6bTvduuBDRZTU4J0dfjoLS/180fba3c8b0f990da177f2f2654bc820/0_hero.webp?fit=fill&w=600&h=1200"
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        imageView.setOnClickListener {
            if (isTextSizeLarge) {
                // Eğer büyükse, eski boyutuna geri dön
                temperaturetv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f) // Eski boyut
                isTextSizeLarge = false
            } else {
                // Eğer küçükse, büyük yap
                temperaturetv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f) // Yeni büyük boyut
                isTextSizeLarge = true
            }
        }

        cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val cityName = cityInput.text.toString()
                fetchWeatherData(cityName, "a0c8211b644bec92e3c7908569c12242")
                true
            } else {
                false
            }
        }

        //burda
        temperaturetv.setOnClickListener {
        }
        var isCelsius = true

        temperaturetv.setOnClickListener {
            val temperature = weatherdata?.main?.temp
            val temperatureDifference = (temperature?.minus(272.5))?.toInt()
            if (isCelsius) {
            // Ekrandaki değeri Kelvin'e geri çevir
            val kelvinTemperature = temperatureDifference?.plus(272.5)
            temperaturetv.text = "Temperature Degree Kelvin: $kelvinTemperature"
            isCelsius = false
        } else {
            // Ekrandaki değeri tekrar Celsius olarak göster
            temperaturetv.text = "Temperature Degree : $temperatureDifference"
            isCelsius = true
        }  }



    }

    private fun fetchWeatherData(cityName: String, apiKey: String,) {
        val client = OkHttpClient()

        // API URL'sini oluştur
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

        // İstek objesini oluştur
        val request = Request.Builder()
            .url(url)
            .build()

        // API çağrısını asenkron yap
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {

                    Log.d("weather","API çağrısı başarısız oldu: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        weatherdata = Gson().fromJson(responseData, WeatherResponse::class.java)
                        runOnUiThread {
                            temperaturetv.text = "Temperature: ${convertKelvinToCelsius(weatherdata?.main?.temp)} °C"
                            feelslike.text = "Feels Like: ${convertKelvinToCelsius(weatherdata?.main?.feels_like)} °C"
                            tempMinMax.text = "Min Temp: ${convertKelvinToCelsius(weatherdata?.main?.temp_min)}°C\nMax Temp: ${
                                convertKelvinToCelsius(weatherdata?.main?.temp_max)}°C"
                            winddegree.text = "Wind Degree ${weatherdata?.wind?.deg}\n Wind Speed:${convertMetersPerSecondToKmPerHour(weatherdata?.wind?.speed)} "
                        }
                    //textView.text = "seaLevel: ${weatherdata.main.sea_level}\nAçıklama: ${weatherdata.weather.get(0).description}"

                    }
                } else {
                    runOnUiThread {
                        //textView.text = "API çağrısı başarılı değil: ${response.code}"
                    }
                }
            }
        })
    }
    private fun toggleTextSize() {
        val newSize = if (isTextSizeLarge) 16f else 24f
        val rootView = findViewById<FrameLayout>(R.id.frameLayoutMain) // Ana layout
        adjustTextSizeInViewGroup(rootView, newSize)
        isTextSizeLarge = !isTextSizeLarge
    }

    // ViewGroup içinde yer alan tüm TextView'lerin yazı boyutunu ayarlayan fonksiyon
    private fun adjustTextSizeInViewGroup(viewGroup: ViewGroup, size: Float) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            } else if (child is ViewGroup) {
                adjustTextSizeInViewGroup(child, size) // Eğer iç içe layout varsa
            }
        }
    }




    private fun convertKelvinToCelsius(kelvin: Double?): Int {
        return kelvin?.minus(272.5)?.toInt() ?: 0}
    private fun convertMetersPerSecondToKmPerHour(speedInMetersPerSecond: Double?): String {
        val kmPerHour = speedInMetersPerSecond?.times(3.6) ?: 0.0
        return String.format("%.2f", kmPerHour)
    }
}
