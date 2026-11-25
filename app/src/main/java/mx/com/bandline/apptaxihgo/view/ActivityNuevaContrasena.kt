package mx.com.bandline.apptaxihgo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.presenter.NuevaContrasenaPresenter

class ActivityNuevaContrasena : AppCompatActivity() {
    private lateinit var presenter: NuevaContrasenaPresenter
    private lateinit var tilCodigo: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var tilConfirmar: TextInputLayout
    private lateinit var btnCambiar: MaterialButton
    private lateinit var btnRegresar: MaterialButton
    private var correoUsuario = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_contrasena)

        correoUsuario = intent.getStringExtra("CORREO") ?: ""
        presenter = NuevaContrasenaPresenter(this)

        tilCodigo = findViewById(R.id.til_codigo)
        tilContrasena = findViewById(R.id.til_contrasena)
        tilConfirmar = findViewById(R.id.til_confirmar_contrasena)
        btnCambiar = findViewById(R.id.btn_cambiar_contrasena)
        btnRegresar = findViewById(R.id.btn_regresar)

        btnRegresar.setOnClickListener { finish() }

        btnCambiar.setOnClickListener {
            val codigo = tilCodigo.editText?.text.toString().trim()
            val contrasena = tilContrasena.editText?.text.toString()
            val confirmar = tilConfirmar.editText?.text.toString()
            presenter.cambiarContrasena(correoUsuario, codigo, contrasena, confirmar)
        }
    }

    fun mostrarCargando(cargando: Boolean) {
        btnCambiar.isEnabled = !cargando
        btnCambiar.text = if (cargando) "Verificando..." else "Cambiar contraseña"
    }

    fun contrasenaCambiada(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        val intent = Intent(this, ActivityLogin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}