package cc.rigoligo.jlusmartcard.network.requests

import cc.rigoligo.jlusmartcard.network.*
import org.json.JSONObject

suspend fun GetCardInfo(request: RequestGetCardInfo): ResponseGetCardInfo {
    val result = RequestWrapper.makeJsonRequest(
        path = "/User/GetCardInfoByAccountNoParm",
        method = "POST",
        parameters = mapOf("json" to "true"),
        additionalHeader = mapOf(
            "Referer" to "http://202.198.17.52:8090/Phone/Login",
            "Cookie" to "sourcetypeticket=${request.sessionKey}; imeiticket=${request.deviceId}"
        )
    )

    return when (result) {
        is Response.Success<PartialResponse<JSONObject>> -> {
            val data = result.data.data

            try {
                val subObj = JSONObject(data.getString("Msg"))
                val query = subObj.getJSONObject("query_card")
                val code = query.getString("retcode").toInt()
                val msg = query.getString("errmsg")
                return if (code != 0)
                    ResponseGetCardInfo(
                        ResponseStatus(code, msg, result.data.header)
                    )
                else {
                    val cards = query.getJSONArray("card")
                    // Assume the user has one card
                    val card = cards.getJSONObject(0)
                    ResponseGetCardInfo(
                        ResponseStatus(
                            code,
                            msg,
                            result.data.header
                        ),
                        studentName = card.getString("name"),
                        cardNo = card.getString("sno"),
                        accountId = card.getString("account"),
                        phoneNumber = card.getString("phone"),
                        bankAccount = card.getString("bankacc"),
                        personalId = card.getString("cert"),

                        balanceDb = card.getString("db_balance").toInt(),
                        balanceUnsettled = card.getString("unsettle_amount").toInt(),

                        autoTransferThreshold = card.getString("autotrans_limite").toInt(),
                        autoTransferAmount = card.getString("autotrans_amt").toInt(),
                        autoTransferFlag = card.getString("autotrans_flag").toInt()
                    )
                }
            } catch (e: Throwable) {
                return ResponseGetCardInfo(
                    ResponseStatus(
                        -1,
                        (e.localizedMessage ?: "") + '\n' + e.stackTraceToString(),
                        result.data.header
                    )
                )
            }
        }
        is Response.Error -> ResponseGetCardInfo(
            ResponseStatus(
                -1, result.exception.localizedMessage ?: "Exception has no message", mapOf()
            )
        )
    }
}