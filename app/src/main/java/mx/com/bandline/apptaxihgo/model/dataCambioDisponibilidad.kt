package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataCambioDisponibilidad(
    @SerializedName("disponible") val disponible: Boolean,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null

)
