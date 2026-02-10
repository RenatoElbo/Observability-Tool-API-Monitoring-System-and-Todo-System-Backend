package com.marlow.systems.login.util

import java.util.Base64
import javax.crypto.KeyGenerator

class GenerateAesKey {
    fun generateKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }
}