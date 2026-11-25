package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.LoginModel
import mx.com.bandline.apptaxihgo.view.ActivityLogin

class LoginPresenter(private val vista: ActivityLogin) {
    private val modelo = LoginModel()

    fun iniciarSesion(correo: String, contrasena: String) {
        //Limpiamos los errores anteriores antes de volver a validar
        vista.limpiarErrores()

        var esValido = true
        val email = correo.trim().lowercase()

        // Validaciones locales (Rellenar campos en rojo)
        if (email.isBlank()) {
            vista.mostrarErrorCorreo("Ingresa tu correo electrónico.")
            esValido = false
        }
        if (contrasena.isBlank()) {
            vista.mostrarErrorContrasena("Ingresa tu contraseña.")
            esValido = false
        }

        // Si falta algo, detenemos el proceso aquí
        if (!esValido) return

        // Petición al servidor
        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.iniciarSesion(email, contrasena)
                vista.mostrarCargando(false)

                if (respuesta.exito) {
                    vista.loginExitoso(respuesta.datos!!)
                } else {
                    vista.mostrarErrorServidor(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarErrorServidor("Error de conexión: ${e.localizedMessage}")
            }
        }
    }
}