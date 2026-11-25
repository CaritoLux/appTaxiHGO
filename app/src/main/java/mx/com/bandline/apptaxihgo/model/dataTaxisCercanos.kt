package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que responde obtener_taxis_cercanos.php
data class dataTaxisCercanos(
    @SerializedName("dentro_zona") val dentroZona: Boolean = true,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("minutos_mas_cercano") val minutosMasCercano: Int? = null,
    @SerializedName("taxis") val taxis: List<dataTaxiCercano> = emptyList()
)
