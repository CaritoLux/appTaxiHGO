package mx.com.bandline.apptaxihgo.view

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.clsUbicacion
import mx.com.bandline.apptaxihgo.model.dataTaxiCercano
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

class clsMapa(private val contexto: Context, private val mapView: MapView, ciclo: Lifecycle) : DefaultLifecycleObserver {

    companion object {
        const val ESTILO = "https://tiles.openfreemap.org/styles/positron"
        private val NEGRO = Color.parseColor("#111111")
    }

    private var mapa: MapLibreMap? = null
    private var estilo: Style? = null
    private val pendientes = mutableListOf<(Style) -> Unit>()
    private var alDetenerse: ((Double, Double) -> Unit)? = null
    private var alMoverConDedo: (() -> Unit)? = null

    init {
        mapView.onCreate(null)
        ciclo.addObserver(this)
        mapView.getMapAsync { m ->
            mapa = m
            m.uiSettings.isLogoEnabled = false
            m.uiSettings.isCompassEnabled = false
            m.uiSettings.isRotateGesturesEnabled = false
            m.uiSettings.isTiltGesturesEnabled = false
            m.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(clsUbicacion.HUEJUTLA_LAT, clsUbicacion.HUEJUTLA_LNG), 14.0))
            m.addOnCameraMoveStartedListener { razon ->
                if (razon == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) alMoverConDedo?.invoke()
            }
            m.addOnCameraIdleListener {
                val centro = m.cameraPosition.target
                if (centro != null) alDetenerse?.invoke(centro.latitude, centro.longitude)
            }
            m.setStyle(Style.Builder().fromUri(ESTILO)) { s ->
                prepararCapas(s)
                estilo = s
                pendientes.forEach { it(s) }
                pendientes.clear()
            }
        }
    }

    private fun onCreate(nothing: Nothing?) {}

    // Se quitan los puntos de interés para que destaquen la ruta y los taxis
    private fun prepararCapas(s: Style) {
        s.layers.filter { it.id.contains("poi") }.forEach { s.removeLayer(it) }
        ContextCompat.getDrawable(contexto, R.drawable.marcador_taxi)?.let { s.addImage("taxi", it.toBitmap()) }
        ContextCompat.getDrawable(contexto, R.drawable.marcador_destino)?.let { s.addImage("destino", it.toBitmap()) }
        s.addSource(GeoJsonSource("ruta"))
        s.addSource(GeoJsonSource("origen"))
        s.addSource(GeoJsonSource("destino"))
        s.addSource(GeoJsonSource("taxis"))
        s.addLayer(LineLayer("capa_ruta", "ruta").withProperties(
            PropertyFactory.lineColor(NEGRO),
            PropertyFactory.lineWidth(4f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)))
        s.addLayer(CircleLayer("capa_origen", "origen").withProperties(
            PropertyFactory.circleRadius(7f),
            PropertyFactory.circleColor(NEGRO),
            PropertyFactory.circleStrokeColor(Color.WHITE),
            PropertyFactory.circleStrokeWidth(3f)))
        s.addLayer(SymbolLayer("capa_destino", "destino").withProperties(
            PropertyFactory.iconImage("destino"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)))
        s.addLayer(SymbolLayer("capa_taxis", "taxis").withProperties(
            PropertyFactory.iconImage("taxi"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)))
    }

    // Si el estilo todavía no carga, la acción espera
    private fun conEstilo(accion: (Style) -> Unit) {
        val s = estilo
        if (s != null) accion(s) else pendientes.add(accion)
    }

    private fun ponerPunto(fuente: String, lat: Double?, lng: Double?) = conEstilo { s ->
        val geoJson = s.getSourceAs<GeoJsonSource>(fuente) ?: return@conEstilo
        if (lat == null || lng == null) geoJson.setGeoJson(FeatureCollection.fromFeatures(emptyList<Feature>()))
        else geoJson.setGeoJson(Feature.fromGeometry(Point.fromLngLat(lng, lat)))
    }

    fun ponerOrigen(lat: Double?, lng: Double?) = ponerPunto("origen", lat, lng)

    fun ponerDestino(lat: Double?, lng: Double?) = ponerPunto("destino", lat, lng)

    fun ponerTaxis(taxis: List<dataTaxiCercano>) = conEstilo { s ->
        val puntos = taxis.map { Feature.fromGeometry(Point.fromLngLat(it.lng, it.lat)) }
        s.getSourceAs<GeoJsonSource>("taxis")?.setGeoJson(FeatureCollection.fromFeatures(puntos))
    }

    // ruta = [[longitud, latitud], ...] como la entrega el PHP
    fun ponerRuta(ruta: List<List<Double>>) = conEstilo { s ->
        val fuente = s.getSourceAs<GeoJsonSource>("ruta") ?: return@conEstilo
        val puntos = ruta.filter { it.size >= 2 }.map { Point.fromLngLat(it[0], it[1]) }
        if (puntos.size < 2) fuente.setGeoJson(FeatureCollection.fromFeatures(emptyList<Feature>()))
        else fuente.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(puntos)))
    }

    fun centrar(lat: Double, lng: Double, zoom: Double = 16.0) {
        val m = mapa
        if (m == null) {
            mapView.getMapAsync { centrar(lat, lng, zoom) }
            return
        }
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
    }

    // Acomoda la cámara para que se vean todos los puntos (lat, lng)
    fun encuadrar(puntos: List<Pair<Double, Double>>) {
        val m = mapa
        if (m == null) {
            mapView.getMapAsync { encuadrar(puntos) }
            return
        }
        val distintos = puntos.distinct()
        if (distintos.isEmpty()) return
        if (distintos.size == 1) {
            centrar(distintos[0].first, distintos[0].second)
            return
        }
        val limites = LatLngBounds.Builder()
        distintos.forEach { limites.include(LatLng(it.first, it.second)) }
        val margen = (56 * contexto.resources.displayMetrics.density).toInt()
        try {
            m.animateCamera(CameraUpdateFactory.newLatLngBounds(limites.build(), margen, margen * 2, margen, margen))
        } catch (e: Exception) {
            centrar(distintos[0].first, distintos[0].second)
        }
    }

    // Cuando el mapa deja de moverse avisa el centro (para el punto de recogida o el destino fijado)
    fun alDetenerCamara(accion: (Double, Double) -> Unit) {
        alDetenerse = accion
    }

    fun alMoverConElDedo(accion: () -> Unit) {
        alMoverConDedo = accion
    }

    // El MapView sigue el ciclo de vida de la pantalla
    override fun onStart(owner: LifecycleOwner) = mapView.onStart()
    override fun onResume(owner: LifecycleOwner) = mapView.onResume()
    override fun onPause(owner: LifecycleOwner) = mapView.onPause()
    override fun onStop(owner: LifecycleOwner) = mapView.onStop()
    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        mapView.onDestroy()
    }
}
