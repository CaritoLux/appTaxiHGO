package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que se envía al PHP
data class dataRegistro(
    @SerializedName("tipo_cuenta") val tipoCuenta: String?,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("confirmar_contrasena") val confirmarContrasena: String,
    @SerializedName("acepta_aviso") val aceptaAviso: Boolean
)

// Lo que responde el PHP
data class dataCuentaCreada(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("correo") val correo: String = ""
)