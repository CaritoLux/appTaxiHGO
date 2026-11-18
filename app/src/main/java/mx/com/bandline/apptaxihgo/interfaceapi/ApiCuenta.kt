package mx.com.bandline.apptaxihgo.interfaceapi

import mx.com.bandline.apptaxihgo.model.dataCuentaCreada
import mx.com.bandline.apptaxihgo.model.dataLogin
import mx.com.bandline.apptaxihgo.model.dataRegistro
import mx.com.bandline.apptaxihgo.model.dataSesionIniciada
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiCuenta {
    @POST("registrar_usuario.php")
    suspend fun registrar(@Body datos: dataRegistro): Response<dataRespuesta<dataCuentaCreada>>

    @POST("iniciar_sesion.php")
    suspend fun iniciarSesion(@Body datos: dataLogin): Response<dataRespuesta<dataSesionIniciada>>

    @GET("obtener_perfil.php")
    suspend fun perfil(): Response<dataRespuesta<dataPerfil>>
}