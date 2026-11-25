package mx.com.bandline.apptaxihgo.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.Destino
import mx.com.bandline.apptaxihgo.model.dataViaje
import mx.com.bandline.apptaxihgo.presenter.ViajeConductorContract
import mx.com.bandline.apptaxihgo.presenter.ViajeConductorPresenter
import mx.com.bandline.apptaxihgo.model.clsGeo
import mx.com.bandline.apptaxihgo.model.clsUbicacion


class ActivityViajeConductor : clsActividadBase(), ViajeConductorContract.View {

    companion object {
        private const val ESPERA_NO_SE_PRESENTO_S = 300
    }

    private lateinit var presenter: ViajeConductorContract.Presenter
    private lateinit var mapa: clsMapa
    private lateinit var ubicacion: clsUbicacion
    private lateinit var hoja: View
    private lateinit var tvEtiqueta: TextView
    private lateinit var tvTitulo: TextView
    private lateinit var btnPrincipal: MaterialButton
    private lateinit var btnCancelar: MaterialButton
    private var viaje: dataViaje? = null
    private var hojaMotivos: clsHojaMotivos? = null
    private var yo: Location? = null
    private var estadoMostrado = ""
    private var llegadaBase = 0L
    private val reloj = Handler(Looper.getMainLooper())
    private val tic = object : Runnable {
        override fun run() {
            mostrarEspera()
            reloj.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viaje_conductor)
        ajustarBordes(findViewById(R.id.capa_superior), abajo = false, consumir = false)
        ajustarBordes(findViewById(R.id.contenido_hoja), arriba = false, consumir = false)
        presenter = ViajeConductorPresenter(this)
        presenter.iniciar(intent.getIntExtra(clsNavegacion.EXTRA_ID_VIAJE, 0))
        ubicacion = clsUbicacion(this)
        mapa = clsMapa(findViewById(R.id.mapa), this, savedInstanceState)

        hoja = findViewById(R.id.hoja)
        tvEtiqueta = findViewById(R.id.tv_etiqueta)
        tvTitulo = findViewById(R.id.tv_titulo)
        btnPrincipal = findViewById(R.id.btn_principal)
        btnCancelar = findViewById(R.id.btn_cancelar)

        btnPrincipal.setOnClickListener { accionPrincipal() }
        btnCancelar.setOnClickListener { presenter.pedirCancelar() }
        findViewById<MaterialButton>(R.id.btn_navegar).setOnClickListener { navegar() }
        findViewById<MaterialButton>(R.id.btn_llamar).setOnClickListener { llamar() }
    }

    override fun onStart() {
        super.onStart()
        presenter.reanudar()
        conPermisoUbicacion(alConceder = {
            ubicacion.obtenerActual { l -> l?.let { nuevaLectura(it) } }
            ubicacion.iniciar(4000) { nuevaLectura(it) }
        })
        reloj.post(tic)
    }

    override fun onStop() {
        presenter.pausar()
        ubicacion.detener()
        reloj.removeCallbacks(tic)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapa.guardarEstado(outState)
    }

    private fun nuevaLectura(l: Location) {
        yo = l
        mapa.ponerYo(l.latitude, l.longitude)
        presenter.ubicacionNueva(l.latitude, l.longitude)
        if (viaje?.estado == "ASIGNADO") mostrarDistanciaAlOrigen()
    }

    override fun mostrarViaje(viaje: dataViaje) {
        this.viaje = viaje
        val cambio = viaje.estado != estadoMostrado
        estadoMostrado = viaje.estado
        val p = viaje.pasajero
        findViewById<TextView>(R.id.tv_pasajero).text = p?.nombre ?: ""
        findViewById<TextView>(R.id.tv_iniciales).text = clsFormato.iniciales(p?.nombre ?: "")
        findViewById<TextView>(R.id.tv_calificacion).text = clsFormato.calificacion(p?.calificacion)
        findViewById<View>(R.id.btn_llamar).visibility = if (p?.telefono.isNullOrBlank()) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.tv_origen).text = viaje.origen.direccion
        findViewById<TextView>(R.id.tv_destino).text = viaje.destino.direccion
        val tvReferencia: TextView = findViewById(R.id.tv_referencia)
        tvReferencia.text = getString(R.string.referencia_de, viaje.referencia ?: "")
        tvReferencia.visibility = if (viaje.referencia.isNullOrBlank() || viaje.estado == "EN_CURSO") View.GONE else View.VISIBLE
        btnCancelar.visibility = if (viaje.puedeCancelar) View.VISIBLE else View.GONE

        when (viaje.estado) {
            "ASIGNADO" -> {
                tvEtiqueta.setText(R.string.etiqueta_ve_por_pasajero)
                mostrarDistanciaAlOrigen()
                btnPrincipal.setText(R.string.llegue)
            }
            "EN_ORIGEN" -> {
                tvEtiqueta.setText(R.string.etiqueta_llegaste)
                val esperado = ESPERA_NO_SE_PRESENTO_S - (viaje.segundosParaNoSePresento ?: ESPERA_NO_SE_PRESENTO_S)
                llegadaBase = SystemClock.elapsedRealtime() - esperado * 1000L
                mostrarEspera()
                btnPrincipal.setText(R.string.iniciar_viaje)
            }
            else -> { // EN_CURSO
                tvEtiqueta.text = getString(R.string.etiqueta_cobra, clsFormato.moneda(viaje.tarifa))
                tvTitulo.text = getString(R.string.hacia, viaje.destino.direccion.substringBefore(","))
                btnPrincipal.setText(R.string.finalizar_viaje)
            }
        }

        mapa.ponerOrigen(viaje.origen.lat, viaje.origen.lng)
        mapa.ponerDestino(if (viaje.estado == "EN_CURSO") viaje.destino.lat else null, if (viaje.estado == "EN_CURSO") viaje.destino.lng else null)
        mapa.ponerRuta(if (viaje.estado == "EN_CURSO") viaje.ruta else emptyList())
        if (cambio) {
            val puntos = mutableListOf<Pair<Double, Double>>()
            yo?.let { puntos.add(it.latitude to it.longitude) }
            if (viaje.estado == "EN_CURSO") {
                puntos.add(viaje.destino.lat to viaje.destino.lng)
                viaje.ruta.filter { it.size >= 2 }.forEach { puntos.add(it[0] to it[1]) }
            } else {
                puntos.add(viaje.origen.lat to viaje.origen.lng)
            }
            hoja.post { mapa.encuadrar(puntos, hoja.height) }
        }
    }

    private fun mostrarDistanciaAlOrigen() {
        val v = viaje ?: return
        val l = yo
        tvTitulo.text = if (l == null) v.origen.direccion.substringBefore(",") else getString(
            R.string.a_metros_del_punto,
            clsFormato.metros(clsGeo.distanciaMetros(l.latitude, l.longitude, v.origen.lat, v.origen.lng).toInt())
        )
    }

    /** "Esperando a Ana R. · 2:15" (cuenta desde que marcó Llegué). */
    private fun mostrarEspera() {
        val v = viaje ?: return
        if (v.estado != "EN_ORIGEN") return
        val segundos = ((SystemClock.elapsedRealtime() - llegadaBase) / 1000).toInt()
        tvTitulo.text = getString(R.string.esperando_a, v.pasajero?.nombre ?: "", clsFormato.cronometro(segundos))
    }

    private fun accionPrincipal() {
        val v = viaje ?: return
        when (v.estado) {
            "ASIGNADO" -> presenter.llegue()
            "EN_ORIGEN" -> mostrarHoja(
                getString(R.string.confirmar_inicio_titulo), getString(R.string.confirmar_inicio_mensaje),
                getString(R.string.si_iniciar), icono = R.drawable.ic_persona,
                textoSecundario = getString(R.string.todavia_no), alPrincipal = { presenter.iniciarViaje() }
            )
            "EN_CURSO" -> mostrarHoja(
                getString(R.string.confirmar_fin_titulo), getString(R.string.cobra_efectivo, clsFormato.moneda(v.tarifa)),
                getString(R.string.finalizar_viaje), icono = R.drawable.ic_efectivo,
                textoSecundario = getString(R.string.seguir_en_viaje), alPrincipal = { presenter.finalizar() }
            )
        }
    }

    /** Abre Google Maps / OsmAnd / cualquier app de mapas con el punto de recogida o el destino. */
    private fun navegar() {
        val v = viaje ?: return
        val p = if (v.estado == "EN_CURSO") v.destino else v.origen
        val uri = Uri.parse("geo:${p.lat},${p.lng}?q=${p.lat},${p.lng}(" + Uri.encode(p.direccion.substringBefore(",")) + ")")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            snackbar(getString(R.string.sin_app_mapas))
        }
    }

    private fun llamar() {
        val telefono = viaje?.pasajero?.telefono ?: return
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
        } catch (e: ActivityNotFoundException) {
            snackbar(getString(R.string.sin_telefono))
        }
    }

    override fun viajeFinalizado(viaje: dataViaje) {
        // Calificar al pasajero es del Módulo 3; si todavía no está en el proyecto se regresa al inicio
        if (clsNavegacion.irACalificar(this, viaje)) finish() else irA(Destino.InicioConductor)
    }

    // ---------------------------------------------------------------- Cancelar (C22)

    override fun mostrarMotivos(motivos: List<String>, segundosParaNoSePresento: Int?) {
        // "El pasajero no se presentó" se habilita 5 min después de marcar Llegué (la API también lo revisa)
        val bloqueado = segundosParaNoSePresento == null || segundosParaNoSePresento > 0
        val ayuda = if (segundosParaNoSePresento == null) getString(R.string.no_se_presento_antes)
        else getString(R.string.no_se_presento_espera, clsFormato.cronometro(segundosParaNoSePresento))
        hojaMotivos = clsHojaMotivos(
            this, getString(R.string.cancelar_viaje), motivos,
            alConfirmar = { motivo, comentario -> presenter.cancelar(motivo, comentario) },
            motivoBloqueado = if (bloqueado) ViajeConductorPresenter.NO_SE_PRESENTO else null,
            ayudaBloqueado = ayuda
        ).also { it.mostrar() }
    }

    override fun mostrarErroresCancelar(errores: Map<String, String>) {
        hojaMotivos?.mostrarErrores(errores)
    }

    override fun mostrarCargandoCancelar(mostrar: Boolean) {
        hojaMotivos?.mostrarCargando(mostrar)
    }

    override fun cancelasteViaje(mensaje: String) {
        hojaMotivos?.cerrar()
        irA(Destino.InicioConductor, mensaje)
    }

    override fun viajeCancelado(mensaje: String) {
        hojaMotivos?.cerrar()
        mostrarHoja(
            getString(R.string.cancelado_pasajero_titulo), getString(R.string.sigues_disponible), getString(R.string.entendido),
            icono = R.drawable.ic_info, cancelable = false, alPrincipal = { irA(Destino.InicioConductor) }
        )
    }

    override fun mostrarSinConexion(sinConexion: Boolean) {
        if (sinConexion) mostrarAviso(getString(R.string.sin_conexion_reintentando), TipoAviso.ERROR) else ocultarAviso()
    }

    override fun mostrarCargando(mostrar: Boolean) {
        super.mostrarCargando(mostrar)
        btnPrincipal.isEnabled = !mostrar
    }

    override fun onDestroy() {
        hojaMotivos?.cerrar()
        reloj.removeCallbacksAndMessages(null)
        presenter.destruir()
        super.onDestroy()
    }
}
