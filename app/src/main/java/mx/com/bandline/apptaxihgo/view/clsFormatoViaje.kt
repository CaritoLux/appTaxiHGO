package mx.com.bandline.apptaxihgo.view

import android.graphics.Color
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object clsFormatoViaje {

    private val espanol = Locale.forLanguageTag("es-MX")
    private fun leerFecha(texto: String?): Date? {
        if (texto.isNullOrEmpty()) {
            return null
        }
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(texto)
        } catch (e: Exception) {
            null
        }
    }
    fun fechaLista(texto: String?): String {
        val fecha = leerFecha(texto) ?: return ""
        val hora = SimpleDateFormat("HH:mm", espanol).format(fecha)
        val dia = Calendar.getInstance()
        dia.time = fecha
        val hoy = Calendar.getInstance()
        val ayer = Calendar.getInstance()
        ayer.add(Calendar.DAY_OF_YEAR, -1)

        if (mismoDia(dia, hoy)) {
            return "Hoy, $hora"
        }
        if (mismoDia(dia, ayer)) {
            return "Ayer, $hora"
        }
        val corta = SimpleDateFormat("d MMM", espanol).format(fecha).replace(".", "")
        return "$corta, $hora"
    }

    private fun mismoDia(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
    fun fechaLarga(texto: String?): String {
        val fecha = leerFecha(texto) ?: return "—"
        return SimpleDateFormat("d MMM yyyy · HH:mm", espanol).format(fecha).replace(".", "")
    }
    fun hora(texto: String?): String {
        val fecha = leerFecha(texto) ?: return ""
        return SimpleDateFormat("HH:mm", espanol).format(fecha)
    }
    fun horaEnMinutos(minutos: Int): String {
        val ahora = Calendar.getInstance()
        ahora.add(Calendar.MINUTE, minutos)
        return SimpleDateFormat("HH:mm", espanol).format(ahora.time)
    }

    fun moneda(cantidad: Int): String {
        return "\$" + cantidad
    }
    fun iniciales(nombre: String): String {
        val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
        var letras = ""
        for (parte in partes.take(2)) {
            letras += parte.first().uppercaseChar()
        }
        return letras
    }
    fun pintarEstado(etiqueta: TextView, estado: String, texto: String) {
        etiqueta.text = texto
        when (estado) {
            "FINALIZADO" -> {
                etiqueta.setBackgroundColor(Color.parseColor("#E6F4EA"))
                etiqueta.setTextColor(Color.parseColor("#1E7B34"))
            }
            "CANCELADO" -> {
                etiqueta.setBackgroundColor(Color.parseColor("#FDECEA"))
                etiqueta.setTextColor(Color.parseColor("#C62828"))
            }
            "SIN_CONDUCTOR" -> {
                etiqueta.setBackgroundColor(Color.parseColor("#EEEEEE"))
                etiqueta.setTextColor(Color.parseColor("#555555"))
            }
            else -> {
                etiqueta.setBackgroundColor(Color.parseColor("#FFF3CD"))
                etiqueta.setTextColor(Color.parseColor("#111111"))
            }
        }
    }
}
