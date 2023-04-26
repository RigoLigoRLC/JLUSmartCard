package cc.rigoligo.jlusmartcard.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object RequestWrapper {
    suspend fun makeJsonRequest(
        path: String,
        method: String,
        parameters: Map<String, String> = mapOf(),
        additionalHeader: Map<String, String> = mapOf(),
        port: Int = 8090,
    ) : Response<PartialResponse<JSONObject>> {
        return withContext(Dispatchers.IO) {
            val url = URL(NetworkConsts.urlPath(port, path))
            (url.openConnection() as? HttpURLConnection)?.run {
                requestMethod = method
                val paramStr = buildString {
                    parameters.forEach { (k, v) ->
                        append("$k=${URLEncoder.encode(v, StandardCharsets.UTF_8.toString())}&")
                    }
                }
                additionalHeader.forEach{ (k, v) ->
                    setRequestProperty(k ,v)
                }
                outputStream.write(paramStr.toByteArray())
                try {
                    val builder = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String? = reader.readLine()
                    while (line != null) {
                        builder.append(line)
                        line = reader.readLine()
                    }
                    return@withContext Response.Success(PartialResponse(headerFields, JSONObject(builder.toString())))
                } catch (e: Exception) {
                    return@withContext Response.Error(e)
                }
            }
            Response.Error(Exception("Cannot open HttpUrlConnection"))
        }
    }

    suspend fun makeImageRequest(
        path: String,
        method: String,
        parameters: Map<String, String> = mapOf(),
        additionalHeader: Map<String, String> = mapOf(),
        port: Int = 8090,
    ) : Response<PartialResponse<Bitmap>> {
        return withContext(Dispatchers.IO) {
            val url = URL(NetworkConsts.urlPath(port, path))
            (url.openConnection() as? HttpURLConnection)?.run {
                requestMethod = method
                val paramStr = buildString {
                    parameters.forEach { (k, v) ->
                        append("$k=${URLEncoder.encode(v, StandardCharsets.UTF_8.toString())}&")
                    }
                }
                additionalHeader.forEach{ (k, v) ->
                    setRequestProperty(k ,v)
                }
                // FIXME: Write only when writing is supported
                try {
                    outputStream.write(paramStr.toByteArray())
                } catch (_: Throwable) {
                    // Just do nothing
                }
                try {
                    return@withContext Response.Success(PartialResponse(headerFields, BitmapFactory.decodeStream(inputStream)))
                } catch (e: Exception) {
                    return@withContext Response.Error(e)
                }
            }
            Response.Error(Exception("Cannot open HttpUrlConnection"))
        }
    }
}