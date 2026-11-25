package mx.com.bandline.apptaxihgo.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente
import retrofit2.Response

// Pedir viaje: taxis cercanos, cotización y crear el viaje (PHP del hosting)
class SolicitudModel {

    suspend fun taxisCercanos(token: String, lat: Double, lng: Double): dataRespuesta<dataTaxisCercanos> {
        val response = RetrofitCliente.apiService.taxisCercanos("Bearer $token", lat, lng)
        return leerRespuesta(response, dataTaxisCercanos::class.java)
    }

    suspend fun cotizar(token: String, datos: dataPedirViaje): dataRespuesta<dataCotizacion> {
        val response = RetrofitCliente.apiService.cotizar("Bearer $token", datos)
        return leerRespuesta(response, dataCotizacion::class.java)
    }

    suspend fun crear(token: String, datos: dataPedirViaje): dataRespuesta<dataViaje> {
        val response = RetrofitCliente.apiService.crear("Bearer $token", datos)
        return leerRespuesta(response, dataViaje::class.java)
    }

    // Si el PHP respondió con error (401, 409, 422...), el JSON del error se lee con el mismo molde
    private fun <T> leerRespuesta(response: Response<dataRespuesta<T>>, clase: Class<T>): dataRespuesta<T> {
        val cuerpo = response.body()
        if (response.isSuccessful && cuerpo != null) {
            return cuerpo
        }
        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            return try {
                val tipo = TypeToken.getParameterized(dataRespuesta::class.java, clase).type
                Gson().fromJson<dataRespuesta<T>>(errorJson, tipo) ?: dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            } catch (e: Exception) {
                dataRespuesta(exito = false, mensaje = "Error al leer respuesta del servidor")
            }
        }
        return dataRespuesta(exito = false, mensaje = "Error de conexión (${response.code()})")
    }
}
