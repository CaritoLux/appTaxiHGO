package mx.com.bandline.apptaxihgo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.presenter.RecuperarPresenter

class ActivityRecuperar : AppCompatActivity() {
    private lateinit var presenter: RecuperarPresenter
    private lateinit var tilCorreo: TextInputLayout
    private lateinit var btnEnviar: MaterialButton
    private lateinit var btnRegresar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar)

        presenter = RecuperarPresenter(this)
        tilCorreo = findViewById(R.id.til_correo)
        btnEnviar = findViewById(R.id.btn_enviar_codigo)
        btnRegresar = findViewById(R.id.btn_regresar)

        btnRegresar.setOnClickListener { finish() }

        btnEnviar.setOnClickListener {
            val correo = tilCorreo.editText?.text.toString()
            presenter.solicitarCodigo(correo)
        }
    }

    fun mostrarCargando(cargando: Boolean) {
        btnEnviar.isEnabled = !cargando
        btnEnviar.text = if (cargando) "Enviando..." else "Enviar código"
    }

    fun codigoEnviado(correo: String) {
        Toast.makeText(this, "Código enviado al correo", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ActivityNuevaContrasena::class.java)
        intent.putExtra("CORREO", correo)
        startActivity(intent)
        finish()
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}