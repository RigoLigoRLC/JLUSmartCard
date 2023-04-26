package cc.rigoligo.jlusmartcard.network.requests

import android.util.Log
import cc.rigoligo.jlusmartcard.crypto.RequestCrypto
import cc.rigoligo.jlusmartcard.crypto.RequestSigner
import cc.rigoligo.jlusmartcard.network.*
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

suspend fun GetPaymentCode(request: RequestGetPaymentCode): ResponseGetPaymentCode {
    // Prepare a request
    val requestDataWrapped = mapOf(
        "synjones.pay.getbarcode" to mapOf(
            "account" to request.accountId,
            "acctype" to "###",
            "flag" to "00",
            "cardid" to "1"
        )
    )

    val requestBody = JSONObject(requestDataWrapped)

    // Value of "request"
    val req1 = requestBody.toString()
    val reqText = URLEncoder.encode(
        String(
            Base64.getEncoder().encode(
                RequestCrypto.encrypt(requestBody.toString().toByteArray())
            ), Charsets.UTF_8),
        Charsets.UTF_8.toString()
    )

    // Generate the "signature"
    var dataToSign = ""
    val params = mutableMapOf(
        "method" to "synjones.pay.getbarcode",
        "registerid" to "mobileby",
        "request" to reqText,
        "timestamp" to URLEncoder.encode(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                System.currentTimeMillis()
            ).toString(),
            Charsets.UTF_8.toString()
        ),
        "v" to "1.0",
    )
    // Sort keys and concatenate
    params.keys.toTypedArray().sortedArray().forEach {
        dataToSign += when (it) {
            "request", "timestamp" -> it + URLDecoder.decode(params[it], Charsets.UTF_8.toString())
            else -> it + params[it]
        }
    }
    val dataByteArray = dataToSign.toByteArray()

    // Value of "sign"
    val sign = URLEncoder.encode(
        Base64.getEncoder().encodeToString(RequestSigner.sign(dataByteArray)),
        Charsets.UTF_8.toString()
    )

    // Add other parts of params
    params.putAll(
        mapOf(
            "sourcetype" to request.sessionKey,
            "versionName" to "1.2.7",
            "clientType" to "1",
            "sign" to sign,
            "imei" to request.deviceId,
            "versionCode" to "10207"
        )
    )

    // Make the ACTUAL request
    val result = RequestWrapper.makeJsonRequest(
        path = "/PhonePay/MobilePayCommon",
        method = "POST",
        port = 8070,
        parameters = params,
        additionalHeader = mapOf("app-ticket" to request.sessionKey)
    )

    return when (result) {
        is Response.Success<PartialResponse<JSONObject>> -> {
            val data = result.data.data

            val code = data.getString("retcode").toInt()
            return if (code != 0)
                ResponseGetPaymentCode(
                    ResponseStatus(code, data.getString("errmsg"), result.data.header),
                    listOf()
                )
            else {
                val barcodeArray = data.getJSONObject("obj").getJSONArray("BARCODE")
                val codeList = mutableListOf<String>()
                for (i in 0 until barcodeArray.length()) {
                    codeList.add(barcodeArray.getString(i))
                }
                ResponseGetPaymentCode(
                    ResponseStatus(
                        code,
                        data.getString("errmsg"),
                        result.data.header
                    ),
                    codeList
                )
            }
        }
        is Response.Error -> ResponseGetPaymentCode(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            ),
            listOf()
        )
    }
}