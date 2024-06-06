package com.alquerias.gasnearme

import retrofit2.http.GET
import retrofit2.http.Path

interface GasPricesApiService {
    @GET("ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/FiltroCCAA/{ccaa}")
    suspend fun getGasStations(
        @Path("ccaa") ccaa: String
    ): GasStationsResponse
}
