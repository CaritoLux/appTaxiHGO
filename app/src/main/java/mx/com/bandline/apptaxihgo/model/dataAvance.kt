package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataAvance(
    @SerializedName("id_viaje") val idViaje: Int,
    @SerializedName("accion") val accion: String,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null

)
