package mx.com.bandline.apptaxihgo.model

import mx.com.bandline.apptaxihgo.interfaceapi.ApiConductor
import mx.com.bandline.apptaxihgo.interfaceapi.RetrofitCliente
import mx.com.bandline.apptaxihgo.interfaceapi.clsLlamadaApi
import mx.com.bandline.apptaxihgo.presenter.InicioConductorContract
import mx.com.bandline.apptaxihgo.presenter.ViajeConductorContract

/** Model del Módulo 4 (Conductor en servicio) -> api_conductor.php */
class ConductorModel(
    private val api: ApiConductor = RetrofitCliente.crear(ApiConductor::class.java)
) : InicioConductorContract.Model, ViajeConductorContract.Model {

    override suspend fun panel(): Resultado<dataPanelConductor> = clsLlamadaApi.llamar { api.panel() }

    override suspend fun disponibilidad(disponible: Boolean, lat: Double?, lng: Double?): Resultado<dataDisponibilidad> =
        clsLlamadaApi.llamar { api.disponibilidad(dataCambioDisponibilidad(disponible, lat, lng)) }

    override suspend fun enviarUbicacion(lat: Double, lng: Double): Resultado<dataDisponibilidad> =
        clsLlamadaApi.llamar { api.ubicacion(dataUbicacion(lat, lng)) }

    override suspend fun ofertas(): Resultado<dataOfertas> = clsLlamadaApi.llamar { api.ofertas() }

    override suspend fun responderOferta(idOferta: Int, aceptar: Boolean): Resultado<dataViaje?> =
        clsLlamadaApi.llamarOpcional { api.responderOferta(dataRespuestaOferta(idOferta, if (aceptar) "ACEPTAR" else "RECHAZAR")) }

    override suspend fun viaje(idViaje: Int): Resultado<dataViaje> = clsLlamadaApi.llamar { api.viajeActivo(idViaje) }

    override suspend fun avanzar(idViaje: Int, accion: String, lat: Double?, lng: Double?): Resultado<dataViaje> =
        clsLlamadaApi.llamar { api.avanzarViaje(dataAvance(idViaje, accion, lat, lng)) }

    override suspend fun cancelar(peticion: dataCancelar): Resultado<dataViaje> = clsLlamadaApi.llamar { api.cancelar(peticion) }
}
