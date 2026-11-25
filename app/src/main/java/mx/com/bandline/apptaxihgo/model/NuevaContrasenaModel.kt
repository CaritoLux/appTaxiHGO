package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente

class NuevaContrasenaModel {
    suspend fun cambiarContrasena(correo: String, codigo: String, contrasena: String): dataRespuesta<Any> {
        val datos = mapOf("correo" to correo, "codigo" to codigo, "contrasena" to contrasena)
        val response = RetrofitCliente.apiService.cambiarContrasena(datos)
        if (response.isSuccessful && response.body() != null) return response.body()!!
        return dataRespuesta(exito = false, mensaje = "Error de conexión")
    }
}