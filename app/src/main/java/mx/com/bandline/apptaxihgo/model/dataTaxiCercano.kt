package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Un taxi disponible en el mapa
data class dataTaxiCercano(
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("distancia_m") val distanciaM: Int = 0,
    @SerializedName("minutos") val minutos: Int = 0
)
