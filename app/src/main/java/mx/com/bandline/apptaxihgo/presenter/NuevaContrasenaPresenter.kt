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
        vista.limpiarErrores()
        var esValido = true

        if (codigo.length != 6) {
            vista.mostrarErrorCodigo("Ingresa el código de 6 dígitos.")
            esValido = false
        }
        if (!contrasena.matches(regex)) {
            vista.mostrarErrorContrasena("Debe tener 8-20 caracteres, mayúscula, minúscula, número y símbolo.")
            esValido = false
        }
        if (contrasena != confirmar) {
            vista.mostrarErrorConfirmar("Las contraseñas no coinciden.")
            esValido = false
        }

        if (!esValido) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.cambiarContrasena(correo, codigo, contrasena)
                vista.mostrarCargando(false)

                if (respuesta.exito) {
                    vista.contrasenaCambiada(respuesta.mensaje)
                } else {
                    vista.mostrarErrorServidor(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarErrorServidor("Error de red: ${e.localizedMessage}")
            }
        }
    }
}