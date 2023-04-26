package cc.rigoligo.jlusmartcard.network.requests

import cc.rigoligo.jlusmartcard.network.*
import org.json.JSONObject

suspend fun CheckSessionValidity(request: RequestCheckSessionValidity): ResponseCheckSessionValidity {
    val result = RequestWrapper.makeJsonRequest(
        path = "/NoBase/GetInfoByToken",
        method = "POST",
        parameters = mapOf(
            "sourcetype" to request.sessionKey,
            "versionName" to "1.2.7",
            "clientType" to "1",
            "imei" to request.deviceId,
            "versionCode" to "10207",
            "token" to request.sessionKey
        ),
        additionalHeader = mapOf(
            "app-ticket" to request.sessionKey
        )
    )

    return when (result) {
        is Response.Success<PartialResponse<JSONObject>> -> {
            val data = result.data.data

            return if (!data.getBoolean("IsSucceed"))
                ResponseCheckSessionValidity(
                    ResponseStatus(1, data.getString("Msg"), result.data.header), "", ""
                )
            else {
                val obj = data.getJSONObject("Obj")
                ResponseCheckSessionValidity(
                    ResponseStatus(
                        if (data.getBoolean("IsSucceed")) 0 else 1,
                        data.getString("Msg"),
                        result.data.header
                    ),
                    obj.getString("NAME"),
                    obj.getString("SNO"),
                    obj.getString("ACCOUNT")
                )
            }
        }
        is Response.Error -> ResponseCheckSessionValidity(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            )
        )
    }
}