package mx.com.bandline.apptaxihgo.interfaceapi

import mx.com.bandline.apptaxihgo.model.dataCancelar
import mx.com.bandline.apptaxihgo.model.dataEmergencia
import mx.com.bandline.apptaxihgo.model.dataFolio
import mx.com.bandline.apptaxihgo.model.dataHistorial
import mx.com.bandline.apptaxihgo.model.dataCotizacion
import mx.com.bandline.apptaxihgo.model.dataCuentaCreada
import mx.com.bandline.apptaxihgo.model.dataLogin
import mx.com.bandline.apptaxihgo.model.dataPedirViaje
import mx.com.bandline.apptaxihgo.model.dataPerfil
import mx.com.bandline.apptaxihgo.model.dataPhotonRespuesta
import mx.com.bandline.apptaxihgo.model.dataRegistro
import mx.com.bandline.apptaxihgo.model.dataRespuesta
import mx.com.bandline.apptaxihgo.model.dataSesionIniciada
import mx.com.bandline.apptaxihgo.model.dataTaxisCercanos
import mx.com.bandline.apptaxihgo.model.dataViaje
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("registrar_usuario.php")
    suspend fun registrar(@Body datos: dataRegistro): Response<dataRespuesta<dataCuentaCreada>>

    @POST("iniciar_sesion.php")
    suspend fun iniciarSesion(@Body datos: dataLogin): Response<dataRespuesta<dataSesionIniciada>>

    @GET("obtener_perfil.php")
    suspend fun perfil(@Header("Authorization") token: String): Response<dataRespuesta<dataPerfil>>

    // bbox = rectángulo de Huejutla; lat y lon dan prioridad a lo que está cerca del pasajero
    @Headers("User-Agent: TaxiHGO/1.0 (proyecto escolar UTHH)")
    @GET("api/")
    suspend fun buscar(@Query("q") texto: String, @Query("lat") lat: Double?, @Query("lon") lng: Double?, @Query("limit") limite: Int, @Query("bbox") bbox: String): Response<dataPhotonRespuesta>

    @Headers("User-Agent: TaxiHGO/1.0 (proyecto escolar UTHH)")
    @GET("reverse")
    suspend fun direccion(@Query("lat") lat: Double, @Query("lon") lng: Double, @Query("limit") limite: Int): Response<dataPhotonRespuesta>

    @GET("obtener_taxis_cercanos.php")
    suspend fun taxisCercanos(@Header("Authorization") token: String, @Query("lat") lat: Double, @Query("lng") lng: Double): Response<dataRespuesta<dataTaxisCercanos>>

    @POST("cotizar_viaje.php")
    suspend fun cotizar(@Header("Authorization") token: String, @Body datos: dataPedirViaje): Response<dataRespuesta<dataCotizacion>>

    @POST("crear_viaje.php")
    suspend fun crear(@Header("Authorization") token: String, @Body datos: dataPedirViaje): Response<dataRespuesta<dataViaje>>

    // Viaje del pasajero
    @GET("obtener_estado_viaje.php")
    suspend fun estado(@Header("Authorization") token: String, @Query("id_viaje") idViaje: Int): Response<dataRespuesta<dataViaje>>

    @POST("cancelar_viaje_pasajero.php")
    suspend fun cancelar(@Header("Authorization") token: String, @Body datos: dataCancelar): Response<dataRespuesta<dataViaje>>

    @POST("enviar_emergencia.php")
    suspend fun emergencia(@Header("Authorization") token: String, @Body datos: dataEmergencia): Response<dataRespuesta<dataFolio>>

    @GET("obtener_historial.php")
    suspend fun historial(@Header("Authorization") token: String, @Query("periodo") periodo: String, @Query("pagina") pagina: Int): Response<dataRespuesta<dataHistorial>>

    @GET("detalle_viaje.php")
    suspend fun detalle(@Header("Authorization") token: String, @Query("id_viaje") idViaje: Int): Response<dataRespuesta<dataViaje>>
}