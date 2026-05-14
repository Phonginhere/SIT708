package com.example.p71.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        return bytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
    }

    @TypeConverter
    fun toByteArray(encoded: String?): ByteArray? {
        return encoded?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
    }
}