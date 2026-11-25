package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Un viaje como lo guarda la tabla viajes (crear_viaje.php regresa el viaje creado)
data class dataViaje(
    @SerializedName("id_viaje") val idViaje: Int = 0,
    @SerializedName("folio") val folio: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("origen_direccion") val origenDireccion: String = "",
    @SerializedName("origen_lat") val origenLat: Double = 0.0,
    @SerializedName("origen_lng") val origenLng: Double = 0.0,
    @SerializedName("destino_direccion") val destinoDireccion: String = "",
    @SerializedName("destino_lat") val destinoLat: Double = 0.0,
    @SerializedName("destino_lng") val destinoLng: Double = 0.0,
    @SerializedName("referencia") val referencia: String? = null,
    @SerializedName("distancia_m") val distanciaM: Int = 0,
    @SerializedName("duracion_s") val duracionS: Int = 0,
    @SerializedName("distancia_km") val distanciaKm: String = "",
    @SerializedName("duracion_min") val duracionMin: Int = 0,
    @SerializedName("tarifa_estimada") val tarifaEstimada: Double = 0.0,
    @SerializedName("ruta") val ruta: List<List<Double>> = emptyList(),
    @SerializedName("fecha_solicitud") val fechaSolicitud: String = ""
)
