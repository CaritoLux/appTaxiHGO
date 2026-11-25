package mx.com.bandline.apptaxihgo.presenter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.ViajeModel
import mx.com.bandline.apptaxihgo.model.dataViaje
import mx.com.bandline.apptaxihgo.view.ActivityViajePasajero

class ViajePasajeroPresenter (private val vista: ActivityViajePasajero){

    private val modelo = ViajeModel()

    private var consulta: Job? = null
    private var idViaje = 0
    private var viajeActual: dataViaje? = null
    private var cancelando = false

    companion object {
        val MOTIVOS = listOf("Ya no lo necesito", "El taxi tarda mucho", "Pedí por error", "Otro")
        const val SEGUNDOS_ENTRE_CONSULTAS = 3
    }

    fun iniciarConsulta(token: String, id: Int) {
        if (id > 0) {
            idViaje = id
        }
        detenerConsulta()
        consulta = CoroutineScope(Dispatchers.Main).launch {
            var seguir = true
            while (seguir) {
                seguir = consultar(token)
                if (seguir) {
                    delay(SEGUNDOS_ENTRE_CONSULTAS * 1000L)
                }
            }
        }
    }
    fun detenerConsulta() {
        consulta?.cancel()
        consulta = null
    }
    private suspend fun consultar(token: String): Boolean {
        try {
            val respuesta = modelo.estado(token, idViaje)
            val viaje = respuesta.datos

            if (respuesta.exito && viaje != null) {
                vista.mostrarSinConexion(false)
                idViaje = viaje.idViaje
                viajeActual = viaje
                if (viaje.estado == "FINALIZADO" && viaje.puedeCalificar) {
                    vista.irACalificar(viaje)
                    return false
                }
                vista.mostrarViaje(viaje)
                return viaje.activo
            }
            if (respuesta.exito) {
                vista.sinViajeActivo(respuesta.mensaje)
                return false
            }
            if (modelo.ultimoCodigo == 401) {
                vista.sesionExpirada()
                return false
            }
            if (modelo.ultimoCodigo == 403 || modelo.ultimoCodigo == 404) {
                vista.mostrarError(respuesta.mensaje)
                return false
            }
            vista.mostrarSinConexion(true)
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            vista.mostrarSinConexion(true)
            return true
        }
    }
    fun pedirCancelar() {
        val viaje = viajeActual ?: return
        if (viaje.activo && viaje.puedeCancelar) {
            vista.mostrarDialogoCancelar(MOTIVOS)
        }
    }
    fun cancelar(token: String, motivo: String?, comentario: String) {
        val texto = comentario.trim()
        if (motivo.isNullOrEmpty()) {
            vista.mostrarErrorCancelar("Elige un motivo de cancelación.")
            return
        }
        if (motivo == "Otro" && texto.isEmpty()) {
            vista.mostrarErrorCancelar("Cuéntanos el motivo.")
            return
        }
        if (texto.length > 150) {
            vista.mostrarErrorCancelar("El comentario puede tener hasta 150 caracteres.")
            return
        }
        if (cancelando) {
            return
        }

        cancelando = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargandoCancelar(true)
                val respuesta = modelo.cancelar(token, idViaje, motivo, texto)
                vista.mostrarCargandoCancelar(false)
                val viaje = respuesta.datos

                if (respuesta.exito && viaje != null) {
                    detenerConsulta()
                    viajeActual = viaje
                    vista.viajeCancelado(respuesta.mensaje, viaje)
                } else if (modelo.ultimoCodigo == 401) {
                    vista.sesionExpirada()
                } else {
                    // ej. "El viaje ya no se puede cancelar." (el taxi ya había iniciado el viaje)
                    vista.mostrarErrorCancelar(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargandoCancelar(false)
                vista.mostrarErrorCancelar("Error de red: " + e.localizedMessage)
            }
            cancelando = false
        }
    }
    fun buscarOtroTaxi() {
        val viaje = viajeActual ?: return
        vista.irABuscarOtro(viaje)
    }
}