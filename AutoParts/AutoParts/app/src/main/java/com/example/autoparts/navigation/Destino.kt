package com.example.autoparts.navigation



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destino(val ruta: String, val titulo: String, val icono: ImageVector) {
    data object Catalogo : Destino("catalogo", "Catálogo", Icons.Default.ShoppingCart)
    data object Repuestos : Destino("repuestos", "Repuestos", Icons.Default.Build)
    data object Servicios : Destino("servicios", "Servicios", Icons.Default.Settings)
}

val destinosInferiores = listOf(Destino.Catalogo, Destino.Repuestos, Destino.Servicios)
