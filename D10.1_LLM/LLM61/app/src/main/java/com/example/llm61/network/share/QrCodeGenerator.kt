package com.example.llm61.network.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object QrCodeGenerator {

    data class SharedProfile(
        val username: String,
        val tier: String,
        val totalQuestions: Int,
        val totalCorrect: Int,
        val interests: List<String>
    )

    fun buildProfileShareUri(
        username: String,
        tier: String,
        totalQuestions: Int,
        totalCorrect: Int,
        interests: Set<String>
    ): String {
        val json = JSONObject().apply {
            put("u", username)
            put("t", tier)
            put("q", totalQuestions)
            put("c", totalCorrect)
            put("i", JSONArray(interests.toList()))
        }
        val base64 = Base64.encodeToString(
            json.toString().toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        return "llm61://profile?data=$base64"
    }

    fun decodeProfileShareUri(data: String): SharedProfile? = try {
        val bytes = Base64.decode(
            data,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val json = JSONObject(String(bytes, Charsets.UTF_8))
        val arr = json.getJSONArray("i")
        val interests = mutableListOf<String>()
        for (i in 0 until arr.length()) interests.add(arr.getString(i))
        SharedProfile(
            username = json.getString("u"),
            tier = json.getString("t"),
            totalQuestions = json.getInt("q"),
            totalCorrect = json.getInt("c"),
            interests = interests
        )
    } catch (e: Exception) {
        null
    }

    fun generateBitmap(text: String, sizePx: Int = 512): Bitmap {
        val matrix: BitMatrix = MultiFormatWriter()
            .encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = matrix.width
        val height = matrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(sharedDir, "profile_qr_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}