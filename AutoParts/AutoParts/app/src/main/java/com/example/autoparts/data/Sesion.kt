package com.example.autoparts.data

data class Sesion(
    val token: String,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val expiraEn: Long
) {
    // determinar si el token aun esta vigente
    val estaVigente: Boolean
        get() = expiraEn == 0L || System.currentTimeMillis() / 1000 < expiraEn

    // Misma regla que los @PreAuthorize del controlador de Spring
    val puedeEditar: Boolean get() = rol == "ADMIN" || rol == "RECEPCIONISTA"
    val puedeEliminar: Boolean get() = rol == "ADMIN"
}