package cc.rigoligo.jlusmartcard.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.material.ScaffoldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.rigoligo.jlusmartcard.JluSmartCardApplication
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.network.RequestGetRsaPublicKey
import cc.rigoligo.jlusmartcard.network.RequestLoginRequest
import cc.rigoligo.jlusmartcard.network.requests.GetLoginVerificationCode
import cc.rigoligo.jlusmartcard.network.requests.GetRsaPublicKey
import cc.rigoligo.jlusmartcard.network.requests.LoginRequest
import cc.rigoligo.jlusmartcard.session.MasterSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

// Reference: https://medium.com/@benlue/forms-with-jetpack-compose-d80c4086dd27

class LoginScreenViewModel(scaffoldState: ScaffoldState) : ViewModel() {
    private val hexArray = "0123456789abcdef".toCharArray()

    enum class VerificationCodeState {
        LoadFailed,
        Loading,
        Loaded
    }

    data class LoginScreenState(
        val scaffoldState: ScaffoldState,
        val currentCardNumber: String = "",
        val currentPassword: String = "",
        val currentVerificationCode: String = "",
        val verificationCodeState: VerificationCodeState = VerificationCodeState.Loading,
        val verificationCodePicture: Bitmap? = null,

        val loginSuccessToast: MutableSharedFlow<String> = MutableSharedFlow(),
        val loginSnackbarInfo: MutableSharedFlow<String> = MutableSharedFlow(),
    )

    private val _uiState = MutableStateFlow(LoginScreenState(scaffoldState))
    val uiState = _uiState.asStateFlow()

    private var loginSessionCookie: String = ""

    fun updateCardNumber(cardNo: String) {
        _uiState.value = _uiState.value.copy(currentCardNumber = cardNo)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(currentPassword = password)
    }

    fun updateVerificationCode(verifCode: String) {
        _uiState.value = _uiState.value.copy(currentVerificationCode = verifCode)
    }

    fun refreshVerificationCodeImage() {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(verificationCodeState = VerificationCodeState.Loading)
            val result = GetLoginVerificationCode()
            if (result.status.code == 0) {
                _uiState.value = _uiState.value.copy(
                    verificationCodeState = VerificationCodeState.Loaded,
                    verificationCodePicture = result.image
                )
                loginSessionCookie = result.loginSessionCookie!!
            } else {
                _uiState.value =
                    _uiState.value.copy(verificationCodeState = VerificationCodeState.LoadFailed)
            }
        }
    }

    fun validateLoginForm(): Boolean {
        return true
    }

    fun performLogin() {
        if (!validateLoginForm()) return
        // Perform login actions asynchronously
        viewModelScope.launch {
            val rsaKeyResponse = GetRsaPublicKey(
                RequestGetRsaPublicKey(
                    loginSessionCookie,
                    _uiState.value.currentCardNumber,
                    MasterSession.deviceUniqueId
                )
            )
            if (rsaKeyResponse.status.code != 0) {
                Log.e("Login", "Error on GetRsaPublicKey, msg=${rsaKeyResponse.status.message}")
                _uiState.value.loginSnackbarInfo.emit(
                    "${JluSmartCardApplication.instance.getString(R.string.login_get_rsa_pubkey_failed)}\n" +
                            rsaKeyResponse.status.message
                )
                refreshVerificationCodeImage()
                return@launch
            }

            // Encrypt password
            val cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding")
            val pubExp = BigInteger(rsaKeyResponse.publicExponent!!, 16)
            val mod = BigInteger(rsaKeyResponse.modulo!!, 16)
            val keySpec = RSAPublicKeySpec(mod, pubExp)
            cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(keySpec))
            val ciphertext = cipher.doFinal(_uiState.value.currentPassword.toByteArray())
            // Convert ciphertext to hex string
            // From https://beecoder.org/en/kotlin/convert-byte-array-to-hexadecimal-in-kotlin
            val hexChars = CharArray(ciphertext.size * 2)
            for (j in ciphertext.indices) {
                val v = ciphertext[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            val pwdEncrypted = String(hexChars)

            // Perform login
            val loginResponse = LoginRequest(
                RequestLoginRequest(
                    _uiState.value.currentCardNumber,
                    pwdEncrypted,
                    _uiState.value.currentVerificationCode,
                    loginSessionCookie,
                    rsaKeyResponse.loginSessionKey!!,
                    MasterSession.deviceUniqueId
                )
            )
            if(loginResponse.status.code != 0) {
                Log.e("Login", "Error on LoginRequest, msg=${loginResponse.status.message}")
                _uiState.value.loginSnackbarInfo.emit(
                    "${JluSmartCardApplication.instance.getString(R.string.login_login_failed)}\n" +
                            loginResponse.status.message
                )
                refreshVerificationCodeImage()
                return@launch
            }
            Log.i("Login", "Session Key=${loginResponse.sessionKey}")
            // Store session key
            _uiState.value.loginSuccessToast.emit(JluSmartCardApplication.instance.getString(R.string.login_login_successful))
            MasterSession.pref.edit().putString("SessionKey", loginResponse.sessionKey).apply()
            MasterSession.refreshSession(loginResponse.sessionKey)
        }
    }

    init {
        refreshVerificationCodeImage()
    }

}