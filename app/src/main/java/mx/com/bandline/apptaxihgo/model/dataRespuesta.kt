package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

data class dataRespuesta<T>(
    @SerializedName("exito") val exito: Boolean = false,
    @SerializedName("mensaje") val mensaje: String = "",
    @SerializedName("datos") val datos: T? = null
)

data class dataPerfil(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellido_paterno") val apellidoPaterno: String = "",
    @SerializedName("apellido_materno") val apellidoMaterno: String? = null,
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("calificacion") val calificacion: Double = 5.0
)