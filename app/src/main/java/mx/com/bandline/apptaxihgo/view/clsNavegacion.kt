package mx.com.bandline.apptaxihgo.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent

// Abre pantallas de otros módulos por su nombre. Si esa pantalla todavía no está en la app, regresa false y no se cierra.
object clsNavegacion {
    const val MI_VIAJE = "mx.com.bandline.apptaxihgo.view.ActivityMiViaje"
    const val MIS_VIAJES = "mx.com.bandline.apptaxihgo.view.ActivityMisViajes"
    const val EXTRA_FOLIO = "FOLIO"

    fun abrir(actividad: Activity, nombreClase: String, folio: String? = null): Boolean {
        return try {
            val intent = Intent(actividad, Class.forName(nombreClase))
            if (folio != null) intent.putExtra(EXTRA_FOLIO, folio)
            actividad.startActivity(intent)
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
