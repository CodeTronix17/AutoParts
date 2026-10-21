package com.example.autoparts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoparts.data.Sesion
import com.example.autoparts.repository.AuthRepository
import com.example.autoparts.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion                      // todavía se lee el DataStore
    data object SinSesion : EstadoSesion                     // se muestra el login
    data class ConSesion(val sesion: Sesion) : EstadoSesion  // se muestra el catálogo
}

data class LoginUiState(
    val cargando: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SesionViewModel @Inject constructor(
    private val authRepositorio: AuthRepository,
    private val sesionRepositorio: SesionRepository
) : ViewModel() {

    // Se deriva del DataStore: cualquier cambio en la sesión actualiza la interfaz sin navegar a mano
    val estado: StateFlow<EstadoSesion> = sesionRepositorio.sesion
        .map { sesion ->
            if (sesion != null && sesion.estaVigente) EstadoSesion.ConSesion(sesion)
            else EstadoSesion.SinSesion
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    fun iniciarSesion(usuario: String, clave: String) {
        if (_login.value.cargando) return                    // ignora toques repetidos
        viewModelScope.launch {
            _login.value = LoginUiState(cargando = true)
            authRepositorio.iniciarSesion(usuario, clave)
                .onSuccess { _login.value = LoginUiState() }   // la sesión guardada cambia el estado y la pantalla pasa sola al catálogo
                .onFailure { _login.value = LoginUiState(error = it.message) }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { authRepositorio.cerrarSesion() }
    }
}