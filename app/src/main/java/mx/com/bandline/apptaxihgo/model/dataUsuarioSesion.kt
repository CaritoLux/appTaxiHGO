package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

data class dataUsuarioSesion(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellido_paterno") val apellidoPaterno: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("estado_revision") val estadoRevision: String? = null
)