package com.example.magnettimer

import androidx.appcompat.app.AppCompatActivity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.MagnetTimer.R
import org.json.JSONObject
import java.net.URL

class weather : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_on)

        weatherTask().execute()
    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?q=seoul,kr&units=metric&appid=d8ee502438d7fd1583880301d9fb582c").readText(
                        Charsets.UTF_8
                    )
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result == null) {
                // Handle the case where result is null
                return
            }

            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")

                val temp = "현재 날씨 : " + main.getString("temp") + "°C"

                Log.d("WeatherTemp", temp) // 로그 추가

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.weather).text = temp

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}
