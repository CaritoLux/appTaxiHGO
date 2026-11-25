package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que responde cotizar_viaje.php: ruta, distancia, minutos y tarifa estimada
data class dataCotizacion(
    @SerializedName("origen_direccion") val origenDireccion: String = "",
    @SerializedName("origen_lat") val origenLat: Double = 0.0,
    @SerializedName("origen_lng") val origenLng: Double = 0.0,
    @SerializedName("destino_direccion") val destinoDireccion: String = "",
    @SerializedName("destino_lat") val destinoLat: Double = 0.0,
    @SerializedName("destino_lng") val destinoLng: Double = 0.0,
    @SerializedName("distancia_m") val distanciaM: Int = 0,
    @SerializedName("duracion_s") val duracionS: Int = 0,
    @SerializedName("distancia_km") val distanciaKm: String = "",
    @SerializedName("duracion_min") val duracionMin: Int = 0,
    @SerializedName("tarifa") val tarifa: Int = 0,
    @SerializedName("ruta_por_calles") val rutaPorCalles: Boolean = false,
    // Puntos de la ruta: [[longitud, latitud], ...]
    @SerializedName("ruta") val ruta: List<List<Double>> = emptyList()
)
