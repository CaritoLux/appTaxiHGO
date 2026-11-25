package mx.com.bandline.apptaxihgo.model

import com.google.gson.annotations.SerializedName

// Lo que responde Photon (buscador gratuito de OpenStreetMap): una lista de lugares en formato GeoJSON
data class dataPhotonRespuesta(
    @SerializedName("features") val features: List<dataPhotonLugar> = emptyList()
)

data class dataPhotonLugar(
    @SerializedName("geometry") val geometry: dataPhotonGeometria = dataPhotonGeometria(),
    @SerializedName("properties") val properties: dataPhotonPropiedades = dataPhotonPropiedades()
)

// coordinates = [longitud, latitud]
data class dataPhotonGeometria(
    @SerializedName("coordinates") val coordinates: List<Double> = emptyList()
)

data class dataPhotonPropiedades(
    @SerializedName("name") val name: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("housenumber") val housenumber: String? = null,
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("county") val county: String? = null,
    @SerializedName("countrycode") val countrycode: String? = null
)
