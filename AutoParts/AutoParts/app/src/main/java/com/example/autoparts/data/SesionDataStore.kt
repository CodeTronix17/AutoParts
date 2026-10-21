package com.example.autoparts.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Una sola instancia por archivo: se declara a nivel superior, nunca dentro de una clase
val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

object ClavesSesion {
    val TOKEN = stringPreferencesKey("token")
    val USUARIO = stringPreferencesKey("usuario")
    val NOMBRE = stringPreferencesKey("nombre")
    val ROL = stringPreferencesKey("rol")
    val EXPIRA_EN = longPreferencesKey("expira_en")
}
