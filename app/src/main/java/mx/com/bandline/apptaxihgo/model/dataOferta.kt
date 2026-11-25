package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName
data class dataOferta(
    @SerializedName("id_oferta") val idOferta: Int = 0,
    @SerializedName("id_viaje") val idViaje: Int = 0,
    @SerializedName("segundos_restantes") val segundosRestantes: Int = 0,
    @SerializedName("distancia_a_origen_km") val distanciaAOrigenKm: Double = 0.0,
    @SerializedName("minutos_a_origen") val minutosAOrigen: Int = 0,
    @SerializedName("origen") val origen: dataPunto = dataPunto(),
    @SerializedName("destino") val destino: dataPunto = dataPunto(),
    @SerializedName("referencia") val referencia: String? = null,
    @SerializedName("distancia_viaje_km") val distanciaViajeKm: Double = 0.0,
    @SerializedName("tarifa_estimada") val tarifaEstimada: Double = 0.0,
    @SerializedName("pasajero") val pasajero: String = "",
    @SerializedName("calificacion_pasajero") val calificacionPasajero: Double? = null

)
