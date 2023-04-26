package cc.rigoligo.jlusmartcard.network.requests

import cc.rigoligo.jlusmartcard.network.*
import org.json.JSONObject

suspend fun LoginRequest(request: RequestLoginRequest): ResponseLoginRequest {
    val result = RequestWrapper.makeJsonRequest(
        path = "/Phone/Login",
        method = "POST",
        parameters = mapOf(
            "sno" to request.cardNumber,
            "pwd" to request.encryptedPassword,
            "remember" to "1",
            "uclass" to "1",
            "yzm" to request.verificationCode,
            "zqcode" to "",
            "key" to request.loginSessionKey,
            "json" to "true"
        ),
        additionalHeader = mapOf(
            "Referer" to "http://202.198.17.52:8090/Phone/Login",
            "Cookie" to "ASP.NET_SessionId=${request.loginSessionCookie}; " +
                    "username=${request.cardNumber}; " +
                    "sourcetypeticket=0; " +
                    "imeiticket=${request.deviceId}"
        ),
    )


    return when (result) {
        is Response.Success<PartialResponse<JSONObject>> -> {
            val data = result.data.data

            return if (!data.getBoolean("IsSucceed"))
                ResponseLoginRequest(
                    ResponseStatus(1, data.getString("Msg"), result.data.header), null
                )
            else {
                ResponseLoginRequest(
                    ResponseStatus(
                        if (data.getBoolean("IsSucceed")) 0 else 1,
                        data.getString("Msg"),
                        result.data.header
                    ),
                    data.getJSONObject("Obj2").getString("RescouseType")
                )
            }
        }
        is Response.Error -> ResponseLoginRequest(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            ),
            null
        )
    }
}