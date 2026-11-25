package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName

data class dataViajeResumen(
    @SerializedName("id_viaje") val idViaje: Int = 0,
    @SerializedName("folio") val folio: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("estado_texto") val estadoTexto: String = "",
    @SerializedName("origen") val origen: String = "",
    @SerializedName("destino") val destino: String = "",
    @SerializedName("tarifa") val tarifa: Int = 0,
    @SerializedName("fecha") val fecha: String = ""
)
