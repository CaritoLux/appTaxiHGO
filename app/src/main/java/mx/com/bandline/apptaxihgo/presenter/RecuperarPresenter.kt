package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.RecuperarModel
import mx.com.bandline.apptaxihgo.view.ActivityRecuperar

class RecuperarPresenter(private val vista: ActivityRecuperar) {
    private val modelo = RecuperarModel()

    fun solicitarCodigo(correo: String) {
        val email = correo.trim().lowercase()
        if (email.isBlank()) {
            vista.mostrarError("Ingresa tu correo.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.solicitarCodigo(email)
                vista.mostrarCargando(false)

                if (respuesta.exito) {
                    vista.codigoEnviado(email)
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