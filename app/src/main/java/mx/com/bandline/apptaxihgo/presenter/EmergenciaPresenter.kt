package mx.com.bandline.apptaxihgo.presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.ViajeModel
import mx.com.bandline.apptaxihgo.view.ActivityViajePasajero

class EmergenciaPresenter (private val vista: ActivityViajePasajero){

    private val modelo = ViajeModel()
    private var enviando = false

    fun enviar(token: String, idViaje: Int) {
        if (idViaje <= 0 || enviando) {
            return
        }
        enviando = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val respuesta = modelo.emergencia(token, idViaje)
                if (respuesta.exito) {
                    vista.alertaEnviada(respuesta.mensaje, respuesta.datos?.folio ?: "")
                } else if (modelo.ultimoCodigo == 401) {
                    vista.sesionExpirada()
                } else {
                    vista.errorEmergencia(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.errorEmergencia("No se pudo enviar la alerta. Llama al 911. (" + e.localizedMessage + ")")
            }
            enviando = false
        }
    }
}