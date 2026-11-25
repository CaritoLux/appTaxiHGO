package mx.com.bandline.apptaxihgo.model

import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente
import java.util.Locale

// Busca lugares y direcciones en Photon (gratis, sin llave). No usa PHP.
class LugaresModel {

    // Lugares de Huejutla que coinciden con lo que escribió el pasajero
    suspend fun buscar(texto: String, lat: Double?, lng: Double?): dataRespuesta<List<dataLugar>> {
        val response = RetrofitCliente.apiService.buscar(texto, lat, lng, 10, clsUbicacion.BBOX_HUEJUTLA)
        val cuerpo = response.body()
        if (!response.isSuccessful || cuerpo == null) {
            return dataRespuesta(exito = false, mensaje = "No pudimos buscar lugares en este momento (${response.code()}). Inténtalo de nuevo.")
        }
        val lugares = cuerpo.features
            .mapNotNull { convertir(it, false) }
            .filter { clsUbicacion.dentroDeHuejutla(it.lat, it.lng) }
            .distinctBy { it.textoCompleto() }
        return dataRespuesta(exito = true, mensaje = "", datos = lugares)
    }

    // Dirección de un punto del mapa (para "Te recogemos en" y "Fijar el destino en el mapa")
    suspend fun direccion(lat: Double, lng: Double): dataLugar {
        val response = RetrofitCliente.apiService.direccion(lat, lng, 1)
        val primero = response.body()?.features?.firstOrNull()
        val lugar = if (primero != null) convertir(primero, true) else null
        // Se guardan las coordenadas del punto que eligió el pasajero, no las del lugar que encontró Photon
        return lugar?.copy(lat = lat, lng = lng) ?: puntoSinNombre(lat, lng)
    }

    fun puntoSinNombre(lat: Double, lng: Double): dataLugar {
        val coordenadas = String.format(Locale.US, "%.5f, %.5f", lat, lng)
        return dataLugar(nombre = "Punto en el mapa", detalle = coordenadas, lat = lat, lng = lng)
    }

    // Convierte un lugar de Photon en dataLugar: nombre corto y detalle (calle, colonia, ciudad)
    private fun convertir(lugar: dataPhotonLugar, esDireccion: Boolean): dataLugar? {
        val coordenadas = lugar.geometry.coordinates
        if (coordenadas.size < 2) return null
        val p = lugar.properties
        val calleNumero = listOfNotNull(p.street, p.housenumber).joinToString(" ").trim()
        val nombre = when {
            esDireccion && calleNumero.isNotEmpty() -> calleNumero
            !p.name.isNullOrBlank() -> p.name
            calleNumero.isNotEmpty() -> calleNumero
            !p.city.isNullOrBlank() -> p.city
            else -> return null
        }
        val colonia = p.district ?: p.locality
        val partes = if (esDireccion) listOf(colonia ?: p.city) else listOf(if (nombre == p.name) p.street else null, colonia, p.city)
        val detalle = partes.filterNotNull().filter { it.isNotBlank() && it != nombre }.distinct().joinToString(", ")
        return dataLugar(nombre = nombre, detalle = detalle, lat = coordenadas[1], lng = coordenadas[0])
    }
}
