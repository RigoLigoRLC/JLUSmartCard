package cc.rigoligo.jlusmartcard.network

data class RequestCheckSessionValidity(
    val sessionKey: String,
    val deviceId: String,
)

data class RequestGetRsaPublicKey(
    val loginSessionCookie: String,
    val cardNumber: String,
    val deviceId: String,
)

data class RequestLoginRequest(
    val cardNumber: String,
    val encryptedPassword: String,
    val verificationCode: String,
    val loginSessionCookie: String,
    val loginSessionKey: String,
    val deviceId: String,
)

data class RequestGetCardInfo(
    val sessionKey: String,
    val deviceId: String,
)

data class RequestGetPaymentCode(
    val accountId: String,
    val sessionKey: String,
    val deviceId: String,
)