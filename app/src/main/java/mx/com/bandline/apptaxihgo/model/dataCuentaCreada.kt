package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que responde el PHP
data class dataCuentaCreada(
    @SerializedName("id_usuario") val idUsuario: Int = 0,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("correo") val correo: String = ""
)