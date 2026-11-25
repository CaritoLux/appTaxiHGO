package mx.com.bandline.apptaxihgo.view
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
data class BotonEmergencia(
    private val activity: ActivityViajePasajero,
    private val fondo: View,
    private val progreso: ProgressBar,
    private val texto: TextView
) {
    private val duracion = 3000L
    private var cuenta: CountDownTimer? = null
    private var enviando = false

    init {
        progreso.max = duracion.toInt()
        reiniciar()

        fondo.setOnTouchListener { v, evento ->
            when (evento.action) {
                MotionEvent.ACTION_DOWN -> {
                    empezar()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    soltar()
                    v.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    soltar()
                    true
                }

                else -> false
            }
        }
        fondo.setOnClickListener {
        }
    }

    private fun empezar() {
        if (enviando) {
            return
        }
        texto.text = "Sigue presionando…"
        cuenta = object : CountDownTimer(duracion, 50) {
            override fun onTick(restante: Long) {
                progreso.progress = (duracion - restante).toInt()
            }

            override fun onFinish() {
                progreso.progress = duracion.toInt()
                enviando = true
                texto.text = "Enviando alerta…"
                activity.enviarEmergencia()
            }
        }.start()
    }

    private fun soltar() {
        if (!enviando) {
            cuenta?.cancel()
            reiniciar()
        }
    }
    fun reiniciar() {
        enviando = false
        progreso.progress = 0
        texto.text = "SOS · Mantén presionado 3 s"
    }

    fun detener() {
        cuenta?.cancel()
    }
}
