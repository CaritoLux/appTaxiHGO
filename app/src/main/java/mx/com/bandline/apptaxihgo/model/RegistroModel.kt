package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente

class RegistroModel {

    suspend fun registrarUsuario(datos: dataRegistro): dataRespuesta<dataCuentaCreada> {
        val response = RetrofitCliente.apiService.registrar(datos)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        // Si el PHP respondió 400 o 409 (correo o teléfono ya registrado), sacamos el mensaje del JSON
        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            return try {
                Gson().fromJson(errorJson, dataRespuesta::class.java) as dataRespuesta<dataCuentaCreada>
            } catch (e: Exception) {
                dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            }
        }

        return dataRespuesta(exito = false, mensaje = "Error de conexión (${response.code()})")
    }
}