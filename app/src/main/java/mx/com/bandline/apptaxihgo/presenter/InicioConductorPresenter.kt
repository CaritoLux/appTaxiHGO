package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.Dispatchers
import mx.com.bandline.apptaxihgo.model.ConductorModel
import mx.com.bandline.apptaxihgo.model.dataOferta
import mx.com.bandline.apptaxihgo.model.clsParametrosApp
import mx.com.bandline.apptaxihgo.model.Resultado
import kotlin.coroutines.CoroutineContext

/**
 * Inicio del conductor (Módulo 4).
 * Disponible: manda su GPS cada 5 s (api_conductor.php?accion=ubicacion) y consulta api_conductor.php?accion=ofertas cada 3 s.
 * Cuando llega una solicitud tiene 20 s para aceptar (si no responde 3 seguidas, la API lo desconecta).
 */
class InicioConductorPresenter(
    vista: InicioConductorContract.View,
    private val modelo: InicioConductorContract.Model = ConductorModel(),
    contexto: CoroutineContext = Dispatchers.Main,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : clsPresenterBase<InicioConductorContract.View>(vista, contexto), InicioConductorContract.Presenter {

    companion object {
        const val VENCIDA = "El tiempo para aceptar terminó."
        const val CANCELADA = "El pasajero canceló la solicitud."
    }

    private var disponible = false
    private var oferta: dataOferta? = null
    private var venceOferta = 0L
    private var respondiendo = false
    private var lat: Double? = null
    private var lng: Double? = null
    private var ultimoEnvio = 0L

    override fun estaDisponible() = disponible

    override fun cargarPanel() {
        lanzar {
            when (val r = modelo.panel()) {
                is Resultado.Exito -> {
                    val p = r.datos
                    vista?.mostrarPanel(p)
                    p.aviso?.let { vista?.mostrarAvisoConductor(it) }
                    val activo = p.viajeActivo
                    if (activo != null) {
                        vista?.irAViaje(activo)
                        return@lanzar
                    }
                    cambiarDisponibilidad(p.disponibilidad == "DISPONIBLE")
                }
                is Resultado.Error -> manejarError(r)
            }
        }
    }

    private fun cambiarDisponibilidad(nueva: Boolean) {
        disponible = nueva
        vista?.mostrarDisponibilidad(nueva)
        if (nueva) {
            val cada = clsParametrosApp.actuales.intervaloConsultaS.coerceAtLeast(2) * 1000L
            repetir("ofertas", cada) { consultarOfertas() }
        } else {
            detener("ofertas")
            if (oferta != null) {
                oferta = null
                vista?.ofertaTerminada(null)
            }
        }
    }

    override fun conectar(lat: Double?, lng: Double?) {
        if (lat != null && lng != null) { this.lat = lat; this.lng = lng }
        lanzar {
            vista?.mostrarCargando(true)
            val r = modelo.disponibilidad(true, this@InicioConductorPresenter.lat, this@InicioConductorPresenter.lng)
            vista?.mostrarCargando(false)
            when (r) {
                is Resultado.Exito -> {
                    ultimoEnvio = reloj()
                    cambiarDisponibilidad(true)
                }
                is Resultado.Error -> when {
                    r.sesionExpirada -> manejarError(r)
                    r.http == 422 -> vista?.ubicacionNecesaria(r.mensaje)
                    r.http == 409 && r.mensaje.contains("venc") -> vista?.documentosVencidos(r.mensaje)
                    r.http == 409 -> cargarPanel() // ya tiene un viaje en curso
                    else -> vista?.mostrarError(r.mensaje)
                }
            }
        }
    }

    override fun desconectar() {
        lanzar {
            vista?.mostrarCargando(true)
            val r = modelo.disponibilidad(false, null, null)
            vista?.mostrarCargando(false)
            when (r) {
                is Resultado.Exito -> cambiarDisponibilidad(false)
                is Resultado.Error -> manejarError(r)
            }
        }
    }

    /** Cada lectura del GPS: se guarda y se manda a la API como máximo cada intervalo_ubicacion_s. */
    override fun ubicacionNueva(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
        val cada = clsParametrosApp.actuales.intervaloUbicacionS.coerceAtLeast(3) * 1000L
        if (!disponible || reloj() - ultimoEnvio < cada) return
        ultimoEnvio = reloj()
        lanzar {
            val r = modelo.enviarUbicacion(lat, lng)
            if (r is Resultado.Exito && r.datos.disponibilidad == "DESCONECTADO" && disponible) cambiarDisponibilidad(false)
        }
    }

    private suspend fun consultarOfertas(): Boolean {
        return when (val r = modelo.ofertas()) {
            is Resultado.Exito -> {
                val d = r.datos
                d.aviso?.let { vista?.mostrarAvisoConductor(it) }
                val activo = d.viajeActivo
                when {
                    activo != null -> { vista?.irAViaje(activo); false }
                    d.disponibilidad != "DISPONIBLE" -> { cambiarDisponibilidad(false); false }
                    else -> {
                        val nueva = d.oferta
                        val actual = oferta
                        if (nueva != null && nueva.idOferta != actual?.idOferta) {
                            oferta = nueva
                            venceOferta = reloj() + nueva.segundosRestantes * 1000L
                            vista?.mostrarOferta(nueva)
                        } else if (nueva == null && actual != null && !respondiendo) {
                            oferta = null
                            vista?.ofertaTerminada(if (reloj() >= venceOferta) VENCIDA else CANCELADA)
                        }
                        true
                    }
                }
            }
            is Resultado.Error -> {
                if (r.sesionExpirada) { manejarError(r); false } else true
            }
        }
    }

    override fun responder(aceptar: Boolean) {
        val actual = oferta ?: return
        respondiendo = true
        lanzar {
            vista?.mostrarCargando(true)
            val r = modelo.responderOferta(actual.idOferta, aceptar)
            vista?.mostrarCargando(false)
            respondiendo = false
            oferta = null
            when (r) {
                is Resultado.Exito -> {
                    val viaje = r.datos
                    if (aceptar && viaje != null) vista?.irAViaje(viaje.idViaje) else vista?.ofertaTerminada(null)
                }
                is Resultado.Error -> if (r.sesionExpirada) manejarError(r) else vista?.ofertaTerminada(r.mensaje)
            }
        }
    }

    override fun ofertaVencida() {
        if (oferta == null || respondiendo) return
        oferta = null
        vista?.ofertaTerminada(VENCIDA)
    }

    override fun pausar() = detenerTodo()
}
