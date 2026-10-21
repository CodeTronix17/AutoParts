package com.example.autoparts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoparts.data.remote.RepuestoDto
import com.example.autoparts.repository.ImagenSeleccionada
import com.example.autoparts.repository.RepuestoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListaUiState(
    val cargando: Boolean = false,
    val repuestos: List<RepuestoDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RepuestosViewModel @Inject constructor(
    private val repositorio: RepuestoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ListaUiState())
    val estado: StateFlow<ListaUiState> = _estado.asStateFlow()


    private val _detalle = MutableStateFlow<RepuestoDto?>(null)
    val detalle: StateFlow<RepuestoDto?> = _detalle.asStateFlow()

    // Estado de la carga del detalle, para no dejar el indicador girando para siempre si falla
    private val _cargandoDetalle = MutableStateFlow(false)
    val cargandoDetalle: StateFlow<Boolean> = _cargandoDetalle.asStateFlow()

    private val _errorDetalle = MutableStateFlow<String?>(null)
    val errorDetalle: StateFlow<String?> = _errorDetalle.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()



    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.obtenerTodos()
                .onSuccess { lista ->
                    _estado.update { it.copy(cargando = false, repuestos = lista) }
                }
                .onFailure { e ->
                    _estado.update { it.copy(cargando = false, error = e.message) }
                }
        }
    }

    // GET /repuestos-servicios/{id}: datos frescos del servidor para el detalle y la edición
    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _cargandoDetalle.value = true
            _errorDetalle.value = null
            repositorio.obtenerPorId(id)
                .onSuccess {
                    _detalle.value = it
                    _errorDetalle.value = null
                }
                .onFailure {
                    _errorDetalle.value = it.message
                    _mensaje.value = it.message
                }
            _cargandoDetalle.value = false
        }
    }

    fun limpiarDetalle() {
        _detalle.value = null
        _errorDetalle.value = null
    }

    // id == null: crea (POST); id != null: edita (PUT)
    fun guardar(
        id: Int?,
        dto: RepuestoDto,
        imagen: ImagenSeleccionada?,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {
            _guardando.value = true
            val resultado =
                if (id == null) repositorio.crear(dto, imagen)
                else repositorio.actualizar(id, dto, imagen)
            _guardando.value = false

            resultado
                .onSuccess { guardado ->
                    _detalle.value = guardado          // NUEVO: el detalle muestra los datos ya actualizados
                    _mensaje.value =
                        if (id == null) "Registrado correctamente" else "Actualizado correctamente"
                    cargar()
                    alTerminar()
                }
                .onFailure { _mensaje.value = it.message }
        }
    }

    // CAMBIO: avisa cuando termina bien, para que el detalle pueda volver a la lista
    fun eliminar(id: Int, alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            repositorio.eliminar(id)
                .onSuccess { mensaje ->
                    _mensaje.value = mensaje
                    cargar()
                    alTerminar()
                }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }

    // Al cerrar sesión no deben quedar datos del usuario anterior
    fun reiniciar() {
        _estado.value = ListaUiState()
        _detalle.value = null
        _mensaje.value = null
    }
}