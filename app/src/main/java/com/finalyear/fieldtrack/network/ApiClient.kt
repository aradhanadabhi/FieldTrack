package com.finalyear.fieldtrack.network

import com.finalyear.fieldtrack.model.Visit
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Demonstrates "Calling a Web-Service" + "Plus JSON encoding and decoding".
 *
 * Replace BASE_URL with your own backend. For testing without a real backend,
 * https://jsonplaceholder.typicode.com/posts accepts POST requests and echoes
 * back a fake created object - useful to prove the round trip works end to end.
 */
object ApiClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/posts"

    /**
     * Encodes a Visit into JSON and POSTs it to the server.
     * Returns true if the server responded with success (2xx).
     */
    fun pushVisit(visit: Visit): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(BASE_URL)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            // ---- JSON ENCODING ----
            val json = JSONObject().apply {
                put("siteName", visit.siteName)
                put("notes", visit.notes)
                put("latitude", visit.latitude)
                put("longitude", visit.longitude)
                put("timestamp", visit.timestamp)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(json.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                // ---- JSON DECODING ----
                val response = BufferedReader(InputStreamReader(connection.inputStream))
                    .use { it.readText() }
                val responseJson = JSONObject(response)
                // e.g. read back an id the server assigned
                responseJson.optInt("id", -1)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection?.disconnect()
        }
    }
}
