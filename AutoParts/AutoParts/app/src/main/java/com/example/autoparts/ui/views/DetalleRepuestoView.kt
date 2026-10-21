package com.example.autoparts.ui.views



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.autoparts.data.remote.ApiConfig
import com.example.autoparts.data.remote.TipoItem
import com.example.autoparts.viewmodel.RepuestosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRepuestoView(
    viewModel: RepuestosViewModel,
    id: Int,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    puedeEditar: Boolean,               // NUEVO: ADMIN y RECEPCIONISTA
    puedeEliminar: Boolean              // NUEVO: solo ADMIN
) {
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val cargandoDetalle by viewModel.cargandoDetalle.collectAsStateWithLifecycle()
    val errorDetalle by viewModel.errorDetalle.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmarBorrado by remember { mutableStateOf(false) }

    // Pide al servidor el ítem completo cada vez que se entra a la pantalla
    LaunchedEffect(id) { viewModel.cargarDetalle(id) }

    // Si el ViewModel todavía guarda el ítem de otra pantalla, se ignora hasta que llegue el correcto
    val item = detalle?.takeIf { it.id == id }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (item != null) {
                        if (puedeEditar) {                                          // CAMBIO
                            IconButton(onClick = onEditar) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                        if (puedeEliminar) {                                        // CAMBIO
                            IconButton(onClick = { confirmarBorrado = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (errorDetalle != null && !cargandoDetalle) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = errorDetalle!!,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.cargarDetalle(id) }) {
                            Text("Reintentar")
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Foto grande
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val url = ApiConfig.urlFoto(item.foto)
                    if (url != null) {
                        AsyncImage(
                            model = url,
                            contentDescription = item.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp))
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.nombre, style = MaterialTheme.typography.headlineSmall)

                    FilaDato("Tipo", if (item.tipo == TipoItem.REPUESTO) "Repuesto" else "Servicio")
                    FilaDato("Precio", "$" + "%.2f".format(item.precio))
                    if (item.tipo == TipoItem.REPUESTO) {
                        FilaDato("Existencias", if (item.stock == 0) "Sin existencias" else "${item.stock} unidades")
                    }
                    FilaDato("Código", "#${item.id}")

                    HorizontalDivider()

                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                    Text(
                        item.descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (confirmarBorrado && item != null) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Eliminar registro") },
            text = { Text("¿Eliminar \"${item.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    viewModel.eliminar(id, onVolver)      // vuelve a la lista solo si se eliminó bien
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.SemiBold)
    }
}
