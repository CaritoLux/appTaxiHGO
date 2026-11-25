package mx.com.bandline.apptaxihgo.interfaceapi
import mx.com.bandline.apptaxihgo.model.dataCancelar
import mx.com.bandline.apptaxihgo.model.dataEmergencia
import mx.com.bandline.apptaxihgo.model.dataFolio
import mx.com.bandline.apptaxihgo.model.dataHistorial
import mx.com.bandline.apptaxihgo.model.dataRespuesta
import mx.com.bandline.apptaxihgo.model.dataViaje
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiViaje {
    @GET("obtener_estado_viaje.php")
    suspend fun estado(
        @Header("Authorization") token: String,
        @Query("id_viaje") idViaje: Int
    ): Response<dataRespuesta<dataViaje>>
    @POST("cancelar_viaje_pasajero.php")
    suspend fun cancelar(
        @Header("Authorization") token: String,
        @Body datos: dataCancelar
    ): Response<dataRespuesta<dataViaje>>
    @POST("enviar_emergencia.php")
    suspend fun emergencia(
        @Header("Authorization") token: String,
        @Body datos: dataEmergencia
    ): Response<dataRespuesta<dataFolio>>
    @GET("obtener_historial.php")
    suspend fun historial(
        @Header("Authorization") token: String,
        @Query("periodo") periodo: String,
        @Query("pagina") pagina: Int
    ): Response<dataRespuesta<dataHistorial>>
    @GET("detalle_viaje.php")
    suspend fun detalle(
        @Header("Authorization") token: String,
        @Query("id_viaje") idViaje: Int
    ): Response<dataRespuesta<dataViaje>>
}