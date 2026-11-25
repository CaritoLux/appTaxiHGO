package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataHistorial(
    @SerializedName("periodo") val periodo: String = "30",
    @SerializedName("pagina") val pagina: Int = 1,
    @SerializedName("hay_mas") val hayMas: Boolean = false,
    @SerializedName("viajes") val viajes: List<dataViajeResumen> = emptyList(),
    @SerializedName("totales") val totales: dataTotales? = null
)
