package mx.com.bandline.apptaxihgo.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
object clsNavegacion {

    const val EXTRA_ID_VIAJE = "ID_VIAJE"
    const val EXTRA_ORIGEN_DIRECCION = "ORIGEN_DIRECCION"
    const val EXTRA_ORIGEN_LAT = "ORIGEN_LAT"
    const val EXTRA_ORIGEN_LNG = "ORIGEN_LNG"
    const val EXTRA_DESTINO_DIRECCION = "DESTINO_DIRECCION"
    const val EXTRA_DESTINO_LAT = "DESTINO_LAT"
    const val EXTRA_DESTINO_LNG = "DESTINO_LNG"

    private const val PAQUETE = "mx.com.bandline.apptaxihgo.view."
    const val CALIFICAR = PAQUETE + "ActivityCalificar"
    const val CONFIRMAR_VIAJE = PAQUETE + "ActivityConfirmarViaje"
    const val INICIO_PASAJERO = PAQUETE + "ActivityInicioPasajero"
    const val REPORTAR_INCIDENTE = PAQUETE + "ActivityReportarIncidente"
    fun abrir(activity: Activity, nombreClase: String, intent: Intent, avisar: Boolean = true): Boolean {
        return try {
            Class.forName(nombreClase) // ¿la clase existe en el proyecto?
            intent.setClassName(activity, nombreClase)
            activity.startActivity(intent)
            true
        } catch (e: ClassNotFoundException) {
            if (avisar) {
                Toast.makeText(activity, "Esta pantalla es de otro módulo y todavía no está en el proyecto.", Toast.LENGTH_LONG).show()
            }
            false
        } catch (e: ActivityNotFoundException) {
            if (avisar) {
                Toast.makeText(activity, "La pantalla existe pero falta registrarla en el AndroidManifest.", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
    fun irACalificar(activity: Activity, idViaje: Int): Boolean {
        val intent = Intent()
        intent.putExtra(EXTRA_ID_VIAJE, idViaje)
        return abrir(activity, CALIFICAR, intent)
    }
    fun irAReportar(activity: Activity, idViaje: Int): Boolean {
        val intent = Intent()
        intent.putExtra(EXTRA_ID_VIAJE, idViaje)
        return abrir(activity, REPORTAR_INCIDENTE, intent)
    }
    fun irAConfirmarViaje(
        activity: Activity, origen: String, origenLat: Double, origenLng: Double,
        destino: String, destinoLat: Double, destinoLng: Double
    ): Boolean {
        val intent = Intent()
        intent.putExtra(EXTRA_ORIGEN_DIRECCION, origen)
        intent.putExtra(EXTRA_ORIGEN_LAT, origenLat)
        intent.putExtra(EXTRA_ORIGEN_LNG, origenLng)
        intent.putExtra(EXTRA_DESTINO_DIRECCION, destino)
        intent.putExtra(EXTRA_DESTINO_LAT, destinoLat)
        intent.putExtra(EXTRA_DESTINO_LNG, destinoLng)
        return abrir(activity, CONFIRMAR_VIAJE, intent)
    }
    fun irAlInicio(activity: Activity) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        abrir(activity, INICIO_PASAJERO, intent, false)
        activity.finish()
    }
    fun irALogin(activity: Activity) {
        val prefs = activity.getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Toast.makeText(activity, "Tu sesión expiró. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
        val intent = Intent(activity, ActivityLogin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
    }
    fun token(activity: Activity): String {
        val prefs = activity.getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }
    fun rol(activity: Activity): String {
        val prefs = activity.getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        return prefs.getString("rol", "PASAJERO") ?: "PASAJERO"
    }
    


// Abre pantallas de otros módulos por su nombre. Si esa pantalla todavía no está en la app, regresa false y no se cierra.
    const val MI_VIAJE = "mx.com.bandline.apptaxihgo.view.ActivityMiViaje" //Activityviajepasajero
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
