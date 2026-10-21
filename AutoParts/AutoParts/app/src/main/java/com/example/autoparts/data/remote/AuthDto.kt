package com.example.autoparts.data.remote

// Lo que se envía: {"username": "...", "password": "..."}
data class LoginRequest(
    val username: String,
    val password: String
)

// Respuesta: {"auth": {...}, "message": "..."}
// auth viene en null cuando el servicio responde solo con un mensaje
data class LoginResponse(
    val auth: AuthDto?,
    val message: String?
)

data class AuthDto(
    val token: String,
    val username: String,
    val nombre: String,
    val tipo: String,
    val role: String
)

// Cuerpo de un error: {"message": "Usuario o contraseña incorrectos"}
data class MensajeErrorDto(
    val message: String?
)
