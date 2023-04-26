package cc.rigoligo.jlusmartcard.network.requests

import cc.rigoligo.jlusmartcard.network.*
import org.json.JSONObject
import kotlin.math.log

suspend fun GetRsaPublicKey(request: RequestGetRsaPublicKey): ResponseGetRsaPublicKey {
    val result = RequestWrapper.makeJsonRequest(
        path = "/Common/GetRsaKey",
        method = "POST",
        parameters = mapOf(
            "json" to "true"
        ),
        additionalHeader = mapOf(
            "Referer" to "http://202.198.17.52:8090/Phone/Login",
            "Cookie" to "ASP.NET_SessionId=${request.loginSessionCookie}; " +
                    "sourcetypeticket=0; " +
                    "imeiticket=${request.deviceId}"
        )
    )

    return when (result) {
        is Response.Success<PartialResponse<JSONObject>> -> {
            val data = result.data.data
            val splitObj = data.getString("Obj").split(',')
            val loginSessionKey = data.getString("Msg")

            ResponseGetRsaPublicKey(
                ResponseStatus(0, "", result.data.header),
                publicExponent = splitObj[0],
                modulo = splitObj[1],
                loginSessionKey = loginSessionKey
            )
        }
        is Response.Error -> ResponseGetRsaPublicKey(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            ),
            null, null, null
        )
    }
}