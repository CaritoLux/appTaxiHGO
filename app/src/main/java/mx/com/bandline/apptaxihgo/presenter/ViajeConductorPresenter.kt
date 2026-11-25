package mx.com.bandline.apptaxihgo.presenter

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import mx.com.bandline.apptaxihgo.model.ConductorModel
import mx.com.bandline.apptaxihgo.model.Resultado
import mx.com.bandline.apptaxihgo.model.clsParametrosApp
import mx.com.bandline.apptaxihgo.model.dataCancelar
import mx.com.bandline.apptaxihgo.model.dataViaje

/**
 * Viaje del conductor (Módulo 4).
 * - Consulta api_conductor.php?accion=viaje_activo cada 4 s (así se entera si el pasajero cancela).
 * - Manda su GPS cada 5 s (accion=ubicacion): el pasajero ve el taxi moverse en su mapa.
 * - LLEGUE manda la ubicación actual: la API exige estar a 150 m o menos del punto de recogida.
 * - Cancelar (C22): valida el motivo aquí y lo manda a api_conductor.php?accion=cancelar.
 */
class ViajeConductorPresenter(
    vista: ViajeConductorContract.View,
    private val modelo: ViajeConductorContract.Model = ConductorModel(),
    contexto: CoroutineContext = Dispatchers.Main,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : clsPresenterBase<ViajeConductorContract.View>(vista, contexto), ViajeConductorContract.Presenter {

    companion object {
        /** Los mismos motivos que acepta la API (MOTIVOS_CONDUCTOR en config/funciones.php). */
        val MOTIVOS = listOf("El pasajero no se presentó", "Problema con el vehículo", "Otro")
        const val NO_SE_PRESENTO = "El pasajero no se presentó"
    }

    private var id = 0
    private var ultimo: dataViaje? = null
    private var cancelando = false
    private var lat: Double? = null
    private var lng: Double? = null
    private var ultimoEnvio = 0L
    private var ocupado = false

    override fun iniciar(idViaje: Int) {
        id = idViaje
    }

    override fun reanudar() = repetir("viaje", 4000) { consultar() }

    override fun pausar() = detener("viaje")

    override fun actualizarAhora() {
        lanzar { consultar() }
    }

    private suspend fun consultar(): Boolean {
        if (ocupado) return true
        return when (val r = modelo.viaje(id)) {
            is Resultado.Exito -> {
                vista?.mostrarSinConexion(false)
                mostrar(r.datos)
            }
            is Resultado.Error -> when {
                r.sesionExpirada -> { manejarError(r); false }
                r.http == 403 || r.http == 404 -> { vista?.viajeCancelado(r.mensaje); false }
                else -> { vista?.mostrarSinConexion(true); true }
            }
        }
    }

    /** true = el viaje sigue activo. */
    private fun mostrar(v: dataViaje): Boolean {
        ultimo = v
        return when (v.estado) {
            "FINALIZADO" -> { vista?.viajeFinalizado(v); false }
            "CANCELADO" -> { vista?.viajeCancelado(v.mensaje); false }
            else -> { vista?.mostrarViaje(v); true }
        }
    }

    override fun ubicacionNueva(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
        val cada = clsParametrosApp.actuales.intervaloUbicacionS.coerceAtLeast(3) * 1000L
        if (reloj() - ultimoEnvio < cada) return
        ultimoEnvio = reloj()
        lanzar { modelo.enviarUbicacion(lat, lng) }
    }

    override fun llegue() = avanzar("LLEGUE")

    override fun iniciarViaje() = avanzar("INICIAR")

    override fun finalizar() = avanzar("FINALIZAR")

    private fun avanzar(accion: String) {
        if (ocupado) return
        ocupado = true
        lanzar {
            vista?.mostrarCargando(true)
            val r = modelo.avanzar(id, accion, lat, lng)
            vista?.mostrarCargando(false)
            ocupado = false
            when (r) {
                is Resultado.Exito -> mostrar(r.datos)
                is Resultado.Error -> when {
                    r.sesionExpirada -> manejarError(r)
                    r.http == 409 && r.mensaje.contains("cancel") -> vista?.viajeCancelado(r.mensaje)
                    else -> vista?.mostrarError(r.mensaje) // p. ej. "Aún estás a 320 m del punto de recogida."
                }
            }
        }
    }

    override fun pedirCancelar() {
        val v = ultimo ?: return
        if (!v.activo || !v.puedeCancelar) return
        val espera = if (v.estado == "EN_ORIGEN") (v.segundosParaNoSePresento ?: 0) else null
        vista?.mostrarMotivos(MOTIVOS, espera)
    }

    override fun cancelar(motivo: String?, comentario: String) {
        val errores = LinkedHashMap(clsValidaciones.erroresCancelacion(motivo, comentario, MOTIVOS))
        // "No se presentó" solo después de esperar (la API también lo revisa)
        if (motivo == NO_SE_PRESENTO && (ultimo?.estado != "EN_ORIGEN" || (ultimo?.segundosParaNoSePresento ?: 0) > 0)) {
            errores["motivo"] = "Espera 5 minutos desde tu llegada para usar esta opción."
        }
        vista?.mostrarErroresCancelar(errores)
        if (errores.isNotEmpty() || motivo == null || cancelando) return
        cancelando = true
        ocupado = true
        lanzar {
            vista?.mostrarCargandoCancelar(true)
            val r = modelo.cancelar(dataCancelar(id, motivo, comentario.trim().ifBlank { null }))
            vista?.mostrarCargandoCancelar(false)
            cancelando = false
            ocupado = false
            when (r) {
                is Resultado.Exito -> {
                    detener("viaje")
                    vista?.cancelasteViaje(r.mensaje)
                }
                is Resultado.Error -> when {
                    r.sesionExpirada -> manejarError(r)
                    r.errores.isNotEmpty() -> vista?.mostrarErroresCancelar(r.errores)
                    else -> vista?.mostrarErroresCancelar(mapOf("motivo" to r.mensaje))
                }
            }
        }

    }
    override fun destruir() {
        detener("viaje")

        ultimo = null
        lat = null
        lng = null

        ocupado = false
        cancelando = false
    }
}
