package com.alquerias.gasnearme

data class PlaceResult(
    val name: String,
    val geometry: Geometry,
    val vicinity: String
)
