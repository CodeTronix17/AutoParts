package com.example.autoparts.data.remote

enum class TipoItem { REPUESTO, SERVICIO }

data class RepuestoDto(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val foto: String? = null,
    val tipo: TipoItem
)

//estas son las respuesta del POST Y PUT
data class RespuestaGuardadoDto(
    val message: String,
    val repuestoServicio: RepuestoDto
)

//respuesta de eliminado DELETE
data class MensajeDto(
    val message: String
)