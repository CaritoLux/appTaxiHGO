package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.LoginModel
import mx.com.bandline.apptaxihgo.view.ActivityLogin

class LoginPresenter(private val vista: ActivityLogin) {

    private val modelo = LoginModel()

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            vista.mostrarError("Escribe tu correo y contraseña.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.iniciarSesion(correo.trim().lowercase(), contrasena)
                vista.mostrarCargando(false)

                if (respuesta.exito && respuesta.datos != null) {
                    vista.loginExitoso(respuesta.datos)
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