package com.example.autoparts.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtil {
    // Devuelve el campo "exp" del token, o null si no se puede leer
    fun expiracion(token: String): Long? = try {
        val datos = token.split(".")[1]
        val bytes = Base64.decode(datos, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        JSONObject(String(bytes)).optLong("exp", 0L).takeIf { it > 0L }
    } catch (e: Exception) {
        null
    }
}