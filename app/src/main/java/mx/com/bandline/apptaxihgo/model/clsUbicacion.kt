package mx.com.bandline.apptaxihgo.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// GPS real del teléfono (Fused Location Provider de Google, gratis y sin llave)
object clsUbicacion {

    // Centro de Huejutla, para abrir el mapa mientras llega el GPS
    const val HUEJUTLA_LAT = 21.1404
    const val HUEJUTLA_LNG = -98.4197

    // Rectángulo que encierra al municipio. El PHP revisa el límite exacto con zonas_cobertura.
    private const val LAT_MIN = 21.056
    private const val LAT_MAX = 21.264
    private const val LNG_MIN = -98.542
    private const val LNG_MAX = -98.328
    const val BBOX_HUEJUTLA = "-98.542,21.056,-98.328,21.264"

    fun dentroDeHuejutla(lat: Double, lng: Double): Boolean {
        return lat in LAT_MIN..LAT_MAX && lng in LNG_MIN..LNG_MAX
    }

    fun distanciaMetros(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val resultado = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, resultado)
        return resultado[0]
    }

    fun tienePermiso(contexto: Context): Boolean {
        val fino = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val aproximado = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fino || aproximado
    }

    fun gpsEncendido(contexto: Context): Boolean {
        val administrador = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(administrador)
    }

    // Pide una lectura nueva del GPS; si no llega, usa la última conocida. Regresa null si no hay ninguna.
    @SuppressLint("MissingPermission")
    suspend fun obtener(contexto: Context): Location? {
        if (!tienePermiso(contexto)) return null
        val cliente = LocationServices.getFusedLocationProviderClient(contexto)
        return suspendCancellableCoroutine { continuacion ->
            val cancelar = CancellationTokenSource()
            try {
                cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelar.token)
                    .addOnSuccessListener { ubicacion ->
                        if (ubicacion != null) {
                            if (continuacion.isActive) continuacion.resume(ubicacion)
                        } else {
                            cliente.lastLocation
                                .addOnSuccessListener { ultima -> if (continuacion.isActive) continuacion.resume(ultima) }
                                .addOnFailureListener { if (continuacion.isActive) continuacion.resume(null) }
                        }
                    }
                    .addOnFailureListener { if (continuacion.isActive) continuacion.resume(null) }
            } catch (e: SecurityException) {
                // El permiso se quitó mientras la app estaba abierta
                if (continuacion.isActive) continuacion.resume(null)
            }
            continuacion.invokeOnCancellation { cancelar.cancel() }
        }
    }
}
