package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName

data class dataCancelar(
    @SerializedName("id_viaje") val idViaje: Int,
    @SerializedName("motivo") val motivo: String,
    @SerializedName("comentario") val comentario: String
)
