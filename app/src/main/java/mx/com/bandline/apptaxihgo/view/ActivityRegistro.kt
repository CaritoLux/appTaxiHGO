package mx.com.bandline.apptaxihgo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataRegistro
import mx.com.bandline.apptaxihgo.presenter.RegistroPresenter

class ActivityRegistro : AppCompatActivity() {

    private lateinit var presenter: RegistroPresenter
    private lateinit var cardPasajero: MaterialCardView
    private lateinit var cardConductor: MaterialCardView
    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilPaterno: TextInputLayout
    private lateinit var tilMaterno: TextInputLayout
    private lateinit var tilTelefono: TextInputLayout
    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var tilConfirmar: TextInputLayout
    private lateinit var cbAviso: MaterialCheckBox
    private lateinit var btnRegistrarme: MaterialButton
    private lateinit var btnRegresar: MaterialButton

    private var tipoCuenta: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        presenter = RegistroPresenter(this)

        cardPasajero = findViewById(R.id.card_pasajero)
        cardConductor = findViewById(R.id.card_conductor)
        tilNombre = findViewById(R.id.til_nombre)
        tilPaterno = findViewById(R.id.til_apellido_paterno)
        tilMaterno = findViewById(R.id.til_apellido_materno)
        tilTelefono = findViewById(R.id.til_telefono)
        tilCorreo = findViewById(R.id.til_correo)
        tilContrasena = findViewById(R.id.til_contrasena)
        tilConfirmar = findViewById(R.id.til_confirmar_contrasena)
        cbAviso = findViewById(R.id.cb_aviso_privacidad)
        btnRegistrarme = findViewById(R.id.btn_registrarme)
        btnRegresar = findViewById(R.id.btn_regresar)

        cardPasajero.setOnClickListener {
            tipoCuenta = "PASAJERO"
            cardPasajero.isChecked = true
            cardConductor.isChecked = false
        }

        cardConductor.setOnClickListener {
            tipoCuenta = "CONDUCTOR"
            cardConductor.isChecked = true
            cardPasajero.isChecked = false
        }

        btnRegresar.setOnClickListener {
            finish()
        }

        btnRegistrarme.setOnClickListener {
            val peticion = dataRegistro(
                tipoCuenta = tipoCuenta,
                nombre = tilNombre.editText?.text.toString().trim(),
                apellidoPaterno = tilPaterno.editText?.text.toString().trim(),
                apellidoMaterno = tilMaterno.editText?.text.toString().trim(),
                telefono = tilTelefono.editText?.text.toString().trim(),
                correo = tilCorreo.editText?.text.toString().trim(),
                contrasena = tilContrasena.editText?.text.toString(),
                confirmarContrasena = tilConfirmar.editText?.text.toString(),
                aceptaAviso = cbAviso.isChecked
            )
            presenter.registrarUsuario(peticion)
        }
    }

    fun mostrarCargando(cargando: Boolean) {
        btnRegistrarme.isEnabled = !cargando
        btnRegistrarme.text = if (cargando) "Registrando..." else "Registrarme"
    }

    fun registroExitoso(mensaje: String, correo: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        val intent = Intent(this, ActivityLogin::class.java)
        intent.putExtra("CORREO", correo)
        startActivity(intent)
        finish()
    }

    fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}