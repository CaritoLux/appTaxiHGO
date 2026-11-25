package mx.com.bandline.apptaxihgo.view

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataViaje
import mx.com.bandline.apptaxihgo.presenter.DetalleViajePresenter


class ActivityDetalleViaje : AppCompatActivity() {

    private lateinit var presenter: DetalleViajePresenter
    private lateinit var pbCargando: ProgressBar
    private lateinit var tvFolio: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvOrigen: TextView
    private lateinit var tvDestino: TextView
    private lateinit var tvDistancia: TextView
    private lateinit var tvTarifa: TextView
    private lateinit var tvEtiquetaPersona: TextView
    private lateinit var tvPersona: TextView
    private lateinit var tvMiCalificacion: TextView
    private lateinit var tvMotivo: TextView
    private lateinit var btnCalificar: MaterialButton
    private lateinit var btnReportar: MaterialButton
    private lateinit var btnRegresar: MaterialButton

    private var token = ""
    private var idViaje = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_viaje)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pbCargando = findViewById(R.id.pb_cargando)
        tvFolio = findViewById(R.id.tv_folio)
        tvEstado = findViewById(R.id.tv_estado)
        tvFecha = findViewById(R.id.tv_fecha)
        tvOrigen = findViewById(R.id.tv_origen)
        tvDestino = findViewById(R.id.tv_destino)
        tvDistancia = findViewById(R.id.tv_distancia)
        tvTarifa = findViewById(R.id.tv_tarifa)
        tvEtiquetaPersona = findViewById(R.id.tv_etiqueta_persona)
        tvPersona = findViewById(R.id.tv_persona)
        tvMiCalificacion = findViewById(R.id.tv_mi_calificacion)
        tvMotivo = findViewById(R.id.tv_motivo)
        btnCalificar = findViewById(R.id.btn_calificar)
        btnReportar = findViewById(R.id.btn_reportar)
        btnRegresar = findViewById(R.id.btn_regresar)

        btnRegresar.setOnClickListener { finish() }
        btnCalificar.setOnClickListener { clsNavegacion.irACalificar(this, idViaje) } // Módulo 3
        btnReportar.setOnClickListener { clsNavegacion.irAReportar(this, idViaje) }   // Módulo 2

        token = clsNavegacion.token(this)
        idViaje = intent.getIntExtra(clsNavegacion.EXTRA_ID_VIAJE, 0)
        if (token.isEmpty()) {
            clsNavegacion.irALogin(this)
            return
        }
        presenter = DetalleViajePresenter(this)
    }

    override fun onStart() {
        super.onStart()
        if (token.isNotEmpty()) {
            presenter.cargar(token, idViaje)
        }
    }

    fun mostrarDetalle(v: dataViaje) {
        tvFolio.text = v.folio
        clsFormatoViaje.pintarEstado(tvEstado, v.estado, v.estadoTexto)

        val inicio = clsFormatoViaje.fechaLarga(v.fechaInicio ?: v.fechaSolicitud)
        val fin = clsFormatoViaje.hora(v.fechaFin)
        tvFecha.text = if (fin.isEmpty()) inicio else "$inicio a $fin"

        tvOrigen.text = v.origen.direccion
        tvDestino.text = v.destino.direccion
        tvDistancia.text = "" + v.distanciaKm + " km"
        tvTarifa.text = if (v.estado == "FINALIZADO") clsFormatoViaje.moneda(v.tarifa) else "—"

        if (clsNavegacion.rol(this) == "CONDUCTOR") {
            tvEtiquetaPersona.text = "Pasajero"
            tvPersona.text = v.pasajero?.nombre ?: "—"
        } else {
            tvEtiquetaPersona.text = "Conductor"
            val c = v.conductor
            tvPersona.text = if (c == null) "—" else c.nombre + " · taxi " + (c.numeroEconomico ?: "")
        }

        tvMiCalificacion.text = if (v.miCalificacion == null) "Sin calificar" else "★ " + v.miCalificacion + " de 5"

        if (v.motivoCancelacion.isNullOrEmpty()) {
            tvMotivo.visibility = View.GONE
        } else {
            tvMotivo.visibility = View.VISIBLE
            tvMotivo.text = "Motivo de cancelación: " + v.motivoCancelacion
        }

        btnCalificar.visibility = if (v.puedeCalificar) View.VISIBLE else View.GONE
        btnReportar.visibility = if (v.puedeReportar) View.VISIBLE else View.GONE
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
}
