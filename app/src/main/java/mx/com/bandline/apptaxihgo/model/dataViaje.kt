package mx.com.bandline.apptaxihgo.model
import com.google.gson.annotations.SerializedName

// Un viaje como lo guarda la tabla viajes (crear_viaje.php regresa el viaje creado)

data class dataViaje(@SerializedName("origen_direccion") val origenDireccion: String = "",
    @SerializedName("origen_lat") val origenLat: Double = 0.0,
    @SerializedName("origen_lng") val origenLng: Double = 0.0,
    @SerializedName("destino_direccion") val destinoDireccion: String = "",
    @SerializedName("destino_lat") val destinoLat: Double = 0.0,
    @SerializedName("destino_lng") val destinoLng: Double = 0.0,
    @SerializedName("referencia") val referencia: String? = null, //Ocupa viajepasajero
    @SerializedName("distancia_m") val distanciaM: Int = 0,
    @SerializedName("duracion_s") val duracionS: Int = 0,
    @SerializedName("distancia_km") val distanciaKm: String = "",  //Ocupa viajepasajero
    @SerializedName("duracion_min") val duracionMin: Int = 0,  //Ocupa viajepasajero
    @SerializedName("tarifa_estimada") val tarifaEstimada: Double = 0.0,
    @SerializedName("ruta") val ruta: List<List<Double>> = emptyList(),
    @SerializedName("fecha_solicitud") val fechaSolicitud: String = "",  //Ocupa viajepasajero
//Apartado del modulo de viajes de pasajeros
    @SerializedName("id_viaje") val idViaje: Int = 0,
    @SerializedName("folio") val folio: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("estado_texto") val estadoTexto: String = "",
    @SerializedName("mensaje") val mensaje: String = "",
    @SerializedName("origen") val origen: dataPunto = dataPunto(),
    @SerializedName("destino") val destino: dataPunto = dataPunto(),
    @SerializedName("tarifa") val tarifa: Int = 0,
    @SerializedName("eta_min") val etaMin: Int? = null,
    @SerializedName("conductor") val conductor: dataConductorViaje? = null,
    @SerializedName("pasajero") val pasajero: dataPasajeroViaje? = null,
    @SerializedName("cancelado_por") val canceladoPor: String? = null,
    @SerializedName("motivo_cancelacion") val motivoCancelacion: String? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String? = null,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    @SerializedName("activo") val activo: Boolean = false,
    @SerializedName("puede_cancelar") val puedeCancelar: Boolean = false,
    @SerializedName("puede_calificar") val puedeCalificar: Boolean = false,
    @SerializedName("puede_reportar") val puedeReportar: Boolean = false,
    @SerializedName("mi_calificacion") val miCalificacion: Int? = null
)

data class dataPunto(
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0
)

data class dataConductorViaje(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("numero_economico") val numeroEconomico: String? = null,
    @SerializedName("placas") val placas: String? = null,
    @SerializedName("vehiculo") val vehiculo: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("calificacion") val calificacion: Double = 5.0,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null
)
data class dataPasajeroViaje(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("calificacion") val calificacion: Double = 5.0
)
