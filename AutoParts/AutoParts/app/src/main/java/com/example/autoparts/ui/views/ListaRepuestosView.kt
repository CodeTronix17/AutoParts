package com.example.autoparts.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoparts.data.remote.TipoItem
import com.example.autoparts.ui.components.RepuestoCard
import com.example.autoparts.viewmodel.RepuestosViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRepuestosView(
    viewModel: RepuestosViewModel,
    tipo: TipoItem?,                  // null = todos; REPUESTO o SERVICIO = solo ese tipo
    titulo: String,
    onAbrirDetalle: (Int) -> Unit,
    onNuevo: () -> Unit,
    puedeCrear: Boolean,                // NUEVO: ADMIN y RECEPCIONISTA
    onCerrarSesion: () -> Unit          // NUEVO
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var busqueda by rememberSaveable { mutableStateOf("") }      // sobrevive a girar la pantalla

    // Filtro: primero por tipo (la pestaña) y luego por el texto escrito
    val visibles = remember(estado.repuestos, tipo, busqueda) {
        val texto = busqueda.trim()
        estado.repuestos.filter { item ->
            (tipo == null || item.tipo == tipo) &&
                    (texto.isEmpty() ||
                            item.nombre.contains(texto, ignoreCase = true) ||
                            item.descripcion.orEmpty().contains(texto, ignoreCase = true))
        }
    }

    // Muestra cada mensaje del ViewModel una sola vez
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),   // la barra inferior ya cuida ese espacio
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                actions = {
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = onCerrarSesion) {                        // NUEVO
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            if (puedeCrear) {                                                       // CAMBIO
                FloatingActionButton(onClick = onNuevo) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (estado.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            // Buscador tipo píldora
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por nombre o descripción") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                estado.error != null && estado.repuestos.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(estado.error!!, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.cargar() }) { Text("Reintentar") }
                    }
                }
                visibles.isEmpty() && !estado.cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (busqueda.isBlank()) "Aún no hay registros"
                            else "Sin resultados para \"$busqueda\""
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            visibles,
                            key = { indice, item -> item.id ?: "sin-id-$indice" }
                        ) { _, item ->
                            RepuestoCard(
                                item = item,
                                onClick = { item.id?.let(onAbrirDetalle) }
                            )
                        }
                    }
                }
            }
        }
    }
}