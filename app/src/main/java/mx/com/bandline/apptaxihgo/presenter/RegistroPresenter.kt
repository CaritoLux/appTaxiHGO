package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.RegistroModel
import mx.com.bandline.apptaxihgo.model.dataRegistro
import mx.com.bandline.apptaxihgo.view.ActivityRegistro

class RegistroPresenter(private val vista: ActivityRegistro) {

    private val modelo = RegistroModel()
    private val regexPass = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{8,20}\$".toRegex()

    fun registrarUsuario(datos: dataRegistro) {
        vista.limpiarErrores()
        var esValido = true

        if (datos.tipoCuenta.isNullOrEmpty()) {
            vista.mostrarError("Elige si eres Pasajero o Conductor.")
            esValido = false
        }
        if (datos.nombre.isBlank()) {
            vista.mostrarErrorNombre("Ingresa tu nombre.")
            esValido = false
        }
        if (datos.apellidoPaterno.isBlank()) {
            vista.mostrarErrorPaterno("Ingresa tu apellido paterno.")
            esValido = false
        }
        if (datos.telefono.trim().length != 10) {
            vista.mostrarErrorTelefono("Debe tener 10 dígitos.")
            esValido = false
        }
        if (datos.correo.isBlank() || !datos.correo.contains("@")) {
            vista.mostrarErrorCorreo("Ingresa un correo válido.")
            esValido = false
        }
        if (!datos.contrasena.matches(regexPass)) {
            vista.mostrarErrorContrasena("De 8 a 20 caracteres, mayúscula, minúscula, número y símbolo.")
            esValido = false
        }
        if (datos.contrasena != datos.confirmarContrasena || datos.confirmarContrasena.isBlank()) {
            vista.mostrarErrorConfirmar("Las contraseñas no coinciden.")
            esValido = false
        }
        if (!datos.aceptaAviso) {
            vista.mostrarError("Debes aceptar el aviso de privacidad.")
            esValido = false
        }

        if (!esValido) return

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