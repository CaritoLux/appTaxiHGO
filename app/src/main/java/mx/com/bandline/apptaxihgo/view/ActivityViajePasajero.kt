package mx.com.bandline.apptaxihgo.view
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataViaje
import mx.com.bandline.apptaxihgo.presenter.EmergenciaPresenter
import mx.com.bandline.apptaxihgo.presenter.ViajePasajeroPresenter
class ActivityViajePasajero : AppCompatActivity(){

    private lateinit var presenter: ViajePasajeroPresenter
    private lateinit var presenterEmergencia: EmergenciaPresenter
    private lateinit var botonEmergencia: BotonEmergencia

    private lateinit var tvSinConexion: TextView
    private lateinit var tvEtiqueta: TextView
    private lateinit var tvTitulo: TextView
    private lateinit var tvMensaje: TextView
    private lateinit var pbBuscando: ProgressBar
    private lateinit var grupoConductor: LinearLayout
    private lateinit var tvIniciales: TextView
    private lateinit var tvConductor: TextView
    private lateinit var tvCalificacion: TextView
    private lateinit var tvVehiculo: TextView
    private lateinit var tvPlacas: TextView
    private lateinit var tvNumeroTaxi: TextView
    private lateinit var grupoAcciones: LinearLayout
    private lateinit var btnCompartir: MaterialButton
    private lateinit var btnReportar: MaterialButton
    private lateinit var grupoEmergencia: View
    private lateinit var tvOrigen: TextView
    private lateinit var tvDestino: TextView
    private lateinit var tvTarifa: TextView
    private lateinit var btnPrincipal: MaterialButton
    private lateinit var btnSecundario: MaterialButton

    private var token = ""
    private var viaje: dataViaje? = null

    private var dialogoCancelar: AlertDialog? = null
    private var tvErrorCancelar: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_viaje_pasajero)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSinConexion = findViewById(R.id.tv_sin_conexion)
        tvEtiqueta = findViewById(R.id.tv_etiqueta)
        tvTitulo = findViewById(R.id.tv_titulo)
        tvMensaje = findViewById(R.id.tv_mensaje)
        pbBuscando = findViewById(R.id.pb_buscando)
        grupoConductor = findViewById(R.id.grupo_conductor)
        tvIniciales = findViewById(R.id.tv_iniciales)
        tvConductor = findViewById(R.id.tv_conductor)
        tvCalificacion = findViewById(R.id.tv_calificacion)
        tvVehiculo = findViewById(R.id.tv_vehiculo)
        tvPlacas = findViewById(R.id.tv_placas)
        tvNumeroTaxi = findViewById(R.id.tv_numero_taxi)
        grupoAcciones = findViewById(R.id.grupo_acciones)
        btnCompartir = findViewById(R.id.btn_compartir)
        btnReportar = findViewById(R.id.btn_reportar)
        grupoEmergencia = findViewById(R.id.grupo_emergencia)
        tvOrigen = findViewById(R.id.tv_origen)
        tvDestino = findViewById(R.id.tv_destino)
        tvTarifa = findViewById(R.id.tv_tarifa)
        btnPrincipal = findViewById(R.id.btn_principal)
        btnSecundario = findViewById(R.id.btn_secundario)
        token = clsNavegacion.token(this)
        if (token.isEmpty()) {
            clsNavegacion.irALogin(this)
            return
        }

        presenter = ViajePasajeroPresenter(this)
        presenterEmergencia = EmergenciaPresenter(this)
        botonEmergencia = BotonEmergencia(
            this,
            findViewById(R.id.fondo_emergencia),
            findViewById(R.id.pb_emergencia),
            findViewById(R.id.tv_emergencia)
        )

        btnPrincipal.setOnClickListener { accionPrincipal() }
        btnSecundario.setOnClickListener { accionSecundaria() }
        btnCompartir.setOnClickListener { compartirViaje() }
        btnReportar.setOnClickListener {
            val v = viaje
            if (v != null) {
                clsNavegacion.irAReportar(this, v.idViaje) // Módulo 2
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (token.isNotEmpty()) {
            presenter.iniciarConsulta(token, intent.getIntExtra(clsNavegacion.EXTRA_ID_VIAJE, 0))
        }
    }

    override fun onStop() {
        if (token.isNotEmpty()) {
            presenter.detenerConsulta()
            botonEmergencia.detener()
        }
        super.onStop()
    }

    fun mostrarViaje(v: dataViaje) {
        viaje = v

        tvOrigen.text = v.origen.direccion
        tvDestino.text = v.destino.direccion
        tvTarifa.text = clsFormatoViaje.moneda(v.tarifa) + " · efectivo"
        mostrarConductor(v)

        // Primero se oculta todo; cada estado prende lo suyo
        tvEtiqueta.visibility = View.GONE
        tvMensaje.visibility = View.VISIBLE
        pbBuscando.visibility = View.GONE
        grupoConductor.visibility = View.GONE
        grupoAcciones.visibility = View.GONE
        btnReportar.visibility = View.GONE
        grupoEmergencia.visibility = View.GONE
        btnPrincipal.visibility = View.GONE
        btnSecundario.visibility = View.GONE
        estiloSecundario(true)

        when (v.estado) {
            "BUSCANDO" -> {
                tvTitulo.text = "Buscando taxi…"
                tvMensaje.text = "Te avisaremos en cuanto un conductor acepte tu viaje."
                pbBuscando.visibility = View.VISIBLE
                mostrarCancelar("Cancelar solicitud", v)
            }
            "ASIGNADO" -> {
                etiqueta("TU TAXI VA EN CAMINO")
                tvTitulo.text = "Llega en aprox. " + (v.etaMin ?: 5) + " min"
                tvMensaje.visibility = View.GONE
                grupoConductor.visibility = View.VISIBLE
                grupoAcciones.visibility = View.VISIBLE
                grupoEmergencia.visibility = View.VISIBLE
                mostrarCancelar("Cancelar viaje", v)
            }
            "EN_ORIGEN" -> {
                etiqueta("TU TAXI LLEGÓ")
                tvTitulo.text = "Tu taxi llegó"
                tvMensaje.text = v.mensaje
                grupoConductor.visibility = View.VISIBLE
                grupoAcciones.visibility = View.VISIBLE
                grupoEmergencia.visibility = View.VISIBLE
                mostrarCancelar("Cancelar viaje", v)
            }
            "EN_CURSO" -> {
                etiqueta("VIAJE EN CURSO")
                val destinoCorto = v.destino.direccion.substringBefore(",")
                tvTitulo.text = "Hacia " + destinoCorto + " · " + clsFormatoViaje.horaEnMinutos(v.etaMin ?: v.duracionMin)
                tvMensaje.visibility = View.GONE
                grupoConductor.visibility = View.VISIBLE
                grupoAcciones.visibility = View.VISIBLE
                btnReportar.visibility = View.VISIBLE
                grupoEmergencia.visibility = View.VISIBLE
            }
            "SIN_CONDUCTOR" -> {
                tvTitulo.text = "No hay taxis disponibles"
                tvMensaje.text = v.mensaje
                principal("Volver a intentar")
                secundarioInicio()
            }
            "CANCELADO" -> {
                if (v.canceladoPor == "CONDUCTOR") {
                    tvTitulo.text = "El conductor canceló tu viaje"
                    tvMensaje.text = v.mensaje
                    principal("Buscar otro taxi")
                    secundarioInicio()
                } else {
                    tvTitulo.text = "Viaje cancelado"
                    tvMensaje.text = "Motivo: " + (v.motivoCancelacion ?: "—")
                    principal("Volver al inicio")
                }
            }
            else -> { // FINALIZADO (ya calificado o Calificar todavía no está en el proyecto)
                tvTitulo.text = "Viaje terminado"
                tvMensaje.text = v.mensaje
                principal("Volver al inicio")
            }
        }
    }

    private fun mostrarConductor(v: dataViaje) {
        val c = v.conductor ?: return
        tvIniciales.text = clsFormatoViaje.iniciales(c.nombre)
        tvConductor.text = c.nombre
        tvCalificacion.text = "★ " + c.calificacion
        tvVehiculo.text = (c.vehiculo ?: "") + " · " + (c.color ?: "")
        tvPlacas.text = c.placas ?: ""
        tvNumeroTaxi.text = "Taxi " + (c.numeroEconomico ?: "")
    }

    private fun etiqueta(texto: String) {
        tvEtiqueta.text = texto
        tvEtiqueta.visibility = View.VISIBLE
    }

    private fun principal(texto: String) {
        btnPrincipal.text = texto
        btnPrincipal.visibility = View.VISIBLE
    }

    private fun mostrarCancelar(texto: String, v: dataViaje) {
        if (v.puedeCancelar) {
            btnSecundario.text = texto
            btnSecundario.visibility = View.VISIBLE
        }
    }

    private fun secundarioInicio() {
        btnSecundario.text = "Volver al inicio"
        btnSecundario.visibility = View.VISIBLE
        estiloSecundario(false)
    }

    // Rojo para cancelar; negro para "Volver al inicio"
    private fun estiloSecundario(peligro: Boolean) {
        btnSecundario.setTextColor(if (peligro) 0xFFD32F2F.toInt() else 0xFF111111.toInt())
    }

    fun mostrarSinConexion(sinConexion: Boolean) {
        tvSinConexion.visibility = if (sinConexion) View.VISIBLE else View.GONE
    }

    fun sinViajeActivo(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        finish()
    }

    fun irACalificar(v: dataViaje) {
        // Calificar es del Módulo 3. Si todavía no está, se queda el resumen del viaje terminado.
        if (clsNavegacion.irACalificar(this, v.idViaje)) {
            finish()
        } else {
            mostrarViaje(v)
        }
    }

    fun irABuscarOtro(v: dataViaje) {
        val abierta = clsNavegacion.irAConfirmarViaje(
            this, v.origen.direccion, v.origen.lat, v.origen.lng,
            v.destino.direccion, v.destino.lat, v.destino.lng
        )
        if (abierta) {
            finish()
        }
    }
    fun sesionExpirada() {
        clsNavegacion.irALogin(this)
    }
    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
    private fun accionPrincipal() {
        val v = viaje ?: return
        if (v.estado == "SIN_CONDUCTOR" || (v.estado == "CANCELADO" && v.canceladoPor == "CONDUCTOR")) {
            presenter.buscarOtroTaxi()
        } else {
            clsNavegacion.irAlInicio(this)
        }
    }
    private fun accionSecundaria() {
        val v = viaje ?: return
        if (v.activo && v.puedeCancelar) {
            presenter.pedirCancelar()
        } else {
            clsNavegacion.irAlInicio(this)
        }
    }
    private fun compartirViaje() {
        val v = viaje ?: return
        val c = v.conductor
        val texto = "Voy en un taxi TaxiH-GO!: taxi " + (c?.numeroEconomico ?: "") +
                ", placas " + (c?.placas ?: "") + ", " + (c?.vehiculo ?: "") + " " + (c?.color ?: "") +
                ", conductor " + (c?.nombre ?: "") + ". Voy hacia " + v.destino.direccion + "."
        val envio = Intent(Intent.ACTION_SEND)
        envio.type = "text/plain"
        envio.putExtra(Intent.EXTRA_TEXT, texto)
        startActivity(Intent.createChooser(envio, "Compartir viaje con"))
    }
    fun mostrarDialogoCancelar(motivos: List<String>) {
        val vistaDialogo = layoutInflater.inflate(R.layout.dialog_cancelar_viaje, null)
        val rgMotivos: RadioGroup = vistaDialogo.findViewById(R.id.rg_motivos)
        val etComentario: EditText = vistaDialogo.findViewById(R.id.et_comentario)
        tvErrorCancelar = vistaDialogo.findViewById(R.id.tv_error_cancelar)
        for (motivo in motivos) {
            val opcion = RadioButton(this)
            opcion.id = View.generateViewId()
            opcion.text = motivo
            rgMotivos.addView(opcion)
        }
        val titulo = if (viaje?.estado == "BUSCANDO") "Cancelar solicitud" else "Cancelar viaje"
        val dialogo = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(vistaDialogo)
            .setPositiveButton("Sí, cancelar", null)
            .setNegativeButton("No, regresar", null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val elegido: RadioButton? = vistaDialogo.findViewById(rgMotivos.checkedRadioButtonId)
                presenter.cancelar(token, elegido?.text?.toString(), etComentario.text.toString())
            }
        }
        dialogoCancelar = dialogo
        dialogo.show()
    }
    fun mostrarErrorCancelar(mensaje: String) {
        val error = tvErrorCancelar
        if (error != null && dialogoCancelar?.isShowing == true) {
            error.text = mensaje
            error.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }
    fun mostrarCargandoCancelar(cargando: Boolean) {
        dialogoCancelar?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !cargando
        dialogoCancelar?.getButton(AlertDialog.BUTTON_POSITIVE)?.text = if (cargando) "Cancelando…" else "Sí, cancelar"
    }
    fun viajeCancelado(mensaje: String, v: dataViaje) {
        dialogoCancelar?.dismiss()
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        mostrarViaje(v)
    }
    fun enviarEmergencia() {
        val v = viaje
        if (v == null) {
            botonEmergencia.reiniciar()
            return
        }
        presenterEmergencia.enviar(token, v.idViaje)
    }
    fun alertaEnviada(mensaje: String, folio: String) {
        botonEmergencia.reiniciar()
        AlertDialog.Builder(this)
            .setTitle("Alerta enviada")
            .setMessage(mensaje + "\n\nFolio: " + folio)
            .setPositiveButton("Llamar al 911") { _, _ ->
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    fun errorEmergencia(mensaje: String) {
        botonEmergencia.reiniciar()
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
    override fun onDestroy() {
        dialogoCancelar?.dismiss()
        super.onDestroy()
    }
}