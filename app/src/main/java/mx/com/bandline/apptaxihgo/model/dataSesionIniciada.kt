package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que responde el PHP (token + datos del usuario)
data class dataSesionIniciada(
    @SerializedName("token") val token: String = "",
    @SerializedName("usuario") val usuario: dataUsuarioSesion = dataUsuarioSesion()
)