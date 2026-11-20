package mx.com.bandline.apptaxihgo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataPerfil
import mx.com.bandline.apptaxihgo.presenter.CuentaPresenter

class ActivityCuenta : AppCompatActivity() {

    private lateinit var presenter: CuentaPresenter
    private lateinit var tvIniciales: TextView
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvRol: TextView
    private lateinit var tvCalificacion: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var btnDocumentos: MaterialButton
    private lateinit var btnCerrarSesion: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuenta)

        presenter = CuentaPresenter(this)

        tvIniciales = findViewById(R.id.tv_iniciales)
        tvNombreCompleto = findViewById(R.id.tv_nombre_completo)
        tvRol = findViewById(R.id.tv_rol)
        tvCalificacion = findViewById(R.id.tv_calificacion)
        tvCorreo = findViewById(R.id.tv_correo)
        tvTelefono = findViewById(R.id.tv_telefono)
        btnDocumentos = findViewById(R.id.btn_mis_documentos)
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion)

        // Leer el token guardado al iniciar sesión y pedir los datos a obtener_perfil.php
        val prefs = getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        presenter.cargarPerfil(token)

        btnCerrarSesion.setOnClickListener {
            prefs.edit().clear().apply()
            sesionCerrada()
        }
    }

    fun mostrarPerfil(perfil: dataPerfil) {
        tvNombreCompleto.text = "${perfil.nombre} ${perfil.apellidoPaterno}".trim()
        tvCorreo.text = perfil.correo
        tvTelefono.text = perfil.telefono
        tvCalificacion.text = "★ ${perfil.calificacion}"

        val letra1 = perfil.nombre.firstOrNull()?.uppercaseChar() ?: 'U'
        val letra2 = perfil.apellidoPaterno.firstOrNull()?.uppercaseChar() ?: 'S'
        tvIniciales.text = "$letra1$letra2"

        if (perfil.rol == "CONDUCTOR") {
            tvRol.text = "Conductor"
            btnDocumentos.visibility = View.VISIBLE
        } else {
            tvRol.text = "Pasajero"
            btnDocumentos.visibility = View.GONE
        }
    }

    fun sesionCerrada() {
        val intent = Intent(this, ActivityLogin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}