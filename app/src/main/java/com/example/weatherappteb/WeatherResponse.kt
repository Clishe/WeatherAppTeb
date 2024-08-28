package com.example.weatherappteb
data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val name: String,
    val sys : Sys,
    val timezone : Int
)

data class Coord(val lon: Double, val lat: Double)

data class Weather(val id: Int, val main: String, val description: String, val icon: String)

data class Main(val temp: Double, val feels_like: Double, val pressure: Int, val humidity: Int, val sea_level: Int,
                val grnd_Level: Int,val temp_min: Double, val temp_max: Double)

data class Wind(val speed: Double, val deg: Int)

data class Sys(val country: String, val sunrise: Int, val sunset: Int)


//{"coord":{"lon":28.9833,"lat":41.0351},
// "weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],
// "base":"stations",
// "main":{"temp":301.83,"feels_like":302.87,"temp_min":301.24,"temp_max":301.83,"pressure":1015,"humidity":54,"sea_level":1015,"grnd_level":1007},
// "visibility":10000,"wind":{"speed":6.69,"deg":60},"clouds":{"all":0},"dt":1723705798,"sys":{"type":1,"id":6970,"country":"TR","sunrise":1723691644,"sunset":1723741392},
// "timezone":10800,"id":745042,"name":"Istanbul","cod":200}