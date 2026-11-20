package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente

class CuentaModel {

    suspend fun obtenerPerfil(token: String): dataRespuesta<dataPerfil> {
        val response = RetrofitCliente.apiService.perfil("Bearer $token")

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            return try {
                Gson().fromJson(errorJson, dataRespuesta::class.java) as dataRespuesta<dataPerfil>
            } catch (e: Exception) {
                dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            }
        }

        return dataRespuesta(exito = false, mensaje = "Error de conexión (${response.code()})")
    }
}