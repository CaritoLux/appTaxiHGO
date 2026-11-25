package mx.com.bandline.apptaxihgo.view

import android.content.res.ColorStateList
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.Destino
import mx.com.bandline.apptaxihgo.model.dataOferta
import mx.com.bandline.apptaxihgo.model.dataPanelConductor
import mx.com.bandline.apptaxihgo.presenter.InicioConductorContract
import mx.com.bandline.apptaxihgo.presenter.InicioConductorPresenter
import mx.com.bandline.apptaxihgo.model.clsUbicacion


class ActivityInicioConductor : clsActividadBase(), InicioConductorContract.View {

    private lateinit var presenter: InicioConductorContract.Presenter
    private lateinit var mapa: clsMapa
    private lateinit var ubicacion: clsUbicacion
    private lateinit var hojaOferta: HojaOferta
    private lateinit var btnDisponibilidad: MaterialButton
    private var ultima: Location? = null
    private var centrado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_conductor)
        ajustarBordes(findViewById(R.id.capa_superior), abajo = false, consumir = false)
        presenter = InicioConductorPresenter(this)
        ubicacion = clsUbicacion(this)
        mapa = clsMapa(findViewById(R.id.mapa), this, savedInstanceState)
        hojaOferta = HojaOferta(this, alResponder = { presenter.responder(it) }, alVencer = { presenter.ofertaVencida() })
        btnDisponibilidad = findViewById(R.id.btn_disponibilidad)
        btnDisponibilidad.setOnClickListener { if (presenter.estaDisponible()) presenter.desconectar() else conectar() }
        findViewById<MaterialButton>(R.id.btn_mi_ubicacion).setOnClickListener {
            ultima?.let { mapa.centrar(it.latitude, it.longitude) } ?: iniciarGps()
        }
        configurarNavegacion(R.id.nav_inicio)
    }

    override fun onStart() {
        super.onStart()
        presenter.cargarPanel()
        iniciarGps()
    }

    override fun onStop() {
        presenter.pausar()
        ubicacion.detener()
        hojaOferta.cerrar()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapa.guardarEstado(outState)
    }

    private fun iniciarGps() {
        conPermisoUbicacion(alConceder = {
            if (ubicacion.estaActivada()) {
                ubicacion.obtenerActual { lectura -> lectura?.let { nuevaLectura(it) } }
                ubicacion.iniciar(5000) { nuevaLectura(it) }
            }
        })
    }

    private fun nuevaLectura(lectura: Location) {
        ultima = lectura
        mapa.ponerYo(lectura.latitude, lectura.longitude)
        if (!centrado) {
            centrado = true
            mapa.centrar(lectura.latitude, lectura.longitude, 15.5)
        }
        presenter.ubicacionNueva(lectura.latitude, lectura.longitude)
    }

    private fun conectar() {
        conPermisoUbicacion(
            alConceder = {
                if (!ubicacion.estaActivada()) {
                    ubicacionNecesaria(getString(R.string.ubicacion_necesaria))
                } else {
                    iniciarGps()
                    val lectura = ultima ?: ubicacion.ultimaConocida()
                    if (lectura != null) {
                        presenter.conectar(lectura.latitude, lectura.longitude)
                    } else {
                        mostrarCargando(true)
                        ubicacion.obtenerActual { l ->
                            mostrarCargando(false)
                            presenter.conectar(l?.latitude, l?.longitude)
                        }
                    }
                }
            },
            alNegar = { ubicacionNecesaria(getString(R.string.ubicacion_necesaria)) }
        )
    }

    override fun mostrarPanel(panel: dataPanelConductor) {
        findViewById<TextView>(R.id.tv_viajes_hoy).text = panel.hoy.viajes.toString()
        findViewById<TextView>(R.id.tv_total_hoy).text = clsFormato.moneda(panel.hoy.total)
        findViewById<TextView>(R.id.tv_calificacion).text = clsFormato.calificacion(panel.calificacion.promedio)
    }

    override fun mostrarDisponibilidad(disponible: Boolean) {
        findViewById<View>(R.id.punto_estado).setBackgroundResource(if (disponible) R.drawable.bg_circulo_exito else R.drawable.bg_circulo_gris)
        findViewById<TextView>(R.id.tv_estado).setText(if (disponible) R.string.disponible else R.string.desconectado)
        findViewById<TextView>(R.id.tv_titulo).setText(if (disponible) R.string.estas_disponible else R.string.estas_desconectado)
        findViewById<TextView>(R.id.tv_mensaje).setText(if (disponible) R.string.esperando_solicitudes else R.string.activa_disponibilidad)
        findViewById<View>(R.id.pb_esperando).visibility = if (disponible) View.VISIBLE else View.GONE
        btnDisponibilidad.setText(if (disponible) R.string.desconectarme else R.string.conectarme)
        // Amarillo solo para la acción principal (conectarse); desconectarse es secundario (gris)
        btnDisponibilidad.backgroundTintList = if (disponible) {
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.relleno_2))
        } else {
            ContextCompat.getColorStateList(this, R.color.fondo_boton_principal)
        }
    }

    override fun mostrarAvisoConductor(aviso: String) {
        mostrarHoja(getString(R.string.aviso_central_titulo), aviso, getString(R.string.entendido), icono = R.drawable.ic_info)
    }

    override fun mostrarOferta(oferta: dataOferta) {
        hojaOferta.mostrar(oferta)
        mapa.ponerOrigen(oferta.origen.lat, oferta.origen.lng)
        ultima?.let { yo ->
            findViewById<View>(R.id.hoja).post {
                mapa.encuadrar(listOf(yo.latitude to yo.longitude, oferta.origen.lat to oferta.origen.lng), findViewById<View>(R.id.hoja).height)
            }
        }
    }

    override fun ofertaTerminada(mensaje: String?) {
        hojaOferta.cerrar()
        mapa.ponerOrigen(null, null)
        if (mensaje != null) {
            mostrarHoja(getString(R.string.solicitud_terminada), mensaje, getString(R.string.entendido), icono = R.drawable.ic_reloj)
        }
    }

    override fun documentosVencidos(mensaje: String) {
        mostrarHoja(
            getString(R.string.documentos_vencidos_titulo), mensaje, getString(R.string.ir_a_mis_documentos),
            icono = R.drawable.ic_documento, tipo = TipoAviso.ERROR, textoSecundario = getString(R.string.ahora_no),
            alPrincipal = { clsNavegacion.abrir(this, clsNavegacion.MIS_DOCUMENTOS) } // Módulo 2
        )
    }

    override fun ubicacionNecesaria(mensaje: String) {
        mostrarHoja(
            getString(R.string.ubicacion_necesaria_titulo), mensaje, getString(R.string.abrir_ajustes),
            icono = R.drawable.ic_sin_ubicacion, textoSecundario = getString(R.string.ahora_no),
            alPrincipal = { if (clsUbicacion.tienePermiso(this)) abrirAjustesDeUbicacion() else abrirAjustesDeLaApp() }
        )
    }

    override fun mostrarCargando(mostrar: Boolean) {
        super.mostrarCargando(mostrar)
        btnDisponibilidad.isEnabled = !mostrar
        hojaOferta.bloquear(mostrar)
    }

    override fun irAViaje(idViaje: Int) {
        hojaOferta.cerrar()
        irA(Destino.ViajeConductor(idViaje))
    }

    override fun onDestroy() {
        hojaOferta.cerrar()
        presenter.destruir()
        super.onDestroy()
    }
}
