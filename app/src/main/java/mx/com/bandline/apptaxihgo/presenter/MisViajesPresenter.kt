package mx.com.bandline.apptaxihgo.presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.com.bandline.apptaxihgo.model.ViajeModel
import mx.com.bandline.apptaxihgo.view.ActivityMisViajes

class MisViajesPresenter (private val vista: ActivityMisViajes){

    private val modelo = ViajeModel()
    private var periodo = "30"
    private var pagina = 1
    private var hayMas = false
    private var cargando = false

    fun cargar(token: String, nuevoPeriodo: String) {
        periodo = nuevoPeriodo
        pagina = 1
        pedir(token, false)
    }

    fun cargarMas(token: String) {
        if (!hayMas || cargando) {
            return
        }
        pagina = pagina + 1
        pedir(token, true)
    }

    private fun pedir(token: String, agregar: Boolean) {
        cargando = true
        val periodoPedido = periodo
        CoroutineScope(Dispatchers.Main).launch {
            try {
                vista.mostrarCargando(true)
                val respuesta = modelo.historial(token, periodoPedido, pagina)
                vista.mostrarCargando(false)
                val historial = respuesta.datos

                if (periodoPedido != periodo) {
                } else if (respuesta.exito && historial != null) {
                    hayMas = historial.hayMas
                    if (!agregar) {
                        vista.mostrarTotales(historial.totales)
                    }
                    if (!agregar && historial.viajes.isEmpty()) {
                        vista.mostrarVacio(respuesta.mensaje)
                    } else {
                        vista.mostrarViajes(historial.viajes, agregar)
                    }
                } else if (modelo.ultimoCodigo == 401) {
                    vista.sesionExpirada()
                } else {
                    if (agregar) {
                        pagina = pagina - 1
                    }
                    vista.mostrarError(respuesta.mensaje)
                }
            } catch (e: Exception) {
                vista.mostrarCargando(false)
                if (agregar) {
                    pagina = pagina - 1
                }
                vista.mostrarError("Error al cargar tus viajes: " + e.localizedMessage)
            }
            cargando = false
        }
    }
}