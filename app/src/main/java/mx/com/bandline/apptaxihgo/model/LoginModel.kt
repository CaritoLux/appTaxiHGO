package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente

class LoginModel {

    suspend fun iniciarSesion(correo: String, contrasena: String): dataRespuesta<dataSesionIniciada> {
        val datos = dataLogin(correo = correo, contrasena = contrasena)
        val response = RetrofitCliente.apiService.iniciarSesion(datos)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        // Si el PHP respondió 401, 403 o 423 (cuenta bloqueada/suspendida), leemos el mensaje de error del JSON
        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            return try {
                Gson().fromJson(errorJson, dataRespuesta::class.java) as dataRespuesta<dataSesionIniciada>
            } catch (e: Exception) {
                dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            }
        }

        return dataRespuesta(exito = false, mensaje = "Error de conexión (${response.code()})")
    }
}