package mx.com.bandline.apptaxihgo.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataCotizacion
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.presenter.ConfirmarViajePresenter
import org.maplibre.android.MapLibre

// Módulo 3 · Confirmar viaje: ruta, tiempo, tarifa, referencia y "Pedir taxi"
class ActivityConfirmarViaje : AppCompatActivity() {

    companion object {
        const val EXTRA_ORIGEN_NOMBRE = "ORIGEN_NOMBRE"
        const val EXTRA_ORIGEN_DETALLE = "ORIGEN_DETALLE"
        const val EXTRA_ORIGEN_LAT = "ORIGEN_LAT"
        const val EXTRA_ORIGEN_LNG = "ORIGEN_LNG"
        const val EXTRA_DESTINO_NOMBRE = "DESTINO_NOMBRE"
        const val EXTRA_DESTINO_DETALLE = "DESTINO_DETALLE"
        const val EXTRA_DESTINO_LAT = "DESTINO_LAT"
        const val EXTRA_DESTINO_LNG = "DESTINO_LNG"
        const val EXTRA_REFERENCIA = "REFERENCIA"

        // Mi viaje (Módulo 5) también abre esta pantalla para "Buscar otro taxi" con el mismo origen y destino
        fun crearIntent(contexto: Context, origen: dataLugar, destino: dataLugar, referencia: String): Intent {
            val intent = Intent(contexto, ActivityConfirmarViaje::class.java)
            intent.putExtra(EXTRA_ORIGEN_NOMBRE, origen.nombre)
            intent.putExtra(EXTRA_ORIGEN_DETALLE, origen.detalle)
            intent.putExtra(EXTRA_ORIGEN_LAT, origen.lat)
            intent.putExtra(EXTRA_ORIGEN_LNG, origen.lng)
            intent.putExtra(EXTRA_DESTINO_NOMBRE, destino.nombre)
            intent.putExtra(EXTRA_DESTINO_DETALLE, destino.detalle)
            intent.putExtra(EXTRA_DESTINO_LAT, destino.lat)
            intent.putExtra(EXTRA_DESTINO_LNG, destino.lng)
            intent.putExtra(EXTRA_REFERENCIA, referencia)
            return intent
        }
    }

    private lateinit var presenter: ConfirmarViajePresenter
    private lateinit var mapa: clsMapa
    private lateinit var origen: dataLugar
    private lateinit var tvDestino: TextView
    private lateinit var grupoCotizando: LinearLayout
    private lateinit var grupoError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var btnReintentar: MaterialButton
    private lateinit var cardTarifa: MaterialCardView
    private lateinit var tvDistanciaTiempo: TextView
    private lateinit var tvTarifa: TextView
    private lateinit var tilReferencia: TextInputLayout
    private lateinit var etReferencia: TextInputEditText
    private lateinit var btnCambiar: MaterialButton
    private lateinit var btnPedir: MaterialButton
    private var hayCotizacion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_confirmar_viaje)
        // Con el teclado abierto (referencia) la hoja sube y el mapa se hace más chico
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left, barras.top, barras.right, maxOf(barras.bottom, teclado.bottom))
            WindowInsetsCompat.CONSUMED
        }

        tvDestino = findViewById(R.id.tv_destino)
        grupoCotizando = findViewById(R.id.grupo_cotizando)
        grupoError = findViewById(R.id.grupo_error)
        tvError = findViewById(R.id.tv_error)
        btnReintentar = findViewById(R.id.btn_reintentar)
        cardTarifa = findViewById(R.id.card_tarifa)
        tvDistanciaTiempo = findViewById(R.id.tv_distancia_tiempo)
        tvTarifa = findViewById(R.id.tv_tarifa)
        tilReferencia = findViewById(R.id.til_referencia)
        etReferencia = findViewById(R.id.et_referencia)
        btnCambiar = findViewById(R.id.btn_cambiar)
        btnPedir = findViewById(R.id.btn_pedir)

        mapa = clsMapa(this, findViewById(R.id.mapa), lifecycle)
        presenter = ConfirmarViajePresenter(this)

        origen = dataLugar(
            nombre = intent.getStringExtra(EXTRA_ORIGEN_NOMBRE) ?: "",
            detalle = intent.getStringExtra(EXTRA_ORIGEN_DETALLE) ?: "",
            lat = intent.getDoubleExtra(EXTRA_ORIGEN_LAT, 0.0),
            lng = intent.getDoubleExtra(EXTRA_ORIGEN_LNG, 0.0)
        )
        val destino = dataLugar(
            nombre = intent.getStringExtra(EXTRA_DESTINO_NOMBRE) ?: "",
            detalle = intent.getStringExtra(EXTRA_DESTINO_DETALLE) ?: "",
            lat = intent.getDoubleExtra(EXTRA_DESTINO_LAT, 0.0),
            lng = intent.getDoubleExtra(EXTRA_DESTINO_LNG, 0.0)
        )
        etReferencia.setText(intent.getStringExtra(EXTRA_REFERENCIA) ?: "")

        findViewById<MaterialCardView>(R.id.btn_regresar).setOnClickListener { finish() }
        btnReintentar.setOnClickListener { presenter.cotizar() }
        btnCambiar.setOnClickListener { cambiarDestino() }
        btnPedir.setOnClickListener { presenter.pedirTaxi(etReferencia.text?.toString() ?: "") }

        val prefs = getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        presenter.iniciar(prefs.getString("token", "") ?: "", origen, destino)
    }

    override fun onDestroy() {
        presenter.destruir()
        super.onDestroy()
    }

    // "Cambiar": regresa al buscador; si esta pantalla se abrió desde Mi viaje, abre uno nuevo
    private fun cambiarDestino() {
        val intent = Intent(this, ActivityElegirDestino::class.java)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_NOMBRE, origen.nombre)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_DETALLE, origen.detalle)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_LAT, origen.lat)
        intent.putExtra(ActivityElegirDestino.EXTRA_ORIGEN_LNG, origen.lng)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun actualizarBotonPedir(pidiendo: Boolean) {
        val activo = hayCotizacion && !pidiendo
        btnPedir.isEnabled = activo
        btnPedir.alpha = if (activo) 1f else 0.5f
        btnPedir.text = if (pidiendo) "Pidiendo…" else "Pedir taxi"
        btnCambiar.isEnabled = !pidiendo
    }

    fun mostrarDestino(nombre: String) {
        tvDestino.text = nombre
    }

    fun mostrarPuntos(origen: dataLugar, destino: dataLugar) {
        mapa.ponerOrigen(origen.lat, origen.lng)
        mapa.ponerDestino(destino.lat, destino.lng)
        mapa.encuadrar(listOf(Pair(origen.lat, origen.lng), Pair(destino.lat, destino.lng)))
    }

    fun mostrarCotizando(cotizando: Boolean) {
        if (cotizando) {
            hayCotizacion = false
            cardTarifa.visibility = View.GONE
            grupoError.visibility = View.GONE
        }
        grupoCotizando.visibility = if (cotizando) View.VISIBLE else View.GONE
        actualizarBotonPedir(false)
    }

    fun mostrarCotizacion(cotizacion: dataCotizacion) {
        hayCotizacion = true
        tvDistanciaTiempo.text = "${cotizacion.distanciaKm} km · ${cotizacion.duracionMin} min"
        tvTarifa.text = "$${cotizacion.tarifa}"
        cardTarifa.visibility = View.VISIBLE
        mapa.ponerRuta(cotizacion.ruta)
        val puntosRuta = cotizacion.ruta.filter { it.size >= 2 }.map { Pair(it[1], it[0]) }
        if (puntosRuta.size >= 2) mapa.encuadrar(puntosRuta)
        actualizarBotonPedir(false)
    }

    fun mostrarErrorCotizacion(mensaje: String, sePuedeReintentar: Boolean) {
        tvError.text = mensaje
        btnReintentar.visibility = if (sePuedeReintentar) View.VISIBLE else View.GONE
        grupoError.visibility = View.VISIBLE
    }

    fun mostrarPidiendo(pidiendo: Boolean) {
        actualizarBotonPedir(pidiendo)
    }

    fun mostrarErrorReferencia(mensaje: String?) {
        tilReferencia.error = mensaje
    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    // P08: viaje creado. Se regresa al inicio, que abre Mi viaje (Módulo 5) con el folio.
    fun viajeCreado(folio: String) {
        val intent = Intent(this, ActivityInicioPasajero::class.java)
        intent.putExtra(ActivityInicioPasajero.EXTRA_FOLIO_NUEVO, folio)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
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
