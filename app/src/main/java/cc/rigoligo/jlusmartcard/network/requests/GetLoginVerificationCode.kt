package cc.rigoligo.jlusmartcard.network.requests

import android.graphics.Bitmap
import android.widget.Toast
import cc.rigoligo.jlusmartcard.network.*

suspend fun GetLoginVerificationCode(): ResponseGetLoginVerificationCode {
    val result = RequestWrapper.makeImageRequest(
        path = "/Phone/GetValidateCode",
        method = "GET",
        parameters = mapOf("time" to System.currentTimeMillis().toString())
    )

    return when (result) {
        is Response.Success<PartialResponse<Bitmap>> -> {
            val cookies: MutableList<String> = mutableListOf()
            var sessionCookie: String = ""
            result.data.header["Set-Cookie"]?.forEach { it ->
                it.split(" ").forEach { it2 ->
                    cookies.add(it2)
                }
            }
            cookies.forEach {
                if (it.startsWith("ASP.NET_SessionId")) {
                    sessionCookie = it.removePrefix("ASP.NET_SessionId=").removeSuffix(";")
                    return@forEach
                }
            }
            ResponseGetLoginVerificationCode(
                ResponseStatus(0, "", result.data.header),
                result.data.data,
                sessionCookie
            )
        }
        is Response.Error -> ResponseGetLoginVerificationCode(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            ),
            null, null
        )
    }
}