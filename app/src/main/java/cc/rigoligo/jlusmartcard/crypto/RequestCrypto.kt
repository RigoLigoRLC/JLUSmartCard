package cc.rigoligo.jlusmartcard.crypto

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object RequestCrypto {
    val desKey = "qJzGEh6hESZDVJeCnFPGuxzaiB7NLQM5"

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val encKeyspec = SecretKeySpec(Base64.getDecoder().decode(desKey), "DESede")

        cipher.init(Cipher.ENCRYPT_MODE, encKeyspec, IvParameterSpec("\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".toByteArray()))
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val encKeyspec = SecretKeySpec(Base64.getDecoder().decode(desKey), "DESede")

        cipher.init(Cipher.DECRYPT_MODE, encKeyspec, IvParameterSpec("\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000".toByteArray()))
        return cipher.doFinal(data)
    }
}