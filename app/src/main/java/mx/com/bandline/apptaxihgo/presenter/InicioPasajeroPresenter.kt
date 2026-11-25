package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mx.com.bandline.apptaxihgo.model.CuentaModel
import mx.com.bandline.apptaxihgo.model.LugaresModel
import mx.com.bandline.apptaxihgo.model.SolicitudModel
import mx.com.bandline.apptaxihgo.model.clsUbicacion
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.view.ActivityInicioPasajero

// Inicio del pasajero: lee el GPS, pone la dirección de recogida y busca taxis cada 10 s
class InicioPasajeroPresenter(private val vista: ActivityInicioPasajero) {

    companion object {
        const val AVISO_PERMISO = 1
        const val AVISO_GPS = 2
        const val AVISO_SIN_SENAL = 3
        const val AVISO_FUERA_DE_ZONA = 4
        private const val CADA_CUANTO_MS = 10000L
    }

    private val modelo = SolicitudModel()
    private val lugares = LugaresModel()
    private val cuenta = CuentaModel()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var token = ""
    private var buscarTaxis: Job? = null
    private var buscarDireccion: Job? = null
    private var recogida: dataLugar? = null
    private var puntoListo = false
    private var leyendoGps = false
    private var permisoPedido = false
    private var dentroZona = true

    fun iniciar(token: String) {
        this.token = token
        if (token.isEmpty()) {
            vista.sesionVencida()
            return
        }
        // El nombre para "Hola, Ana" viene de obtener_perfil.php (Módulo 1)
        scope.launch {
            try {
                val respuesta = cuenta.obtenerPerfil(token)
                if (respuesta.exito && respuesta.datos != null) {
                    // Solo el primer nombre, con mayúscula: "Hola, Ana"
                    val primerNombre = respuesta.datos.nombre.trim().substringBefore(" ")
                    if (primerNombre.isNotEmpty()) vista.mostrarSaludo(primerNombre.replaceFirstChar { it.uppercase() })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin nombre se queda "Hola"
            }
        }
    }

    // Al volver a la pantalla: busca taxis cada 10 s y, si falta, lee el GPS
    fun reanudar() {
        buscarTaxis?.cancel()
        buscarTaxis = scope.launch {
            while (isActive) {
                val punto = recogida
                if (punto != null) cargarTaxis(punto.lat, punto.lng)
                delay(CADA_CUANTO_MS)
            }
        }
        if (puntoListo || leyendoGps) return
        if (!clsUbicacion.tienePermiso(vista)) {
            if (!permisoPedido) {
                permisoPedido = true
                vista.pedirPermisoUbicacion()
            } else {
                vista.mostrarAviso("Permite el acceso a tu ubicación para saber dónde recogerte, o mueve el mapa hasta tu punto de recogida.", "Permitir", AVISO_PERMISO)
            }
            return
        }
        leerUbicacion()
    }

    fun pausar() {
        buscarTaxis?.cancel()
    }

    fun permisoRespondido(concedido: Boolean) {
        if (concedido) {
            leerUbicacion()
        } else {
            vista.mostrarAviso("Permite el acceso a tu ubicación para saber dónde recogerte, o mueve el mapa hasta tu punto de recogida.", "Permitir", AVISO_PERMISO)
        }
    }

    // Botón "mi ubicación" y lectura inicial
    fun leerUbicacion() {
        if (!clsUbicacion.tienePermiso(vista)) {
            vista.pedirPermisoUbicacion()
            return
        }
        if (!clsUbicacion.gpsEncendido(vista)) {
            vista.mostrarAviso("Tu ubicación está apagada. Actívala para saber dónde recogerte.", "Activar", AVISO_GPS)
            return
        }
        if (leyendoGps) return
        leyendoGps = true
        vista.ocultarAviso()
        if (!puntoListo) vista.mostrarDireccion("Buscando tu ubicación…")
        scope.launch {
            val ubicacion = withTimeoutOrNull(15000L) { clsUbicacion.obtener(vista) }
            leyendoGps = false
            if (ubicacion == null) {
                vista.mostrarAviso("No pudimos leer tu ubicación. Sal a un lugar abierto o mueve el mapa hasta tu punto de recogida.", "Reintentar", AVISO_SIN_SENAL)
                return@launch
            }
            puntoListo = true
            vista.centrarMapa(ubicacion.latitude, ubicacion.longitude)
        }
    }

    // El pasajero movió el mapa con el dedo: el centro del mapa pasa a ser su punto de recogida
    fun mapaMovidoPorUsuario() {
        puntoListo = true
    }

    // El mapa dejó de moverse: se busca la dirección del centro y los taxis cercanos
    fun mapaDetenido(lat: Double, lng: Double) {
        if (!puntoListo) return
        val anterior = recogida
        if (anterior != null && clsUbicacion.distanciaMetros(anterior.lat, anterior.lng, lat, lng) < 10f) return
        recogida = lugares.puntoSinNombre(lat, lng)
        vista.mostrarDireccion("Buscando la dirección…")
        buscarDireccion?.cancel()
        buscarDireccion = scope.launch {
            launch { cargarTaxis(lat, lng) }
            val lugar = try {
                lugares.direccion(lat, lng)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lugares.puntoSinNombre(lat, lng)
            }
            recogida = lugar
            vista.mostrarDireccion(lugar.textoCompleto())
        }
    }

    private suspend fun cargarTaxis(lat: Double, lng: Double) {
        try {
            val respuesta = modelo.taxisCercanos(token, lat, lng)
            if (respuesta.codigo == "SESION_VENCIDA") {
                vista.sesionVencida()
                return
            }
            val datos = respuesta.datos
            if (respuesta.exito && datos != null) {
                vista.mostrarTaxis(datos.taxis)
                if (dentroZona != datos.dentroZona) {
                    dentroZona = datos.dentroZona
                    if (dentroZona) vista.ocultarAviso()
                    else vista.mostrarAviso("Este punto está fuera de Huejutla. Por ahora el servicio solo funciona dentro del municipio.", null, AVISO_FUERA_DE_ZONA)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin internet: se vuelve a intentar en 10 s
        }
    }

    // "¿A dónde vas?"
    fun elegirDestino() {
        val punto = recogida
        when {
            punto == null -> vista.mostrarError("Todavía no sabemos dónde recogerte. Activa tu ubicación o mueve el mapa.")
            !dentroZona -> vista.mostrarError("Tu punto de recogida está fuera de Huejutla.")
            else -> vista.abrirElegirDestino(punto)
        }
    }

    fun destruir() {
        scope.cancel()
    }
}
