package com.example.autoparts.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    // POST http://localhost:8080/api/auth/login: la ruta se complementa con baseUrl (.../api/)
    @POST("auth/login")
    suspend fun login(@Body datos: LoginRequest): LoginResponse
}