package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName

data class dataEmergencia(
    @SerializedName("id_viaje") val idViaje: Int,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null
)
