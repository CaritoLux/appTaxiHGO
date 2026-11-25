package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.SolicitudModel
import mx.com.bandline.apptaxihgo.model.dataCotizacion
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.model.dataPedirViaje
import mx.com.bandline.apptaxihgo.view.ActivityConfirmarViaje

// Confirmar viaje: cotiza la ruta y pide el taxi
class ConfirmarViajePresenter(private val vista: ActivityConfirmarViaje) {

    private val modelo = SolicitudModel()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var token = ""
    private var origen = dataLugar()
    private var destino = dataLugar()
    private var cotizacion: dataCotizacion? = null
    private var pidiendo = false

    fun iniciar(token: String, origen: dataLugar, destino: dataLugar) {
        this.token = token
        this.origen = origen
        this.destino = destino
        if (token.isEmpty()) {
            vista.sesionVencida()
            return
        }
        vista.mostrarDestino(destino.nombre)
        vista.mostrarPuntos(origen, destino)
        cotizar()
    }

    private fun peticion(referencia: String, tarifa: Int?): dataPedirViaje {
        return dataPedirViaje(
            origenLat = origen.lat,
            origenLng = origen.lng,
            origenDireccion = origen.textoCompleto(),
            destinoLat = destino.lat,
            destinoLng = destino.lng,
            destinoDireccion = destino.textoCompleto(),
            referencia = referencia,
            tarifaCotizada = tarifa
        )
    }

    // cotizar_viaje.php calcula la ruta, la distancia, los minutos y la tarifa
    fun cotizar() {
        cotizacion = null
        vista.mostrarCotizando(true)
        scope.launch {
            try {
                val respuesta = modelo.cotizar(token, peticion("", null))
                vista.mostrarCotizando(false)
                val datos = respuesta.datos
                if (respuesta.exito && datos != null) {
                    cotizacion = datos
                    vista.mostrarCotizacion(datos)
                } else if (respuesta.codigo == "SESION_VENCIDA") {
                    vista.sesionVencida()
                } else {
                    // Fuera de zona o destino muy cerca: se arregla con "Cambiar", no con reintentar
                    val sePuedeReintentar = respuesta.codigo == null || respuesta.codigo == "ERROR_SERVIDOR"
                    vista.mostrarErrorCotizacion(respuesta.mensaje, sePuedeReintentar)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                vista.mostrarCotizando(false)
                vista.mostrarErrorCotizacion("Sin conexión. Revisa tu internet e inténtalo de nuevo.", true)
            }
        }
    }

    // crear_viaje.php guarda el viaje y manda la solicitud al taxi más cercano
    fun pedirTaxi(referencia: String) {
        val datosCotizacion = cotizacion ?: return
        if (pidiendo) return
        val texto = referencia.trim()
        if (texto.length > 100) {
            vista.mostrarErrorReferencia("Escribe hasta 100 caracteres.")
            return
        }
        vista.mostrarErrorReferencia(null)
        pidiendo = true
        vista.mostrarPidiendo(true)
        scope.launch {
            try {
                val respuesta = modelo.crear(token, peticion(texto, datosCotizacion.tarifa))
                pidiendo = false
                vista.mostrarPidiendo(false)
                val viaje = respuesta.datos
                when {
                    respuesta.exito && viaje != null -> vista.viajeCreado(viaje.folio)
                    respuesta.codigo == "VIAJE_ACTIVO" && viaje != null -> {
                        vista.mostrarMensaje(respuesta.mensaje)
                        vista.viajeCreado(viaje.folio)
                    }
                    respuesta.codigo == "TARIFA_CAMBIO" -> {
                        vista.mostrarMensaje(respuesta.mensaje)
                        cotizar()
                    }
                    respuesta.codigo == "SESION_VENCIDA" -> vista.sesionVencida()
                    else -> vista.mostrarMensaje(respuesta.mensaje)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pidiendo = false
                vista.mostrarPidiendo(false)
                vista.mostrarMensaje("Sin conexión. Revisa tu internet e inténtalo de nuevo.")
            }
        }
    }

    fun destruir() {
        scope.cancel()
    }
}
