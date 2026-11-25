package mx.com.bandline.apptaxihgo.model
import com.google.gson.Gson
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitViaje
import retrofit2.Response

class ViajeModel {

    // Código HTTP de la última respuesta (200, 401, 404...). El presentador lo revisa
    // para saber si la sesión expiró (401).
    var ultimoCodigo = 0

    suspend fun estado(token: String, idViaje: Int): dataRespuesta<dataViaje> {
        val response = RetrofitViaje.api.estado("Bearer $token", idViaje)
        return leer(response)
    }

    suspend fun cancelar(token: String, idViaje: Int, motivo: String, comentario: String): dataRespuesta<dataViaje> {
        val response = RetrofitViaje.api.cancelar("Bearer $token", dataCancelar(idViaje, motivo, comentario))
        return leer(response)
    }

    suspend fun emergencia(token: String, idViaje: Int): dataRespuesta<dataFolio> {
        val response = RetrofitViaje.api.emergencia("Bearer $token", dataEmergencia(idViaje))
        return leer(response)
    }

    suspend fun historial(token: String, periodo: String, pagina: Int): dataRespuesta<dataHistorial> {
        val response = RetrofitViaje.api.historial("Bearer $token", periodo, pagina)
        return leer(response)
    }

    suspend fun detalle(token: String, idViaje: Int): dataRespuesta<dataViaje> {
        val response = RetrofitViaje.api.detalle("Bearer $token", idViaje)
        return leer(response)
    }

    // Lee cualquier respuesta: si salió bien regresa el cuerpo; si no, saca el mensaje del JSON de error.
    private fun <T> leer(response: Response<dataRespuesta<T>>): dataRespuesta<T> {
        ultimoCodigo = response.code()
        val cuerpo = response.body()
        if (response.isSuccessful && cuerpo != null) {
            return cuerpo
        }

        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            return try {
                val error = Gson().fromJson(errorJson, dataRespuesta::class.java)
                dataRespuesta(exito = false, mensaje = error.mensaje)
            } catch (e: Exception) {
                dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            }
        }

        return dataRespuesta(exito = false, mensaje = "Error de conexión (${response.code()})")
    }
}