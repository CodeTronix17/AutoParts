package com.example.autoparts.repository

import com.example.autoparts.data.remote.RepuestoApi
import com.example.autoparts.data.remote.RepuestoDto
import com.google.gson.Gson
import com.google.gson.JsonParseException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

// Foto elegida por el usuario, ya leída de la galería
class ImagenSeleccionada(val bytes: ByteArray, val mime: String)

class RepuestoRepository @Inject constructor(
    private val api: RepuestoApi
) {
    private val gson = Gson()

    suspend fun obtenerTodos(): Result<List<RepuestoDto>> =
        llamar { api.obtenerTodos() }

    suspend fun obtenerPorId(id: Int): Result<RepuestoDto> =
        llamar { api.obtenerPorId(id) }

    suspend fun crear(dto: RepuestoDto, imagen: ImagenSeleccionada?): Result<RepuestoDto> =
        llamar { api.crear(dto.aParteJson(), imagen?.aParte()).repuestoServicio }

    suspend fun actualizar(id: Int, dto: RepuestoDto, imagen: ImagenSeleccionada?): Result<RepuestoDto> =
        llamar { api.actualizar(id, dto.aParteJson(), imagen?.aParte()).repuestoServicio }

    suspend fun eliminar(id: Int): Result<String> =
        llamar { api.eliminar(id).message }

    // Parte "dto": el JSON debe declararse como application/json
    private fun RepuestoDto.aParteJson(): RequestBody =
        gson.toJson(this).toRequestBody("application/json".toMediaType())

    // Parte "imagen": un archivo con su nombre y su tipo
    private fun ImagenSeleccionada.aParte(): MultipartBody.Part {
        val extension = if (mime == "image/png") "png" else "jpg"
        return MultipartBody.Part.createFormData(
            "imagen", "imagen.$extension", bytes.toRequestBody(mime.toMediaType())
        )
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: HttpException) {
        Result.failure(Exception(mensajeHttp(e.code())))
    } catch (e: IOException) {
        Result.failure(Exception("No se pudo conectar con el servidor. Revise adb reverse y el contenedor."))
    } catch (e: JsonParseException) {
        Result.failure(Exception("La respuesta del servidor no tiene el formato esperado."))
    }

    private fun mensajeHttp(codigo: Int) = when (codigo) {
        400 -> "Datos inválidos: revise los campos (nombre, precio, stock, tipo)."
        // Antes:  401, 403 -> "Sin permiso: la API exige autenticación."
        401 -> "Su sesión expiró. Inicie sesión de nuevo."
        403 -> "Su rol no permite esta acción."
        404 -> "El repuesto o servicio ya no existe."
        413 -> "La imagen es demasiado grande para el servidor."
        415 -> "El servidor rechazó el tipo de contenido de la petición."
        else -> "Error del servidor ($codigo)."
    }
}