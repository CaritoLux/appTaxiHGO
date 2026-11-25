package mx.com.bandline.apptaxihgo.presenter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.LugaresModel
import mx.com.bandline.apptaxihgo.model.clsUbicacion
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.view.ActivityElegirDestino

// Elegir destino: busca lugares en Photon y revisa que estén en Huejutla
class ElegirDestinoPresenter(private val vista: ActivityElegirDestino) {

    private val lugares = LugaresModel()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var busqueda: Job? = null
    private var busquedaMapa: Job? = null
    private var origen = dataLugar()
    private var puntoMapa: dataLugar? = null

    fun iniciar(origen: dataLugar) {
        this.origen = origen
        vista.mostrarOrigen(origen.textoCompleto())
    }

    // Se busca cuando el pasajero deja de escribir (pausa de 0.4 s), no en cada letra
    fun textoCambiado(texto: String) {
        busqueda?.cancel()
        val buscado = texto.trim()
        if (buscado.length < 3) {
            vista.mostrarCargando(false)
            vista.mostrarLugares(emptyList())
            vista.mostrarEstado(null)
            return
        }
        busqueda = scope.launch {
            delay(400)
            vista.mostrarCargando(true)
            try {
                val respuesta = lugares.buscar(buscado, origen.lat, origen.lng)
                vista.mostrarCargando(false)
                val lista = respuesta.datos ?: emptyList()
                vista.mostrarLugares(lista)
                if (!respuesta.exito) {
                    vista.mostrarEstado(respuesta.mensaje)
                } else if (lista.isEmpty()) {
                    vista.mostrarEstado("No encontramos \"$buscado\" en Huejutla. Prueba con otro nombre o fija el destino en el mapa.")
                } else {
                    vista.mostrarEstado(null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                vista.mostrarEstado("Sin conexión. Revisa tu internet e inténtalo de nuevo.")
            }
        }
    }

    fun lugarElegido(lugar: dataLugar) {
        if (!clsUbicacion.dentroDeHuejutla(lugar.lat, lugar.lng)) {
            vista.mostrarError("Ese lugar está fuera de Huejutla. Por ahora el servicio solo funciona dentro del municipio.")
            return
        }
        if (clsUbicacion.distanciaMetros(origen.lat, origen.lng, lugar.lat, lugar.lng) < 150f) {
            vista.mostrarError("El destino está muy cerca de tu punto de recogida. Elige otro destino.")
            return
        }
        vista.abrirConfirmar(origen, lugar)
    }

    // "Fijar el destino en el mapa": el mapa empieza en el punto de recogida
    fun abrirMapa() {
        puntoMapa = null
        vista.mostrarDireccionMapa("Buscando la dirección…", false)
        vista.mostrarModoMapa(origen.lat, origen.lng)
    }

    fun mapaDetenido(lat: Double, lng: Double) {
        puntoMapa = null
        vista.mostrarDireccionMapa("Buscando la dirección…", false)
        busquedaMapa?.cancel()
        busquedaMapa = scope.launch {
            delay(300)
            val lugar = try {
                lugares.direccion(lat, lng)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lugares.puntoSinNombre(lat, lng)
            }
            puntoMapa = lugar
            vista.mostrarDireccionMapa(lugar.textoCompleto(), true)
        }
    }

    fun confirmarPuntoMapa() {
        val lugar = puntoMapa ?: return
        lugarElegido(lugar)
    }

    fun destruir() {
        scope.cancel()
    }
}
