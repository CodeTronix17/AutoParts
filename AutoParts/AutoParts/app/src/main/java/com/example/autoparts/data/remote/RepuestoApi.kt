package com.example.autoparts.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface RepuestoApi {
    //develve una collecion de respuestos-servicios
    @GET("repuestos-servicios")
    suspend fun obtenerTodos(): List<RepuestoDto>

    //nos va ha devolver un objeto de respuesto-servicio
    @GET("repuestos-servicios/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): RepuestoDto

    @Multipart
    @POST("repuestos-servicios")
    suspend fun crear(
        @Part("dto") dto: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): RespuestaGuardadoDto

    @Multipart
    @PUT("repuestos-servicios/{id}")
    suspend fun actualizar(
        @Path("id") id: Int,
        @Part("dto") dto: RequestBody,
        @Part imagen: MultipartBody.Part?          // si es null, el servidor conserva la foto
    ): RespuestaGuardadoDto

    @DELETE("repuestos-servicios/{id}")
    suspend fun eliminar(@Path("id") id: Int): MensajeDto
}