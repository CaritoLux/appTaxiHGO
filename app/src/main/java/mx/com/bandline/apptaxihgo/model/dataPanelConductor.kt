package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataPanelConductor(
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("estado_revision") val estadoRevision: String = "",
    @SerializedName("disponibilidad") val disponibilidad: String = "DESCONECTADO",
    @SerializedName("aviso") val aviso: String? = null,
    @SerializedName("calificacion") val calificacion: dataCalificacion = dataCalificacion(),
    @SerializedName("vehiculo") val vehiculo: dataVehiculoResumen? = null,
    @SerializedName("viaje_activo") val viajeActivo: Int? = null,
    @SerializedName("hoy") val hoy: dataResumenHoy = dataResumenHoy()
)
