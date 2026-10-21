package com.example.autoparts.repository

import com.example.autoparts.data.remote.AuthApi
import com.example.autoparts.data.remote.LoginRequest
import com.example.autoparts.data.remote.MensajeErrorDto
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val sesionRepositorio: SesionRepository
) {
    suspend fun iniciarSesion(usuario: String, clave: String): Result<Unit> = try {
        val respuesta = api.login(LoginRequest(usuario.trim(), clave))
        val auth = respuesta.auth
        if (auth == null) {
            // El servidor respondió, pero sin sesión: se muestra su mensaje
            Result.failure(Exception(respuesta.message ?: "No se pudo iniciar sesión."))
        } else {
            sesionRepositorio.guardar(auth)
            Result.success(Unit)
        }
    } catch (e: HttpException) {
        // Código 4xx o 5xx: el cuerpo trae {"message": "..."}
        Result.failure(Exception(mensajeDelServidor(e) ?: "Error al iniciar sesión (${e.code()})."))
    } catch (e: IOException) {
        Result.failure(Exception("No se pudo conectar con el servidor. Revise adb reverse tcp:8080 y que la API esté activa."))
    } catch (e: JsonParseException) {
        Result.failure(Exception("La respuesta del login no tiene el formato esperado."))
    }

    suspend fun cerrarSesion() = sesionRepositorio.cerrar()

    private fun mensajeDelServidor(error: HttpException): String? = try {
        val cuerpo = error.response()?.errorBody()?.string()
        Gson().fromJson(cuerpo, MensajeErrorDto::class.java)?.message
    } catch (ex: Exception) {
        null
    }
}