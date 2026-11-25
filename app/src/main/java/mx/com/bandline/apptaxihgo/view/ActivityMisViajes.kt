package mx.com.bandline.apptaxihgo.view
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.adapter.ViajeAdapter
import mx.com.bandline.apptaxihgo.model.dataTotales
import mx.com.bandline.apptaxihgo.model.dataViajeResumen
import mx.com.bandline.apptaxihgo.presenter.MisViajesPresenter
class ActivityMisViajes : AppCompatActivity(){

    private lateinit var presenter: MisViajesPresenter
    private lateinit var adaptador: ViajeAdapter
    private lateinit var rvViajes: RecyclerView
    private lateinit var tvVacio: TextView
    private lateinit var pbCargando: ProgressBar
    private lateinit var grupoTotales: LinearLayout
    private lateinit var tvFinalizados: TextView
    private lateinit var tvTotalTarifas: TextView
    private lateinit var tgPeriodo: MaterialButtonToggleGroup
    private lateinit var btnRegresar: MaterialButton

    private var token = ""
    private var rol = "PASAJERO"
    private var periodo = "30"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_viajes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvViajes = findViewById(R.id.rv_viajes)
        tvVacio = findViewById(R.id.tv_vacio)
        pbCargando = findViewById(R.id.pb_cargando)
        grupoTotales = findViewById(R.id.grupo_totales)
        tvFinalizados = findViewById(R.id.tv_finalizados)
        tvTotalTarifas = findViewById(R.id.tv_total_tarifas)
        tgPeriodo = findViewById(R.id.tg_periodo)
        btnRegresar = findViewById(R.id.btn_regresar)

        token = clsNavegacion.token(this)
        rol = clsNavegacion.rol(this)
        if (token.isEmpty()) {
            clsNavegacion.irALogin(this)
            return
        }

        presenter = MisViajesPresenter(this)
        adaptador = ViajeAdapter(this, mutableListOf())
        val layoutManager = LinearLayoutManager(this)
        rvViajes.layoutManager = layoutManager
        rvViajes.adapter = adaptador

        rvViajes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && layoutManager.findLastVisibleItemPosition() >= adaptador.itemCount - 3) {
                    presenter.cargarMas(token)
                }
            }
        })
        tgPeriodo.check(R.id.btn_30_dias)
        tgPeriodo.addOnButtonCheckedListener { _, idBoton, marcado ->
            if (marcado) {
                periodo = when (idBoton) {
                    R.id.btn_7_dias -> "7"
                    R.id.btn_todo -> "todo"
                    else -> "30"
                }
                presenter.cargar(token, periodo)
            }
        }

        btnRegresar.setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        if (token.isNotEmpty()) {
            // Se recarga al volver del detalle o de Mi viaje (el estado pudo cambiar)
            presenter.cargar(token, periodo)
        }
    }
    fun mostrarViajes(viajes: List<dataViajeResumen>, agregar: Boolean) {
        tvVacio.visibility = View.GONE
        rvViajes.visibility = View.VISIBLE
        if (agregar) {
            adaptador.agregarViajes(viajes)
        } else {
            adaptador.actualizarLista(viajes)
            rvViajes.scrollToPosition(0)
        }
    }

    fun mostrarTotales(totales: dataTotales?) {
        if (totales == null) {
            grupoTotales.visibility = View.GONE
        } else {
            grupoTotales.visibility = View.VISIBLE
            tvFinalizados.text = totales.finalizados.toString()
            tvTotalTarifas.text = clsFormatoViaje.moneda(totales.totalTarifas)
        }
    }
    fun mostrarVacio(mensaje: String) {
        adaptador.actualizarLista(emptyList())
        rvViajes.visibility = View.GONE
        tvVacio.visibility = View.VISIBLE
        tvVacio.text = mensaje
    }

    fun mostrarCargando(cargando: Boolean) {
        pbCargando.visibility = if (cargando) View.VISIBLE else View.GONE
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    fun sesionExpirada() {
        clsNavegacion.irALogin(this)
    }

    fun seleccionarViaje(viaje: dataViajeResumen) {
        val activo = viaje.estado == "BUSCANDO" || viaje.estado == "ASIGNADO" ||
                viaje.estado == "EN_ORIGEN" || viaje.estado == "EN_CURSO"

        val intent = if (activo && rol == "PASAJERO") {
            // Un viaje que sigue en marcha se abre en "Mi viaje"
            Intent(this, ActivityViajePasajero::class.java)
        } else {
            Intent(this, ActivityDetalleViaje::class.java)
        }
        intent.putExtra(clsNavegacion.EXTRA_ID_VIAJE, viaje.idViaje)
        startActivity(intent)
    }
}