package mx.com.bandline.apptaxihgo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataSesionIniciada
import mx.com.bandline.apptaxihgo.presenter.LoginPresenter

class ActivityLogin : AppCompatActivity() {

    private lateinit var presenter: LoginPresenter
    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var btnIniciar: MaterialButton
    private lateinit var btnOlvidaste: MaterialButton
    private lateinit var btnCrearCuenta: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Revisar si ya hay una sesión guardada en el teléfono
        val prefs = getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        val tokenGuardado = prefs.getString("token", "")
        if (!tokenGuardado.isNullOrEmpty()) {
            val intent = Intent(this, ActivityInicioPasajero::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this)

        tilCorreo = findViewById(R.id.til_correo)
        tilContrasena = findViewById(R.id.til_contrasena)
        btnIniciar = findViewById(R.id.btn_iniciar_sesion)
        btnOlvidaste = findViewById(R.id.btn_olvidaste_contrasena)
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta)

        // Si viene recién registrado, rellenar el correo
        val correoNuevo = intent.getStringExtra("CORREO")
        if (correoNuevo != null) {
            tilCorreo.editText?.setText(correoNuevo)
        }

        btnIniciar.setOnClickListener {
            val correo = tilCorreo.editText?.text.toString()
            val contrasena = tilContrasena.editText?.text.toString()
            presenter.iniciarSesion(correo, contrasena)
        }

        btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, ActivityRegistro::class.java))
        }

        btnOlvidaste.setOnClickListener {
            startActivity(Intent(this, ActivityRecuperar::class.java))
        }
    }

    fun mostrarCargando(cargando: Boolean) {
        btnIniciar.isEnabled = !cargando
        btnIniciar.text = if (cargando) "Iniciando..." else "Iniciar sesión"
    }

    fun loginExitoso(datos: dataSesionIniciada) {
        //guarda el token
        val prefs = getSharedPreferences("sesion_taxihgo", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("token", datos.token)
            .putInt("id_usuario", datos.usuario.idUsuario)
            .putString("rol", datos.usuario.rol)
            .apply()

        Toast.makeText(this, "Bienvenido ${datos.usuario.nombre}", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, ActivityInicioPasajero::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    fun limpiarErrores() {
        tilCorreo.error = null
        tilContrasena.error = null
    }

    fun mostrarErrorCorreo(mensaje: String) {
        tilCorreo.error = mensaje
    }

    fun mostrarErrorContrasena(mensaje: String) {
        tilContrasena.error = mensaje
    }

    fun mostrarErrorServidor(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}