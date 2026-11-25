package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que se envía a cotizar_viaje.php y a crear_viaje.php
data class dataPedirViaje(
    @SerializedName("origen_lat") val origenLat: Double,
    @SerializedName("origen_lng") val origenLng: Double,
    @SerializedName("origen_direccion") val origenDireccion: String,
    @SerializedName("destino_lat") val destinoLat: Double,
    @SerializedName("destino_lng") val destinoLng: Double,
    @SerializedName("destino_direccion") val destinoDireccion: String,
    @SerializedName("referencia") val referencia: String = "",
    // La tarifa que vio el pasajero; si cambió, el PHP no crea el viaje y avisa
    @SerializedName("tarifa_cotizada") val tarifaCotizada: Int? = null
)
