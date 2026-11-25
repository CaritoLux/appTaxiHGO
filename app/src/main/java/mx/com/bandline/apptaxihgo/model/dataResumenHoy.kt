package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataResumenHoy(
    @SerializedName("viajes") val viajes: Int = 0,
    @SerializedName("total") val total: Double = 0.0

)
