package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Un lugar con sus coordenadas: el punto de recogida o un destino que se encontró
data class dataLugar(
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("detalle") val detalle: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0
) {
    fun textoCompleto(): String = if (detalle.isBlank()) nombre else "$nombre, $detalle"
}
