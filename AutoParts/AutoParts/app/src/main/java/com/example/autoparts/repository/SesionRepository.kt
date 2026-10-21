package com.example.autoparts.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.autoparts.data.ClavesSesion
import com.example.autoparts.data.Sesion
import com.example.autoparts.data.remote.AuthDto
import com.example.autoparts.data.sesionDataStore
import com.example.autoparts.utils.JwtUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SesionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // null = no hay sesión guardada. Se actualiza solo cuando cambia el DataStore
    val sesion: Flow<Sesion?> = context.sesionDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            val token = p[ClavesSesion.TOKEN] ?: return@map null
            Sesion(
                token = token,
                usuario = p[ClavesSesion.USUARIO].orEmpty(),
                nombre = p[ClavesSesion.NOMBRE].orEmpty(),
                rol = p[ClavesSesion.ROL].orEmpty(),
                expiraEn = p[ClavesSesion.EXPIRA_EN] ?: 0L
            )
        }

    suspend fun guardar(auth: AuthDto) {
        context.sesionDataStore.edit { p ->
            p[ClavesSesion.TOKEN] = auth.token
            p[ClavesSesion.USUARIO] = auth.username
            p[ClavesSesion.NOMBRE] = auth.nombre
            p[ClavesSesion.ROL] = auth.role
            p[ClavesSesion.EXPIRA_EN] = JwtUtil.expiracion(auth.token) ?: 0L
        }
    }

    suspend fun cerrar() {
        context.sesionDataStore.edit { it.clear() }
    }

    // Para el interceptor de OkHttp: corre en un hilo de red y no es una corrutina
    fun sesionActual(): Sesion? = runBlocking { sesion.first() }
}