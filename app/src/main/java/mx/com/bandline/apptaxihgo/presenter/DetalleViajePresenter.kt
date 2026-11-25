package mx.com.bandline.apptaxihgo.presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.ViajeModel
import mx.com.bandline.apptaxihgo.view.ActivityDetalleViaje

class DetalleViajePresenter (private val vista: ActivityDetalleViaje){

    private val modelo = ViajeModel()

    fun cargar(token: String, idViaje: Int) {
        if (idViaje <= 0) {
            vista.mostrarError("No se recibió el viaje.")
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.detalle(token, idViaje)
                vista.mostrarCargando(false)
                val viaje = respuesta.datos

                if (respuesta.exito && viaje != null) {
                    vista.mostrarDetalle(viaje)
                } else if (modelo.ultimoCodigo == 401) {
                    vista.sesionExpirada()
                } else {
                    vista.mostrarError(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarError("Error al cargar el viaje: " + e.localizedMessage)
            }
        }
    }
}