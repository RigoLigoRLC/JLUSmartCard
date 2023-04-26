package cc.rigoligo.jlusmartcard.crypto

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object RequestSigner {
    val privateKeyPem = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAOKXc9mFdLC49ln0zihEBFULK82NXo6DDad0T7W4jO3hICwjlKVbCncQvft6z74VZ3veUtfviHk1jGvNSj7Is9zUTR6ojDowA0+136YAVj5KwDYV2Mk33WI2iuov4crNgbMyffJDwFuI1a1fcvr3ayZNFxWyKZ542bb8R04B12ulAgMBAAECgYAwiEnq/DerJmK1j8acPz1CTds68p2fHpjNFg+Al5+vz7lJWvGanS5XpEFc3MgkKYd5s3vA/nAXrg1+hYDyg6BqM+lI6OHRZxPynaeQQeOiMDdTvR+pZ3b8uSx386riD1DM5d1oGRg9tMvJxQVCQU0bLrxViv2+KJf44dT3yqLJ3QJBAOwmsjpZi1BT+13IREjmdwEuTZRf7Ksffa1SvNyI+EoIpHZrM6V1w6vhRmJsENWe75qvJYy3hhem3i77O6z84oMCQQD1ow/fPTNxtM5qQKxMux3BEixp/j23ib+MIkoswmd+ARGtUEKqwnjXJWoXneXfWhMQTTJhBb5REwOEjOmv8IC3AkB09dlyMuVgHKgz07uWS6cHS7Ka2UOzoX4yePcXVzN6H3utNv02ZvRJzeJ5XsKbuwM7HqI/ZqogTsJejIoK7JkXAkAODxoudctG+8lArZju/1qxnT+rhWC0645qD+Bc9XeE77y6Rbi7G0xdTAfpeCEbCoXCzhhPE0wUSdlOsd4CMuq7AkBUD8WlEup0Ypa7oXTPGIdCOPZ+gxIWfEKBMAvi1paik+HIRfBdeaIPG1PyXrYDg8QWcZ3IrzMqLd9+jZ7XdYUF"

    fun sign(data: ByteArray): ByteArray {
        val key = Base64.getDecoder().decode(privateKeyPem)
        val var2 = PKCS8EncodedKeySpec(key)
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(var2)
        val signature = Signature.getInstance("SHA1withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }
}