package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.CuentaModel
import mx.com.bandline.apptaxihgo.view.ActivityCuenta

class CuentaPresenter(private val vista: ActivityCuenta) {

    private val modelo = CuentaModel()

    fun cargarPerfil(token: String) {
        if (token.isEmpty()) {
            vista.sesionCerrada()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val respuesta = modelo.obtenerPerfil(token)
                if (respuesta.exito && respuesta.datos != null) {
                    vista.mostrarPerfil(respuesta.datos)
                } else {
                    vista.mostrarError(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarError("Error al cargar perfil: " + e.localizedMessage)
            }
        }
    }
}