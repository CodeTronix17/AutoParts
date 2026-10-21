package com.example.autoparts.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoparts.data.Sesion
import com.example.autoparts.data.remote.TipoItem
import com.example.autoparts.ui.views.DetalleRepuestoView
import com.example.autoparts.ui.views.FormularioRepuestoView
import com.example.autoparts.ui.views.ListaRepuestosView
import com.example.autoparts.ui.views.LoginView
import com.example.autoparts.viewmodel.EstadoSesion
import com.example.autoparts.viewmodel.RepuestosViewModel
import com.example.autoparts.viewmodel.SesionViewModel

@Composable
fun NavManager(
    viewModel: RepuestosViewModel,
    sesionViewModel: SesionViewModel                                  // NUEVO
) {
    val estadoSesion by sesionViewModel.estado.collectAsStateWithLifecycle()

    // Al cerrar la sesión no deben quedar datos del usuario anterior
    LaunchedEffect(estadoSesion) {
        if (estadoSesion is EstadoSesion.SinSesion) viewModel.reiniciar()
    }

    // NUEVO: lo que se ve depende solo del estado de la sesión
    when (val estado = estadoSesion) {
        EstadoSesion.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        EstadoSesion.SinSesion -> LoginView(sesionViewModel)
        is EstadoSesion.ConSesion -> ContenidoApp(
            viewModel = viewModel,
            sesion = estado.sesion,
            onCerrarSesion = sesionViewModel::cerrarSesion
        )
    }
}

// Lo que antes era todo el NavManager: la barra inferior y el NavHost
@Composable
private fun ContenidoApp(
    viewModel: RepuestosViewModel,
    sesion: Sesion,
    onCerrarSesion: () -> Unit
) {
    // La sesión empezó: ahora sí se puede pedir el catálogo
    LaunchedEffect(sesion.token) { viewModel.cargar() }

    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val abrirDetalle = { id: Int -> navController.navigate("detalle/$id") }
    val abrirNuevo = { navController.navigate("formulario") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (destinosInferiores.any { it.ruta == rutaActual }) {
                NavigationBar {
                    destinosInferiores.forEach { destino ->
                        NavigationBarItem(
                            selected = rutaActual == destino.ruta,
                            onClick = {
                                navController.navigate(destino.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                            label = { Text(destino.titulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Catalogo.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destino.Catalogo.ruta) {
                ListaRepuestosView(
                    viewModel, tipo = null, titulo = Destino.Catalogo.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo,
                    puedeCrear = sesion.puedeEditar, onCerrarSesion = onCerrarSesion     // NUEVO
                )
            }
            composable(Destino.Repuestos.ruta) {
                ListaRepuestosView(
                    viewModel, tipo = TipoItem.REPUESTO, titulo = Destino.Repuestos.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo,
                    puedeCrear = sesion.puedeEditar, onCerrarSesion = onCerrarSesion     // NUEVO
                )
            }
            composable(Destino.Servicios.ruta) {
                ListaRepuestosView(
                    viewModel, tipo = TipoItem.SERVICIO, titulo = Destino.Servicios.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo,
                    puedeCrear = sesion.puedeEditar, onCerrarSesion = onCerrarSesion     // NUEVO
                )
            }

            composable(
                route = "detalle/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: return@composable
                DetalleRepuestoView(
                    viewModel = viewModel,
                    id = id,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate("formulario?id=$id") },
                    puedeEditar = sesion.puedeEditar,                                   // NUEVO
                    puedeEliminar = sesion.puedeEliminar                                // NUEVO
                )
            }

            composable(
                route = "formulario?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id")?.takeIf { it != -1 }
                FormularioRepuestoView(
                    viewModel = viewModel,
                    id = id,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}
