package com.example.weather

import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
data class WeatherInfo(
    val condition: String
)


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var CITY :String

    val API: String = "eec950893eb376eefefcefc4423217c7"
    private fun updateWeatherImage(weatherCondition: String) {
        val drawableResId = when (weatherCondition) {
            "Clear" -> R.drawable.sunny // Имя ресурса для солнечной погоды
            "Storm" -> R.drawable.storm // Имя ресурса для грозы
            "Snow" -> R.drawable.snowy // Имя ресурса для снегопада
            "Rain" -> R.drawable.rainy // Имя ресурса для дождя
            "Clouds" -> R.drawable.cloudy // Имя ресурса для облачной погоды
            else -> R.drawable.sunny // Имя ресурса по умолчанию или обработка неизвестной погоды
        }
        binding.ww.setImageResource(drawableResId)
    }
    private fun updateWeatherBackground(weatherCondition: String) {
        val drawableResId = when (weatherCondition) {
            "Clear" -> R.drawable.sunny_bg // Имя ресурса для ясной погоды
            "Storm" -> R.drawable.haze_bg // Имя ресурса для грозовой погоды
            "Snow" -> R.drawable.snow_bg // Имя ресурса для снегопада
            "Rain" -> R.drawable.rainy_bg // Имя ресурса для дождливой погоды
            "Clouds" -> R.drawable.haze_bg // Имя ресурса для облачной погоды
            else -> R.drawable.sunny_bg // Имя ресурса по умолчанию или обработка неизвестной погоды
        }
        binding.imageView.setImageResource(drawableResId)
        Log.d("WeatherApp", "Weather condition: $weatherCondition")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        CITY = binding.cityTxt.text.toString()

        // Выполняем задачу по получению погоды при запуске приложения
        weatherTask().execute()

        // Обработчик нажатия на кнопку
        binding.addCity.setOnClickListener {
            CITY = binding.cityTxt.text.toString()

            weatherTask().execute()
        }
    }

    private fun updateUI(result: String?) {
        try {
            val jsonObj = JSONObject(result)

            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val updatedAt: Long = jsonObj.getLong("dt")
            val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
            val temp = main.getDouble("temp").roundToInt().toString() + "°C"
            val tempMin = main.getDouble("temp_min").roundToInt().toString() + "°C"
            val tempMax = main.getDouble("temp_max").roundToInt().toString() + "°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")

            val weatherDescription = weather.getString("main")
            var address = jsonObj.getString("name") + ", " + sys.getString("country")

            // Обновляем текстовые поля
            binding.cityTxt.setText(address)

            //binding.updatedAt.text = updatedAtText
            binding.statusTxt.text = weatherDescription.capitalize(Locale.ROOT)
            binding.currentTempTxt.text = temp
            binding.minTempTxt.text = tempMin
            binding.maxTempTxt.text = tempMax
            binding.sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            binding.sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            binding.windTxt.text = windSpeed
            binding.pressure.text = pressure
            binding.humidityTxt.text = humidity
            updateWeatherImage(weatherDescription)
            updateWeatherBackground(weatherDescription)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("WeatherApp", "Error updating UI: ${e.message}")
        }
    }

    inner class weatherTask : AsyncTask<String, Void, String?>() {
        override fun doInBackground(vararg params: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WeatherApp", "Error fetching weather data: ${e.message}")
                null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                updateWeatherImage(result)
                updateUI(result)
            } else {
                Log.e("WeatherApp", "Result is null")
            }
        }
    }
}
