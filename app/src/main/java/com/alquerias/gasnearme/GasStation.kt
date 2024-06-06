package com.alquerias.gasnearme

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GasStation(
    @SerializedName("Latitud") val latitude: String,
    @SerializedName("Longitud (WGS84)") val longitude: String,
    @SerializedName("Dirección") val address: String,
    @SerializedName("Precio Gasolina 95 E5") val priceGasolina95: String?,
    @SerializedName("Precio Gasoleo A") val priceDiesel: String?,
    @SerializedName("Rótulo") val name: String
) : Serializable
