package mx.com.bandline.apptaxihgo.presenter

import mx.com.bandline.apptaxihgo.model.dataDisponibilidad
import mx.com.bandline.apptaxihgo.model.dataOferta
import mx.com.bandline.apptaxihgo.model.dataOfertas
import mx.com.bandline.apptaxihgo.model.dataPanelConductor
import mx.com.bandline.apptaxihgo.model.Resultado
import mx.com.bandline.apptaxihgo.model.dataViaje

/** Inicio del conductor: disponibilidad, GPS y solicitudes de viaje · C07 a C14 (Módulo 4). */
interface InicioConductorContract {
    interface View : BaseView {
        fun mostrarPanel(panel: dataPanelConductor)
        fun mostrarDisponibilidad(disponible: Boolean)
        /** Aviso de la central, p. ej. "Te desconectamos porque no respondiste 3 solicitudes seguidas." */
        fun mostrarAvisoConductor(aviso: String)
        fun mostrarOferta(oferta: dataOferta)
        /** La solicitud ya no está: vencida, cancelada por el pasajero o rechazada (mensaje = null). */
        fun ofertaTerminada(mensaje: String?)
        fun documentosVencidos(mensaje: String)
        fun ubicacionNecesaria(mensaje: String)
        fun irAViaje(idViaje: Int)
    }

    interface Presenter {
        fun cargarPanel()
        fun conectar(lat: Double?, lng: Double?)
        fun desconectar()
        fun ubicacionNueva(lat: Double, lng: Double)
        fun responder(aceptar: Boolean)
        fun ofertaVencida()
        fun estaDisponible(): Boolean
        fun pausar()
        fun destruir()
    }

    interface Model {
        suspend fun panel(): Resultado<dataPanelConductor>
        suspend fun disponibilidad(disponible: Boolean, lat: Double?, lng: Double?): Resultado<dataDisponibilidad>
        suspend fun enviarUbicacion(lat: Double, lng: Double): Resultado<dataDisponibilidad>
        suspend fun ofertas(): Resultado<dataOfertas>
        suspend fun responderOferta(idOferta: Int, aceptar: Boolean): Resultado<dataViaje?>
    }
}
