package mx.com.bandline.apptaxihgo.view

import android.media.RingtoneManager
import android.os.CountDownTimer
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataOferta

class HojaOferta(
    private val actividad: clsActividadBase,
    private val alResponder: (aceptar: Boolean) -> Unit,
    private val alVencer: () -> Unit
) {
    private var dialogo: BottomSheetDialog? = null
    private var cuenta: CountDownTimer? = null
    private var botones: List<MaterialButton> = emptyList()

    fun mostrar(oferta: dataOferta) {
        cerrar()
        val v = actividad.layoutInflater.inflate(R.layout.hoja_oferta, null)
        v.findViewById<TextView>(R.id.tv_tarifa).text = clsFormato.moneda(oferta.tarifaEstimada)
        v.findViewById<TextView>(R.id.tv_distancia).text = actividad.getString(
            R.string.a_distancia_de_ti, clsFormato.km(oferta.distanciaAOrigenKm), clsFormato.minutos(oferta.minutosAOrigen)
        )
        v.findViewById<TextView>(R.id.tv_origen).text = oferta.origen.direccion
        v.findViewById<TextView>(R.id.tv_destino).text = oferta.destino.direccion
        val tvReferencia: TextView = v.findViewById(R.id.tv_referencia)
        tvReferencia.text = actividad.getString(R.string.referencia_de, oferta.referencia ?: "")
        tvReferencia.visibility = if (oferta.referencia.isNullOrBlank()) View.GONE else View.VISIBLE
        v.findViewById<TextView>(R.id.tv_pasajero).text = oferta.pasajero
        v.findViewById<TextView>(R.id.tv_calificacion_pasajero).text = clsFormato.calificacion(oferta.calificacionPasajero)

        val aceptar: MaterialButton = v.findViewById(R.id.btn_aceptar)
        val rechazar: MaterialButton = v.findViewById(R.id.btn_rechazar)
        botones = listOf(aceptar, rechazar)
        aceptar.setOnClickListener { bloquear(true); alResponder(true) }
        rechazar.setOnClickListener { bloquear(true); alResponder(false) }

        val pbTiempo: CircularProgressIndicator = v.findViewById(R.id.pb_tiempo)
        val tvSegundos: TextView = v.findViewById(R.id.tv_segundos)
        val total = oferta.segundosRestantes.coerceAtLeast(1)
        pbTiempo.max = total * 10
        cuenta = object : CountDownTimer(total * 1000L, 100) {
            override fun onTick(restanteMs: Long) {
                pbTiempo.progress = (restanteMs / 100).toInt()
                tvSegundos.text = ((restanteMs + 999) / 1000).toString()
            }

            override fun onFinish() {
                cerrar()
                alVencer()
            }
        }.start()

        val d = BottomSheetDialog(actividad)
        d.setContentView(v)
        d.setCancelable(false)
        d.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        d.behavior.skipCollapsed = true
        d.show()
        dialogo = d
        avisar(v)
    }

    private fun avisar(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        try {
            RingtoneManager.getRingtone(actividad, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.play()
        } catch (e: Exception) {
            // sin sonido disponible
        }
    }

    fun bloquear(bloqueado: Boolean) = botones.forEach { it.isEnabled = !bloqueado }

    fun cerrar() {
        cuenta?.cancel()
        cuenta = null
        dialogo?.dismiss()
        dialogo = null
    }
}
