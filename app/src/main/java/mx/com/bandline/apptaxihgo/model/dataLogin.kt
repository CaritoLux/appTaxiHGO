package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que c envia al PHP
data class dataLogin(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("plataforma") val plataforma: String = "APP"
)