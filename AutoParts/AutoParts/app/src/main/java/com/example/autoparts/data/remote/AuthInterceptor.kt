package com.example.autoparts.data.remote

import com.example.autoparts.repository.SesionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sesionRepositorio: SesionRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // El login no lleva token: todavía no existe
        if (original.url.encodedPath.endsWith("/login")) return chain.proceed(original)

        val sesion = sesionRepositorio.sesionActual()

        // Token vencido: se cierra la sesión y la interfaz vuelve al login
        if (sesion != null && !sesion.estaVigente) {
            runBlocking { sesionRepositorio.cerrar() }
            return chain.proceed(original)
        }

        val peticion = if (sesion != null) {
            original.newBuilder()
                .header("Authorization", "Bearer ${sesion.token}")
                .build()
        } else {
            original
        }

        val respuesta = chain.proceed(peticion)

        // El servidor rechazó el token: la sesión ya no vale
        if (respuesta.code == 401 && sesion != null) {
            runBlocking { sesionRepositorio.cerrar() }
        }
        return respuesta
    }
}