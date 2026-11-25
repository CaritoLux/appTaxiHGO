package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataTotales(
    @SerializedName("finalizados") val finalizados: Int = 0,
    @SerializedName("total_tarifas") val totalTarifas: Int = 0
)
