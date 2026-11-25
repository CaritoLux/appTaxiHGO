package mx.com.bandline.apptaxihgo.interfaceapi

import mx.com.bandline.apptaxihgo.model.dataAvance
import mx.com.bandline.apptaxihgo.model.dataCambioDisponibilidad
import mx.com.bandline.apptaxihgo.model.dataCancelar
import mx.com.bandline.apptaxihgo.model.dataDisponibilidad
import mx.com.bandline.apptaxihgo.model.dataOfertas
import mx.com.bandline.apptaxihgo.model.dataPanelConductor
import mx.com.bandline.apptaxihgo.model.dataRespuesta
import mx.com.bandline.apptaxihgo.model.dataRespuestaOferta
import mx.com.bandline.apptaxihgo.model.dataUbicacion
import mx.com.bandline.apptaxihgo.model.dataViaje
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Módulo 4 · Conductor en servicio. Un archivo PHP por acción:
 * obtener_panel_conductor.php, cambiar_disponibilidad.php, actualizar_ubicacion.php, obtener_ofertas.php, responder_oferta.php, obtener_viaje_conductor.php, avanzar_viaje.php, cancelar_viaje_conductor.php
 */
interface ApiConductor {
    @GET("obtener_panel_conductor.php")
    suspend fun panel(): Response<dataRespuesta<dataPanelConductor>>

    @POST("cambiar_disponibilidad.php")
    suspend fun disponibilidad(@Body peticion: dataCambioDisponibilidad): Response<dataRespuesta<dataDisponibilidad>>

    @POST("actualizar_ubicacion.php")
    suspend fun ubicacion(@Body peticion: dataUbicacion): Response<dataRespuesta<dataDisponibilidad>>

    @GET("obtener_ofertas.php")
    suspend fun ofertas(): Response<dataRespuesta<dataOfertas>>

    /** ACEPTAR devuelve el viaje; RECHAZAR devuelve datos = null. */
    @POST("responder_oferta.php")
    suspend fun responderOferta(@Body peticion: dataRespuestaOferta): Response<dataRespuesta<dataViaje>>

    @GET("obtener_viaje_conductor.php")
    suspend fun viajeActivo(@Query("id_viaje") idViaje: Int?): Response<dataRespuesta<dataViaje>>

    @POST("avanzar_viaje.php")
    suspend fun avanzarViaje(@Body peticion: dataAvance): Response<dataRespuesta<dataViaje>>

    @POST("cancelar_viaje_conductor.php")
    suspend fun cancelar(@Body peticion: dataCancelar): Response<dataRespuesta<dataViaje>>
}
