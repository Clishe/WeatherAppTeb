package com.example.weatherappteb

import android.app.Activity
import android.content.Intent
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
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private lateinit var temperaturetv: TextView
    private lateinit var feelslike: TextView
    private lateinit var tempMinMax : TextView
    private lateinit var cityInput: EditText
    private lateinit var winddegree : TextView
    private lateinit var windDirectionArrow: ImageView
    private lateinit var description : TextView
    private lateinit var pressure : TextView
    private lateinit var humidity : TextView
    private lateinit var sunriseset : TextView
    private var isTextSizeLarge = false
    private var weatherdata: WeatherResponse? = null
    private var isKelvin = false
    private var selectedUnit: String= "m/s"
    private var selectedPressureUnit = "hPa"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        winddegree = findViewById(R.id.textViewWind)
        temperaturetv = findViewById(R.id.textViewTemperature)
        feelslike  = findViewById(R.id.textViewFeelsLike)
        tempMinMax = findViewById(R.id.textViewMinMaxTemp)
        cityInput = findViewById(R.id.plain_text_input)
        windDirectionArrow = findViewById(R.id.windDirectionArrow)
        description = findViewById(R.id.textViewDescription)
        pressure = findViewById(R.id.textViewPressure)
        humidity = findViewById(R.id.textViewHumidity)
        sunriseset = findViewById(R.id.textViewWeather)
        val imageView: ImageView = findViewById(R.id.yourImageViewId)

        val buttonSettings: ImageButton = findViewById(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, 100)
        }



        val placeholderUrl = "https://pbs.twimg.com/media/GMUm51KXoAAEVDV.jpg"
        Glide.with(this)
            .load(placeholderUrl)
            .into(imageView)


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

        cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val cityName = cityInput.text.toString()
                fetchWeatherData(cityName, "c06d7786b337375d98565ef307296059", imageView)
                true
            } else {
                false
            }
        }
    }

    private fun fetchWeatherData(cityName: String, apiKey: String, imageView: ImageView) {
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("weather", "API çağrısı başarısız oldu: ${e.message}")
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
                            winddegree.text = "Wind Degree: ${weatherdata?.wind?.deg}°\n Wind Speed: ${convertWindSpeed(weatherdata?.wind?.speed, selectedUnit)} $selectedUnit"
                            weatherdata?.wind?.deg?.let { windDegree ->
                                windDirectionArrow.rotation = windDegree.toFloat()
                            }
                            description.text = "Description: ${weatherdata?.weather?.get(0)?.description}"
                            pressure.text = "Pressure: ${convertPressure(weatherdata?.main?.pressure, selectedPressureUnit)} $selectedPressureUnit"

                            humidity.text = "Humidity : %${weatherdata?.main?.humidity}"

                            val sunriseTime = convertUnixToTime(weatherdata?.sys?.sunrise?.toLong() ?: 0L, weatherdata?.timezone ?: 0)
                            val sunsetTime = convertUnixToTime(weatherdata?.sys?.sunset?.toLong() ?: 0L, weatherdata?.timezone ?: 0)
                            sunriseset.text = "Sunrise: $sunriseTime\nSunset: $sunsetTime"

                            val weatherIconView: ImageView = findViewById(R.id.weatherIconView)

                            val isDay = isDaylight(
                                weatherdata?.sys?.sunrise?.toLong() ?: 0L,
                                weatherdata?.sys?.sunset?.toLong() ?: 0L,
                                weatherdata?.timezone ?: 0
                            )

                            val weatherIcon = getWeatherIcon(weatherdata?.weather?.get(0)?.description ?: "", isDay)
                            weatherIconView.setImageResource(weatherIcon)

                            val imageResource: Int


                            if (isDay) {
                                imageResource = R.drawable.day_background
                            } else {
                                imageResource = R.drawable.night_background
                            }


                            imageView.setImageResource(imageResource)
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.d("weather", "API çağrısı başarılı değil: ${response.code}")
                    }
                }
            }
        })
    }

    private fun toggleTextSize() {
        val newSize = if (isTextSizeLarge) 22f else 24f
        val rootView = findViewById<FrameLayout>(R.id.frameLayoutMain) // Ana layout
        adjustTextSizeInViewGroup(rootView, newSize)
        isTextSizeLarge = !isTextSizeLarge
    }


    private fun adjustTextSizeInViewGroup(viewGroup: ViewGroup, size: Float) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            } else if (child is ViewGroup) {
                adjustTextSizeInViewGroup(child, size)
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
        val temperatureKelvin = weatherdata?.main?.temp
        val feelsLikeKelvin = weatherdata?.main?.feels_like
        val tempMinKelvin = weatherdata?.main?.temp_min
        val tempMaxKelvin = weatherdata?.main?.temp_max

        if (isKelvin) {
            temperaturetv.text = "Temperature: $temperatureKelvin K"
            feelslike.text = "Feels Like: $feelsLikeKelvin K"
            tempMinMax.text = "Min Temp: $tempMinKelvin K\nMax Temp: $tempMaxKelvin K"
        } else {
            temperaturetv.text = "Temperature: ${convertKelvinToCelsius(temperatureKelvin)} °C"
            feelslike.text = "Feels Like: ${convertKelvinToCelsius(feelsLikeKelvin)} °C"
            tempMinMax.text = "Min Temp: ${convertKelvinToCelsius(tempMinKelvin)} °C\nMax Temp: ${convertKelvinToCelsius(tempMaxKelvin)} °C"
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            isKelvin = data?.getBooleanExtra("isKelvin", false) ?: false
            val selectedWindSpeedUnit = data?.getStringExtra("selectedWindSpeedUnit") ?: "m/s"
            val selectedPressureUnit = data?.getStringExtra("selectedPressureUnit") ?: "hPa"

            updateTemperatureDisplay()
            updateWindSpeedDisplay(selectedWindSpeedUnit)
            updatePressureDisplay(selectedPressureUnit)
        }
    }
    private fun updateWindSpeedDisplay(unit: String) {
        val windSpeed = weatherdata?.wind?.speed
        winddegree.text = "Wind Speed: ${convertWindSpeed(windSpeed, unit)} $unit"
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
        val pressureValue = weatherdata?.main?.pressure
        pressure.text = "Pressure: ${convertPressure(pressureValue, unit)} $unit"
    }

    private fun convertMetersPerSecondToMilesPerHour(speedInMetersPerSecond: Double?): String {
        val mph = speedInMetersPerSecond?.times(2.237) ?: 0.0
        return String.format("%.2f", mph)
    }

    private fun convertMetersPerSecondToKnots(speedInMetersPerSecond: Double?): String {
        val knots = speedInMetersPerSecond?.times(1.944) ?: 0.0
        return String.format("%.2f", knots)
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
