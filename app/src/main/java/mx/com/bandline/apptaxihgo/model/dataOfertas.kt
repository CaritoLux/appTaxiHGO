package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataOfertas(
    @SerializedName("disponibilidad") val disponibilidad: String = "",
    @SerializedName("aviso") val aviso: String? = null,
    @SerializedName("oferta") val oferta: dataOferta? = null,
    @SerializedName("viaje_activo") val viajeActivo: Int? = null
)
