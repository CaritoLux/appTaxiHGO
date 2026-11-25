package mx.com.bandline.apptaxihgo.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.adapter.LugaresAdapter
import mx.com.bandline.apptaxihgo.model.dataLugar
import mx.com.bandline.apptaxihgo.presenter.ElegirDestinoPresenter
import org.maplibre.android.MapLibre

// Módulo 3 · Elegir destino: buscador de lugares o fijar el destino en el mapa
class ActivityElegirDestino : AppCompatActivity() {

    companion object {
        const val EXTRA_ORIGEN_NOMBRE = "ORIGEN_NOMBRE"
        const val EXTRA_ORIGEN_DETALLE = "ORIGEN_DETALLE"
        const val EXTRA_ORIGEN_LAT = "ORIGEN_LAT"
        const val EXTRA_ORIGEN_LNG = "ORIGEN_LNG"
    }

    private lateinit var presenter: ElegirDestinoPresenter
    private lateinit var adaptador: LugaresAdapter
    private lateinit var contenidoLista: LinearLayout
    private lateinit var contenidoMapa: LinearLayout
    private lateinit var tvOrigen: TextView
    private lateinit var etDestino: TextInputEditText
    private lateinit var pbBuscando: LinearProgressIndicator
    private lateinit var tvEstado: TextView
    private lateinit var tvDireccionMapa: TextView
    private lateinit var btnConfirmarDestino: MaterialButton
    private var mapa: clsMapa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_elegir_destino)
        // Con el teclado abierto la lista se recorta para no quedar debajo de él
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left, barras.top, barras.right, maxOf(barras.bottom, teclado.bottom))
            WindowInsetsCompat.CONSUMED
        }

        contenidoLista = findViewById(R.id.contenido_lista)
        contenidoMapa = findViewById(R.id.contenido_mapa)
        tvOrigen = findViewById(R.id.tv_origen)
        etDestino = findViewById(R.id.et_destino)
        pbBuscando = findViewById(R.id.pb_buscando)
        tvEstado = findViewById(R.id.tv_estado)
        tvDireccionMapa = findViewById(R.id.tv_direccion_mapa)
        btnConfirmarDestino = findViewById(R.id.btn_confirmar_destino)

        presenter = ElegirDestinoPresenter(this)
        adaptador = LugaresAdapter(emptyList()) { lugar -> presenter.lugarElegido(lugar) }
        val rvLugares = findViewById<RecyclerView>(R.id.rv_lugares)
        rvLugares.layoutManager = LinearLayoutManager(this)
        rvLugares.adapter = adaptador

        findViewById<MaterialCardView>(R.id.btn_regresar).setOnClickListener { finish() }
        findViewById<MaterialCardView>(R.id.btn_regresar_mapa).setOnClickListener { ocultarModoMapa() }
        findViewById<LinearLayout>(R.id.fila_mapa).setOnClickListener { presenter.abrirMapa() }
        btnConfirmarDestino.setOnClickListener { presenter.confirmarPuntoMapa() }
        etDestino.doAfterTextChanged { texto -> presenter.textoCambiado(texto?.toString() ?: "") }
        etDestino.setOnEditorActionListener { _, accion, _ ->
            if (accion == EditorInfo.IME_ACTION_SEARCH) ocultarTeclado()
            accion == EditorInfo.IME_ACTION_SEARCH
        }
        // Atrás: del mapa regresa al buscador; del buscador regresa al inicio
        onBackPressedDispatcher.addCallback(this) {
            if (contenidoMapa.visibility == View.VISIBLE) ocultarModoMapa() else finish()
        }

        val origen = dataLugar(
            nombre = intent.getStringExtra(EXTRA_ORIGEN_NOMBRE) ?: "",
            detalle = intent.getStringExtra(EXTRA_ORIGEN_DETALLE) ?: "",
            lat = intent.getDoubleExtra(EXTRA_ORIGEN_LAT, 0.0),
            lng = intent.getDoubleExtra(EXTRA_ORIGEN_LNG, 0.0)
        )
        presenter.iniciar(origen)
        etDestino.requestFocus()
    }

    override fun onDestroy() {
        presenter.destruir()
        super.onDestroy()
    }

    private fun ocultarTeclado() {
        val teclado = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        teclado.hideSoftInputFromWindow(etDestino.windowToken, 0)
    }

    fun mostrarOrigen(texto: String) {
        tvOrigen.text = texto
    }

    fun mostrarLugares(lista: List<dataLugar>) {
        adaptador.actualizarLista(lista)
    }

    fun mostrarCargando(cargando: Boolean) {
        pbBuscando.visibility = if (cargando) View.VISIBLE else View.INVISIBLE
    }

    fun mostrarEstado(texto: String?) {
        tvEstado.text = texto ?: ""
        tvEstado.visibility = if (texto.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    // P04: el mapa se crea la primera vez que se abre
    fun mostrarModoMapa(lat: Double, lng: Double) {
        ocultarTeclado()
        contenidoMapa.visibility = View.VISIBLE
        val mapaDestino = mapa ?: clsMapa(this, findViewById(R.id.mapa_destino), lifecycle).also {
            it.alDetenerCamara { latitud, longitud -> presenter.mapaDetenido(latitud, longitud) }
            mapa = it
        }
        mapaDestino.centrar(lat, lng)
    }

    private fun ocultarModoMapa() {
        contenidoMapa.visibility = View.GONE
    }

    fun mostrarDireccionMapa(texto: String, listo: Boolean) {
        tvDireccionMapa.text = texto
        btnConfirmarDestino.isEnabled = listo
        btnConfirmarDestino.alpha = if (listo) 1f else 0.5f
    }

    fun abrirConfirmar(origen: dataLugar, destino: dataLugar) {
        ocultarTeclado()
        startActivity(ActivityConfirmarViaje.crearIntent(this, origen, destino, ""))
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}
