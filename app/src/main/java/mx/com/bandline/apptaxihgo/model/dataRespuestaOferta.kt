package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataRespuestaOferta(
    @SerializedName("id_oferta") val idOferta: Int,
    @SerializedName("accion") val accion: String

)
