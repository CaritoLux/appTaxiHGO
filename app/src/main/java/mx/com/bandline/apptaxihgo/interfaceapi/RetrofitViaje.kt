package mx.com.bandline.apptaxihgo.interfaceapi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitViaje {
    val api: ApiViaje by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitCliente.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiViaje::class.java)
    }
}