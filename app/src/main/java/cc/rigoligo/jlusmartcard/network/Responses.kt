package cc.rigoligo.jlusmartcard.network

import android.graphics.Bitmap

sealed class Response<out R> {
    data class Success<out T>(val data: T) : Response<T>()
    data class Error(val exception: Exception) : Response<Nothing>()
}

data class PartialResponse<T>(
    val header: Map<String, List<String>>,
    val data: T,
)

data class ResponseStatus(
    val code: Int,
    val message: String,
    val header: Map<String, List<String>>,
)

data class ResponseCheckSessionValidity(
    val status: ResponseStatus,
    val studentName: String? = null,
    val cardNo: String? = null,
    val accountId: String? = null,
)

data class ResponseGetLoginVerificationCode(
    val status: ResponseStatus,
    val image: Bitmap?,
    val loginSessionCookie: String?,
)

data class ResponseGetRsaPublicKey(
    val status: ResponseStatus,
    val publicExponent: String?,
    val modulo: String?,
    val loginSessionKey: String?,
)

data class ResponseLoginRequest(
    val status: ResponseStatus,
    val sessionKey: String?,
)

data class ResponseGetCardInfo(
    val status: ResponseStatus,
    val studentName: String? = null,
    val cardNo: String? = null,
    val accountId: String? = null,
    val phoneNumber: String? = null,
    val bankAccount: String? = null,
    val personalId: String? = null,

    val balanceDb: Int? = null,
    val balanceUnsettled: Int? = null,

    val autoTransferThreshold: Int? = null,
    val autoTransferAmount: Int? = null,
    val autoTransferFlag: Int? = null,
)

data class ResponseGetPaymentCode(
    val status: ResponseStatus,
    val codes: List<String>,
)