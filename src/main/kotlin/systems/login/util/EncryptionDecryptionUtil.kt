package com.marlow.systems.login.util

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionDecryptionUtil {
    private val invalidByte = "Invalid key size: must be 16, 24, or 32 bytes"

    fun EncryptString(keyParam: String, plainTextParam: String): String {
        val keyBytes = keyParam.toByteArray(Charsets.UTF_8)
        require(keyBytes.size == 16 || keyBytes.size == 24 || keyBytes.size == 32) { invalidByte }

        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)

        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = cipher.doFinal(plainTextParam.toByteArray(Charsets.UTF_8))

        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun encryptStringAuto(plainTextParam: String): String {
        val key = "IZhSNJULuDq0FvlQhRbpkg=="

        val keyBytes = key.toByteArray(Charsets.UTF_8)
        require(keyBytes.size == 16 || keyBytes.size == 24 || keyBytes.size == 32) { invalidByte }

        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = cipher.doFinal(plainTextParam.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decryptString(keyParam: String, cipherText: String): String {
        val keyBytes = keyParam.toByteArray(Charsets.UTF_8)
        require(keyBytes.size == 16 || keyBytes.size == 24 || keyBytes.size == 32) { invalidByte }

        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = Base64.getDecoder().decode(cipherText)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun decryptStringAuto(cipherTextParam: String): String {
        val key = "IZhSNJULuDq0FvlQhRbpkg=="
        val keyBytes = key.toByteArray(Charsets.UTF_8)

        require(keyBytes.size == 16 || keyBytes.size == 24 || keyBytes.size == 32) { invalidByte }

        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val encryptedBytes = Base64.getDecoder().decode(cipherTextParam)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}