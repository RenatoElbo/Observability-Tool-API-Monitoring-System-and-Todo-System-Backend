package com.marlow.systems.login.util

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class GenerateAesKeyFromDatabase {
    /*
        private val GET_KEY = "IZhSNJULuDq0FvlQhRbpkg=="

        fun setAESKey(userNo: String): String {
            var getPassword = ""
            var encryptedPassword: String

            return try {
                MySqlDbConnection db = new MySqlDbConnection(); // from MysqlConnection class
                 val db = MySqlDbConnection() // Kotlin equivalent

                // Example connection code, replace with original code for db connection

                db.getConnection().use { conn ->
                    conn.prepareStatement("SELECT password FROM tblaccount_details WHERE userno = ?").use { stmt ->
                        stmt.setString(1, userNo)
                        conn.open() // if applicable in your helper class

                        stmt.executeQuery().use { rs ->
                            while (rs.next()) {
                                getPassword = rs.getString(1)
                            }
                        }
                    }
                }

                // Simulate retrieved password if needed
                // getPassword = "somePasswordFromDb"

                encryptedPassword = EncryptionDecryptionUtil().encryptString(GET_KEY, getPassword)
                encryptedPassword
            } catch (e: Exception) {
                e.message ?: "Unknown error"
            }*/
    }