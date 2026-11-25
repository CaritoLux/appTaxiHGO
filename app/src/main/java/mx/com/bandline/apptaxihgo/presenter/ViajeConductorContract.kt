package mx.com.bandline.apptaxihgo.presenter

import mx.com.bandline.apptaxihgo.model.Resultado
import mx.com.bandline.apptaxihgo.model.dataCancelar
import mx.com.bandline.apptaxihgo.model.dataCambioDisponibilidad
import mx.com.bandline.apptaxihgo.model.dataViaje

/** Viaje del conductor: ir por el pasajero, llegué, iniciar, finalizar y cancelar · diseños C15 a C22 (Módulo 4). */
interface ViajeConductorContract {
    interface View : BaseView {
        fun mostrarViaje(viaje: dataViaje)
        fun viajeFinalizado(viaje: dataViaje)
        /** El pasajero canceló (se detecta en la consulta periódica). */
        fun viajeCancelado(mensaje: String)
        fun mostrarSinConexion(sinConexion: Boolean)
        /**
         * Abre la hoja de motivos (base: clsHojaMotivos). segundosParaNoSePresento:
         * null = todavía no marca Llegué; > 0 = falta ese tiempo; 0 = ya puede elegir "El pasajero no se presentó".
         */
        fun mostrarMotivos(motivos: List<String>, segundosParaNoSePresento: Int?)
        /** Claves: "motivo" y "comentario". Mapa vacío = sin errores. */
        fun mostrarErroresCancelar(errores: Map<String, String>)
        fun mostrarCargandoCancelar(mostrar: Boolean)
        /** El conductor canceló: vuelve a su inicio. */
        fun cancelasteViaje(mensaje: String)
    }

    interface Presenter {
        fun iniciar(idViaje: Int)
        fun reanudar()
        fun pausar()
        fun ubicacionNueva(lat: Double, lng: Double)
        fun llegue()
        fun iniciarViaje()
        fun finalizar()
        fun actualizarAhora()
        fun pedirCancelar()
        fun cancelar(motivo: String?, comentario: String)
        fun destruir()
    }

    interface Model {
        suspend fun viaje(idViaje: Int): Resultado<dataViaje>
        suspend fun avanzar(idViaje: Int, accion: String, lat: Double?, lng: Double?): Resultado<dataViaje>
        suspend fun enviarUbicacion(lat: Double, lng: Double): Resultado<dataDisponibilidad>
        suspend fun cancelar(peticion: dataCancelar): Resultado<dataViaje>
    }
}
