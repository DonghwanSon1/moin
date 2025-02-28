package com.moin.project.common.authority

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class EncryptionUtil {

  private val algorithm = "AES"

  @Value("\${encryption.secretKey}")
  private lateinit var secretKeyBase64: String

  private val secretKey: SecretKey by lazy { getSecretKeySpec(secretKeyBase64) }

  // AES-256 암호화
  fun encrypt(data: String): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encryptedData = cipher.doFinal(data.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedData)
  }

  // AES-256 복호화
  fun decrypt(encryptedData: String): String {
    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val decodedData = Base64.getDecoder().decode(encryptedData)
    val decryptedData = cipher.doFinal(decodedData)
    return String(decryptedData)
  }

  // AES-256을 위한 SecretKeySpec 생성
  private fun getSecretKeySpec(key: String): SecretKeySpec {
    val decodedKey = Base64.getDecoder().decode(key)
    return SecretKeySpec(decodedKey, algorithm)
  }
}
