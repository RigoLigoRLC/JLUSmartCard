package cc.rigoligo.jlusmartcard.session

import android.content.Context.MODE_PRIVATE
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import cc.rigoligo.jlusmartcard.JluSmartCardApplication
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.network.RequestCheckSessionValidity
import cc.rigoligo.jlusmartcard.network.RequestGetCardInfo
import cc.rigoligo.jlusmartcard.network.requests.CheckSessionValidity
import cc.rigoligo.jlusmartcard.network.requests.GetCardInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


object MasterSession {
    sealed class LoginState {
        object LoggingIn : LoginState()
        object LoggedIn : LoginState()
        object LoginExpired : LoginState()
        object NoSessionData : LoginState()
    }

    val pref = JluSmartCardApplication.instance.applicationContext.getSharedPreferences(
        JluSmartCardApplication.instance.getString(R.string.app_pref), MODE_PRIVATE
    )

    data class SessionState(
        var _loginState: MutableState<LoginState> = mutableStateOf(LoginState.NoSessionData),
        var _studentName: MutableState<String> = mutableStateOf(""),
        var _cardNo: MutableState<String> = mutableStateOf(""),
        var _accountId: MutableState<String> = mutableStateOf(""),
        var startupFinished: MutableState<Boolean> = mutableStateOf(false),

        var _sessionKey: MutableState<String> = mutableStateOf(pref.getString("SessionKey", "")!!),
        var _deviceUniqueId: MutableState<String> = mutableStateOf(pref.getString("DeviceUniqueId", "")!!)
    )

    var state = SessionState()

    val loginState by state._loginState
    val studentName by state._studentName
    val cardNo by state._cardNo
    val accountId by state._accountId
    val sessionKey by state._sessionKey
    val deviceUniqueId by state._deviceUniqueId

    fun RegenerateDeviceUniqueId() {
        val id = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.ROOT);
        state._deviceUniqueId.value = id
        val edit = pref.edit()
        edit.putString("DeviceUniqueId", id)
        edit.apply()
    }

    suspend fun refreshSession(newSessionKey: String? = null): Boolean {
        if (newSessionKey != null) state._sessionKey.value = newSessionKey
        val result = CheckSessionValidity(
            RequestCheckSessionValidity(
                sessionKey, deviceUniqueId
            )
        )
        if (result.status.code == 0) {
            state._studentName.value = result.studentName!!
            state._cardNo.value = result.cardNo!!
            state._accountId.value = result.accountId!!
            state._loginState.value = LoginState.LoggedIn
            return true
        } else {
            state._loginState.value = LoginState.LoginExpired
            return false
        }
    }

    suspend fun queryAccountBalance(): BalanceDetails {
        if (loginState != LoginState.LoggedIn) return BalanceDetails(0, 0)
        val result = GetCardInfo(RequestGetCardInfo(sessionKey, deviceUniqueId))
        return if (result.status.code == 0) BalanceDetails(
            DatabaseBalance = result.balanceDb ?: 0,
            UnsettledAmount = result.balanceUnsettled ?: 0
        ) else BalanceDetails(0, 0)
    }

    suspend fun startup(): Boolean {
        var ret: Boolean = false

        // Generate device ID
        if (deviceUniqueId.isEmpty()) RegenerateDeviceUniqueId()
        // Try logging in
        if (sessionKey.isEmpty()) {
            state._loginState.value = LoginState.NoSessionData
            return ret
        }

        withContext(Dispatchers.IO) {
            ret = refreshSession()
        }

        state.startupFinished.value = true
        return ret
    }
}