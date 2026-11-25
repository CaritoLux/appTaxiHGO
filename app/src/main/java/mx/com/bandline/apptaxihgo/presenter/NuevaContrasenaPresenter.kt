package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.NuevaContrasenaModel
import mx.com.bandline.apptaxihgo.view.ActivityNuevaContrasena

class NuevaContrasenaPresenter(private val vista: ActivityNuevaContrasena) {
    private val modelo = NuevaContrasenaModel()
    // validacion de 8 a 20 carracteres, mayusculas y minusculas,y un caracter  especial
    private val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{8,20}\$".toRegex()

    fun cambiarContrasena(correo: String, codigo: String, contrasena: String, confirmar: String) {
        if (codigo.length != 6) {
            vista.mostrarError("Ingresa el código de 6 dígitos.")
            return
        }
        if (contrasena != confirmar) {
            vista.mostrarError("Las contraseñas no coinciden.")
            return
        }
        if (!contrasena.matches(regex)) {
            vista.mostrarError("La contraseña debe tener de 8 a 20 caracteres, mayúscula, minúscula, número y símbolo.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.cambiarContrasena(correo, codigo, contrasena)
                vista.mostrarCargando(false)

                if (respuesta.exito) {
                    vista.contrasenaCambiada(respuesta.mensaje)
                } else {
                    vista.mostrarError(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarError("Error de red: ${e.localizedMessage}")
            }
        }
    }
}