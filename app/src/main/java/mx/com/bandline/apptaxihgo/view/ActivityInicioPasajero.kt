package mx.com.bandline.apptaxihgo.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.model.dataTaxiCercano
import mx.com.bandline.apptaxihgo.presenter.InicioPasajeroPresenter
import org.maplibre.android.MapLibre

// Módulo 3 · Inicio del pasajero: mapa con su ubicación, taxis cercanos y "¿A dónde vas?"
class ActivityInicioPasajero : AppCompatActivity() {

    companion object {
        // Confirmar viaje regresa aquí con el folio del viaje creado para abrir Mi viaje (Módulo 5)
        const val EXTRA_FOLIO_NUEVO = "FOLIO_NUEVO"
    }

    private lateinit var presenter: InicioPasajeroPresenter
    private lateinit var mapa: clsMapa
    private lateinit var tvSaludo: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var cardBurbuja: MaterialCardView
    private lateinit var imgPin: ImageView
    private lateinit var cardAviso: MaterialCardView
    private lateinit var tvAviso: TextView
    private lateinit var btnAviso: MaterialButton
    private lateinit var cardBuscar: MaterialCardView
    private lateinit var btnMiUbicacion: MaterialCardView
    private lateinit var navegacion: BottomNavigationView
    private var tipoAviso = 0

    private val pedirPermiso = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
        presenter.permisoRespondido(permisos.values.any { it })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_inicio_pasajero)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            WindowInsetsCompat.CONSUMED
        }

        tvSaludo = findViewById(R.id.tv_saludo)
        tvDireccion = findViewById(R.id.tv_direccion)
        cardBurbuja = findViewById(R.id.card_burbuja)
        imgPin = findViewById(R.id.img_pin)
        cardAviso = findViewById(R.id.card_aviso)
        tvAviso = findViewById(R.id.tv_aviso)
        btnAviso = findViewById(R.id.btn_aviso)
        cardBuscar = findViewById(R.id.card_buscar)
        btnMiUbicacion = findViewById(R.id.btn_mi_ubicacion)
        navegacion = findViewById(R.id.navegacion)

        presenter = InicioPasajeroPresenter(this)
        mapa = clsMapa(this, findViewById(R.id.mapa), lifecycle)
        mapa.alMoverConElDedo { presenter.mapaMovidoPorUsuario() }
        mapa.alDetenerCamara { lat, lng -> presenter.mapaDetenido(lat, lng) }

        cardBuscar.setOnClickListener { presenter.elegirDestino() }
        btnMiUbicacion.setOnClickListener { presenter.leerUbicacion() }
        btnAviso.setOnClickListener { accionDelAviso() }

        navegacion.selectedItemId = R.id.nav_inicio
        navegacion.setOnItemSelectedListener { opcion ->
            when (opcion.itemId) {
                R.id.nav_mis_viajes -> {
                    if (!clsNavegacion.abrir(this, clsNavegacion.MIS_VIAJES)) {
                        mostrarError("La pantalla Mis viajes todavía no está en la app.")
                    }
                    false
                }
                R.id.nav_cuenta -> {
                    startActivity(Intent(this, ActivityCuenta::class.java))
                    false
                }
                else -> true
            }
        }

        // Token guardado al iniciar sesión (Módulo 1)
        val prefs = getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        presenter.iniciar(prefs.getString("token", "") ?: "")
        revisarViajeNuevo(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        revisarViajeNuevo(intent)
    }

    override fun onResume() {
        super.onResume()
        navegacion.menu.findItem(R.id.nav_inicio).isChecked = true
        presenter.reanudar()
    }

    override fun onPause() {
        presenter.pausar()
        super.onPause()
    }

    override fun onDestroy() {
        presenter.destruir()
        super.onDestroy()
    }

    // Después de pedir el taxi se abre Mi viaje (Módulo 5) por su nombre
    private fun revisarViajeNuevo(intent: Intent) {
        val folio = intent.getStringExtra(EXTRA_FOLIO_NUEVO) ?: return
        intent.removeExtra(EXTRA_FOLIO_NUEVO)
        if (!clsNavegacion.abrir(this, clsNavegacion.MI_VIAJE, folio)) {
            Toast.makeText(this, "Pedimos tu taxi (folio $folio). La pantalla Mi viaje todavía no está en la app.", Toast.LENGTH_LONG).show()
        }
    }

    private fun accionDelAviso() {
        when (tipoAviso) {
            InicioPasajeroPresenter.AVISO_PERMISO -> {
                val ajustes = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
                startActivity(ajustes)
            }
            InicioPasajeroPresenter.AVISO_GPS -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            InicioPasajeroPresenter.AVISO_SIN_SENAL -> presenter.leerUbicacion()
        }
    }

    fun pedirPermisoUbicacion() {
        pedirPermiso.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    fun mostrarSaludo(nombre: String) {
        tvSaludo.text = "Hola, $nombre"
    }

    fun centrarMapa(lat: Double, lng: Double) {
        mapa.centrar(lat, lng)
    }

    fun mostrarDireccion(texto: String) {
        tvDireccion.text = texto
        cardBurbuja.visibility = View.VISIBLE
        imgPin.visibility = View.VISIBLE
    }

    fun mostrarTaxis(taxis: List<dataTaxiCercano>) {
        mapa.ponerTaxis(taxis)
    }

    fun mostrarAviso(texto: String, boton: String?, tipo: Int) {
        tipoAviso = tipo
        tvAviso.text = texto
        btnAviso.text = boton ?: ""
        btnAviso.visibility = if (boton == null) View.GONE else View.VISIBLE
        cardAviso.visibility = View.VISIBLE
    }

    fun ocultarAviso() {
        tipoAviso = 0
        cardAviso.visibility = View.GONE
    }

    fun abrirElegirDestino(origen: dataLugar) {
        val intent = Intent(this, ActivityElegirDestino::class.java)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_NOMBRE, origen.nombre)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_DETALLE, origen.detalle)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_LAT, origen.lat)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_LNG, origen.lng)
        startActivity(intent)
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    fun sesionVencida() {
        if (isFinishing) return
        getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(this, "Tu sesión terminó. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, ActivityLogin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
