package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que c envia al PHP
data class dataLogin(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("plataforma") val plataforma: String = "APP"
)

// Lo que responde el PHP (token + datos del usuario)
data class dataSesionIniciada(
    @SerializedName("token") val token: String = "",
    @SerializedName("usuario") val usuario: dataUsuarioSesion = dataUsuarioSesion()
)

data class dataUsuarioSesion(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellido_paterno") val apellidoPaterno: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("estado_revision") val estadoRevision: String? = null
)