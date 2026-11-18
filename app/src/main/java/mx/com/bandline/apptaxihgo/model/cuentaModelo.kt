package mx.com.bandline.apptaxihgo.model

import kotlin.jvm.java


class cuentaModelo(
    private val apiService: ApiService = RetrofitCliente.retrofit.create(ApiService::class.java),
    private val clsLlamadaApi: clsLlamadaApi = clsLlamadaApi()
)
