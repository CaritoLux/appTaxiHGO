package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente

class RecuperarModel {
    suspend fun solicitarCodigo(correo: String): dataRespuesta<Any> {
        val datos = mapOf("correo" to correo)
        val response = RetrofitCliente.apiService.solicitarCodigo(datos)
        if (response.isSuccessful && response.body() != null) return response.body()!!
        return dataRespuesta(exito = false, mensaje = "Error de conexión")
    }
}