package com.example.weatherappteb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.weatherappteb.base.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.util.TimeZone

class MainActivity : BaseActivity() {
    private lateinit var temperaturetv: TextView
    private lateinit var feelslike: TextView
    private lateinit var tempMinMax : TextView
    private lateinit var cityInput: TextInputEditText
    private lateinit var winddegree : TextView
    private lateinit var windDirectionArrow: ImageView
    private lateinit var description : TextView
    private lateinit var pressure : TextView
    private lateinit var humidity : TextView
    private lateinit var sunriseset : TextView
    private lateinit var weatherIconView: ImageView
    private var isTextSizeLarge = false
    private var weatherdata: WeatherResponse? = null
    private var isKelvin = false
    private var selectedUnit: String= "m/s"
    private var selectedPressureUnit = "hPa"
    private val weatherViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        applyDarkMode()
        initViews()


        val rootView = findViewById<ViewGroup>(R.id.frameLayoutMain)
        setTextSizeInViewGroup(rootView, 26f)
        // Observe ViewModel data changes and update the UI accordingly
        weatherViewModel.weatherData.observe(this, Observer { weatherData ->
            weatherData?.let { updateWeatherUI(it) }
        })

        weatherViewModel.isKelvin.observe(this, Observer { isKelvin ->
            updateTemperatureDisplay()
        })

        weatherViewModel.selectedWindSpeedUnit.observe(this, Observer { windUnit ->
            updateWindSpeedDisplay(windUnit)
        })

        weatherViewModel.selectedPressureUnit.observe(this, Observer { pressureUnit ->
            updatePressureDisplay(pressureUnit)
        })

        // Handle city input to fetch data
        cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val cityName = cityInput.text.toString()
                fetchWeatherData(cityName, "c06d7786b337375d98565ef307296059")
                true
            } else {
                false
            }
        }

        // Button to open settings
        val buttonSettings: ImageButton = findViewById(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, 100)

            // Apply the animation when starting SettingsActivity
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }


    private fun applyDarkMode() {
        val settingsPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = settingsPreferences.getBoolean("isDarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun initViews() {
        winddegree = findViewById(R.id.textViewWind)
        temperaturetv = findViewById(R.id.textViewTemperature)
        feelslike = findViewById(R.id.textViewFeelsLike)
        tempMinMax = findViewById(R.id.textViewMinMaxTemp)
        cityInput = findViewById(R.id.plain_text_input)
        windDirectionArrow = findViewById(R.id.windDirectionArrow)
        description = findViewById(R.id.textViewDescription)
        pressure = findViewById(R.id.textViewPressure)
        humidity = findViewById(R.id.textViewHumidity)
        sunriseset = findViewById(R.id.textViewWeather)
        weatherIconView = findViewById(R.id.weatherIconView)
    }

    private fun fetchWeatherData(cityName: String, apiKey: String) {
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    responseData?.let {
                        val weatherResponse = Gson().fromJson(responseData, WeatherResponse::class.java)
                        runOnUiThread {
                            weatherViewModel.setWeatherData(weatherResponse) // Save weather data in ViewModel
                        }
                    }
                }
            }
        })
    }

    private fun updateWeatherUI(weatherData: WeatherResponse) {
        temperaturetv.text = "Temperature: ${convertKelvinToCelsius(weatherData.main.temp)} °C"
        feelslike.text = "Feels Like: ${convertKelvinToCelsius(weatherData.main.feels_like)} °C"
        tempMinMax.text = "Min Temp: ${convertKelvinToCelsius(weatherData.main.temp_min)}°C\nMax Temp: ${
            convertKelvinToCelsius(weatherData.main.temp_max)}°C"
        winddegree.text = "Wind Degree: ${weatherData.wind.deg}°\n Wind Speed: ${convertWindSpeed(weatherData.wind.speed, weatherViewModel.selectedWindSpeedUnit.value ?: "m/s")} ${weatherViewModel.selectedWindSpeedUnit.value}"
        windDirectionArrow.rotation = weatherData.wind.deg.toFloat()
        description.text = "Description: ${weatherData.weather[0].description}"
        pressure.text = "Pressure: ${convertPressure(weatherData.main.pressure, weatherViewModel.selectedPressureUnit.value ?: "hPa")} ${weatherViewModel.selectedPressureUnit.value}"
        humidity.text = "Humidity: %${weatherData.main.humidity}"

        val sunriseTime = convertUnixToTime(weatherData.sys.sunrise.toLong(), weatherData.timezone)
        val sunsetTime = convertUnixToTime(weatherData.sys.sunset.toLong(), weatherData.timezone)
        sunriseset.text = "Sunrise: $sunriseTime\nSunset: $sunsetTime"

        // Set the weather icon
        val isDay = isDaylight(
            weatherData.sys.sunrise.toLong(),
            weatherData.sys.sunset.toLong(),
            weatherData.timezone
        )
        val weatherIcon = getWeatherIcon(weatherData.weather[0].description, isDay)
        weatherIconView.setImageResource(weatherIcon)
    }

    private fun setTextSizeInViewGroup(viewGroup: ViewGroup, size: Float) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            } else if (child is ViewGroup) {
                setTextSizeInViewGroup(child, size)
            }
        }
    }


    private fun convertTimezoneToUTCFormat(timezoneInSeconds: Int): String {
        // Timezone saniye cinsinden geliyor, saat cinsine çevirmek için 3600'e bölüyoruz
        val timezoneInHours = timezoneInSeconds / 3600

        // UTC formatında göstermek için pozitif ya da negatif işaret kontrolü yapıyoruz
        return if (timezoneInHours >= 0) {
            "UTC+$timezoneInHours"
        } else {
            "UTC$timezoneInHours"
        }
    }

    private fun isDaylight(sunrise: Long, sunset: Long, timezoneOffsetInSeconds: Int): Boolean {

        val utcNow = System.currentTimeMillis()

        val cityTimeMillis = utcNow + (timezoneOffsetInSeconds * 1000L)

        // Gün doğumu ve gün batımı zamanlarını şehir saat dilimine dönüştürüyoruz
        val sunriseMillis = (sunrise + timezoneOffsetInSeconds) * 1000L
        val sunsetMillis = (sunset + timezoneOffsetInSeconds) * 1000L

        // Loglar ile bilgileri kontrol edin
        Log.d("DaylightCheck", "Current City Time: ${Date(cityTimeMillis)}")
        Log.d("DaylightCheck", "Sunrise (City Time): ${Date(sunriseMillis)}")
        Log.d("DaylightCheck", "Sunset (City Time): ${Date(sunsetMillis)}")

        // Şu anki zamanın gün ışığında olup olmadığını kontrol ediyoruz
        return cityTimeMillis in sunriseMillis..sunsetMillis
    }

    private fun getWeatherIcon(description: String, isDay: Boolean): Int {
        return when (description) {
            "clear sky" -> if (isDay) R.drawable.clear_sky_day_icon else R.drawable.clear_sky_night_icon
            "few clouds" -> if (isDay) R.drawable.few_clouds_day_icon else R.drawable.few_clouds_night_icon
            "scattered clouds" -> if (isDay) R.drawable.scattered_clouds_dn_icon else R.drawable.scattered_clouds_dn_icon
            "broken clouds" -> if(isDay) R.drawable.broken_clouds_dn_icon else R.drawable.broken_clouds_dn_icon
            "overcast clouds"-> if(isDay) R.drawable.broken_clouds_dn_icon else R.drawable.broken_clouds_dn_icon
            //Sis
            "mist"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "smoke"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "haze"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "sand/dust whirls"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "fog"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "sand"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "dust"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "volcanic ash"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "squalls"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            "tornado"-> if(isDay) R.drawable.mist_dn_icon else R.drawable.mist_dn_icon
            //Gar
            "light snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "heavy snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "sleet"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "light shower sleet"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "shower sleet"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "light rain and snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "rain and snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "light shower snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "shower snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "heavy shower snow"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            //Yağmur
            "light rain"-> if(isDay) R.drawable.rain_day_icon else R.drawable.rain_night_icon
            "moderate rain"-> if(isDay) R.drawable.rain_day_icon else R.drawable.rain_night_icon
            "heavy intensity rain"-> if(isDay) R.drawable.rain_day_icon else R.drawable.rain_night_icon
            "very heavy rain"-> if(isDay) R.drawable.rain_day_icon else R.drawable.rain_night_icon
            "extreme rain"-> if(isDay) R.drawable.rain_day_icon else R.drawable.rain_night_icon
            "freezing rain"-> if(isDay) R.drawable.snow_dn_icon else R.drawable.snow_dn_icon
            "light intensity shower rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "shower rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "heavy intensity shower rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "ragged shower rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            //Çiseleme
            "light intensity drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "heavy intensity drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "light intensity drizzle rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "drizzle rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "heavy intensity drizzle rain"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "shower rain and drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "heavy shower rain and drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            "shower drizzle"-> if(isDay) R.drawable.shower_rain_dn_icon else R.drawable.shower_rain_dn_icon
            //Şimşek
            "thunderstorm with light rain"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm with rain"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm with heavy rain"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "light thunderstorm"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "heavy thunderstorm"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "ragged thunderstorm"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm with light drizzle"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm with drizzle"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            "thunderstorm with heavy drizzle"-> if(isDay) R.drawable.thunderstorm_dn_icon else R.drawable.thunderstorm_dn_icon
            else -> R.drawable.yukari_ok
        }
    }

    private fun convertUnixToTime(unixTime: Long, timezoneOffsetInSeconds: Int): String {

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = unixTime * 1000L // Unix zamanını milisaniyeye çeviriyoruz

        val timezoneOffsetMillis = timezoneOffsetInSeconds * 1000L
        calendar.add(Calendar.MILLISECOND, timezoneOffsetMillis.toInt())

        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.timeZone = calendar.timeZone

        return sdf.format(calendar.time)
    }

    private fun updateTemperatureDisplay() {
        weatherViewModel.weatherData.value?.let { weatherData ->
            val tempKelvin = weatherData.main.temp
            val tempCelsius = convertKelvinToCelsius(tempKelvin)
            temperaturetv.text = if (weatherViewModel.isKelvin.value == true) {
                "Temperature: $tempKelvin K"
            } else {
                "Temperature: $tempCelsius °C"
            }
        }
    }

    private fun updateWindSpeedDisplay(unit: String) {
        weatherViewModel.weatherData.value?.let { weatherData ->
            winddegree.text = "Wind Speed: ${convertWindSpeed(weatherData.wind.speed, unit)} $unit"
        }
    }


    private fun convertPressure(pressureInHpa: Int?, unit: String): String {
        return when (unit) {
            "mbar" -> pressureInHpa?.toString() ?: ""
            "mmHg" -> pressureInHpa?.let { String.format("%.2f", it * 0.750062) } ?: ""
            "inHg" -> pressureInHpa?.let { String.format("%.2f", it * 0.02953) } ?: ""
            "atm" -> pressureInHpa?.let { String.format("%.4f", it / 1013.25) } ?: ""
            else -> pressureInHpa?.toString() ?: ""
        }
    }

    private fun updatePressureDisplay(unit: String) {
        weatherViewModel.weatherData.value?.let { weatherData ->
            pressure.text = "Pressure: ${convertPressure(weatherData.main.pressure, unit)} $unit"
        }
    }

    private fun convertMetersPerSecondToMilesPerHour(speedInMetersPerSecond: Double?): String {
        val mph = speedInMetersPerSecond?.times(2.237) ?: 0.0
        return String.format("%.2f", mph)
    }

    private fun convertMetersPerSecondToKnots(speedInMetersPerSecond: Double?): String {
        val knots = speedInMetersPerSecond?.times(1.944) ?: 0.0
        return String.format("%.2f", knots)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Handle other changes like temperature unit, wind speed unit, pressure unit
            val isKelvin = data?.getBooleanExtra("isKelvin", false) ?: false
            val selectedWindSpeedUnit = data?.getStringExtra("selectedWindSpeedUnit") ?: "m/s"
            val selectedPressureUnit = data?.getStringExtra("selectedPressureUnit") ?: "hPa"

            weatherViewModel.setKelvin(isKelvin)
            weatherViewModel.setWindSpeedUnit(selectedWindSpeedUnit)
            weatherViewModel.setPressureUnit(selectedPressureUnit)
        }
    }

    private fun convertKelvinToCelsius(kelvin: Double?): Int {
        return kelvin?.minus(272.5)?.toInt() ?: 0}

    private fun convertWindSpeed(speedInMetersPerSecond: Double?, unit: String): String {
        val speed = when (unit) {
            "km/h" -> (speedInMetersPerSecond?.times(3.6) ?: 0.0)
            "mph" -> (speedInMetersPerSecond?.times(2.23694) ?: 0.0)
            "kn" -> (speedInMetersPerSecond?.times(1.94384) ?: 0.0)
            else -> speedInMetersPerSecond ?: 0.0 // Varsayılan birim m/s
        }
        return String.format("%.2f", speed)
    }
}
