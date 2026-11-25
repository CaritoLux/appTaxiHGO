package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataUbicacion(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
