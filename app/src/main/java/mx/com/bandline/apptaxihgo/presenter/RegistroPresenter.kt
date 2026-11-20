package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.RegistroModel
import mx.com.bandline.apptaxihgo.model.dataRegistro
import mx.com.bandline.apptaxihgo.view.ActivityRegistro

class RegistroPresenter(private val vista: ActivityRegistro) {

    private val modelo = RegistroModel()

    fun registrarUsuario(datos: dataRegistro) {
        if (datos.tipoCuenta.isNullOrEmpty()) {
            vista.mostrarError("Elige si eres Pasajero o Conductor.")
            return
        }
        if (datos.nombre.isBlank() || datos.apellidoPaterno.isBlank() || datos.telefono.isBlank() || datos.correo.isBlank() || datos.contrasena.isBlank()) {
            vista.mostrarError("Llena todos los campos obligatorios.")
            return
        }
        if (datos.contrasena != datos.confirmarContrasena) {
            vista.mostrarError("Las contraseñas no coinciden.")
            return
        }
        if (!datos.aceptaAviso) {
            vista.mostrarError("Debes aceptar el aviso de privacidad.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.registrarUsuario(datos)
                vista.mostrarCargando(false)

                if (respuesta.exito) {
                    vista.registroExitoso(respuesta.mensaje, datos.correo)
                } else {
                    vista.mostrarError(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarError("Error de red: " + e.localizedMessage)
            }
        }
    }
}