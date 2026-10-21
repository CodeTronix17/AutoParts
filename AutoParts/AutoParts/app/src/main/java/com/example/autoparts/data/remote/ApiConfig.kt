package com.example.autoparts.data.remote

object ApiConfig {

    // Con adb reverse, localhost del dispositivo llega a la PC
    private const val SERVIDOR = "http://localhost:8080/"
    const val BASE_URL = SERVIDOR + "api/"          // debe terminar en /
    private const val BASE_IMAGENES = SERVIDOR + "images/"

    // "abc.jpg" -> "http://localhost:8080/images/abc.jpg"; null si el ítem no tiene foto
    fun urlFoto(foto: String?): String? = foto?.let { BASE_IMAGENES + it }
}