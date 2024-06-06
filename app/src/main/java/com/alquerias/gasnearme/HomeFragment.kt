package com.alquerias.gasnearme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.abs
import android.content.Context

class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedClient: FusedLocationProviderClient
    private val REQUEST_CODE = 101
    private lateinit var placesApiService: PlacesApiService
    private lateinit var gasPricesApiService: GasPricesApiService
    private var selectedFuelType: String = "gasolina95"
    private var gasStations: MutableList<GasStation> = mutableListOf()
    private var tankCapacity: Int = 0
    private var searchRadius: Int = 5000

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocation()

        val placesRetrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        placesApiService = placesRetrofit.create(PlacesApiService::class.java)

        val gasPricesRetrofit = Retrofit.Builder()
            .baseUrl("https://sedeaplicaciones.minetur.gob.es/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        gasPricesApiService = gasPricesRetrofit.create(GasPricesApiService::class.java)

        val sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        tankCapacity = sharedPreferences.getInt("tank_capacity", 0)
        Log.d("HomeFragment", "Tank Capacity: $tankCapacity")
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return
        }
        val task = fusedClient.lastLocation

        task.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                showNearbyGasStations(latLng)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMarkerClickListener { marker ->
            marker.tag?.let {
                val gasStation = it as GasStation
                val gasolineCost = calculateCost(gasStation.priceGasolina95)
                val dieselCost = calculateCost(gasStation.priceDiesel)
                Log.d("HomeFragment", "Gasoline Cost: $gasolineCost, Diesel Cost: $dieselCost")
                val fragment = DataFragment.newInstance(
                    gasStation.name,
                    gasStation.address,
                    gasStation.priceGasolina95,
                    gasStation.priceDiesel,
                    gasolineCost,
                    dieselCost
                )
                childFragmentManager.beginTransaction()
                    .replace(R.id.details_container, fragment)
                    .commit()

                // Make the details container visible
                view?.findViewById<View>(R.id.details_container)?.visibility = View.VISIBLE
            }
            true
        }
    }

    private fun showNearbyGasStations(location: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val placesResponse = placesApiService.getNearbyPlaces(
                    "${location.latitude},${location.longitude}",
                    searchRadius,
                    "gas_station",
                    getString(R.string.google_maps_key)
                )

                val gasStationsResponse = gasPricesApiService.getGasStations("14")

                // Log the data fetched from APIs
                Log.d("HomeFragment", "Places Response: ${placesResponse.results}")
                Log.d(
                    "HomeFragment",
                    "Gas Stations Response: ${gasStationsResponse.ListaEESSPrecio}"
                )

                withContext(Dispatchers.Main) {
                    for (place in placesResponse.results) {
                        val placeLatLng =
                            LatLng(place.geometry.location.lat, place.geometry.location.lng)

                        val matchedStation =
                            gasStationsResponse.ListaEESSPrecio.find { gasStation ->
                                val gasStationLat = gasStation.latitude.replace(",", ".").toDouble()
                                val gasStationLng =
                                    gasStation.longitude.replace(",", ".").toDouble()

                                val latDiff = abs(gasStationLat - placeLatLng.latitude)
                                val lngDiff = abs(gasStationLng - placeLatLng.longitude)
                                val tolerance =
                                    0.001

                                latDiff < tolerance && lngDiff < tolerance
                            }

                        val price = matchedStation?.let {
                            when (selectedFuelType) {
                                "Diesel" -> it.priceDiesel
                                "Gasolina 95" -> it.priceGasolina95
                                else -> "N/A"
                            }
                        } ?: "N/A"

                        val markerOptions = MarkerOptions()
                            .position(placeLatLng)
                            .title(place.name)
                            .snippet("Precio: $price\nDireccion: ${place.vicinity}")

                        val marker = googleMap.addMarker(markerOptions)
                        if (matchedStation != null) {
                            marker?.tag = matchedStation
                            val gasStation = GasStation(
                                latitude = matchedStation.latitude.replace(",", "."),
                                longitude = matchedStation.longitude.replace(",", "."),
                                address = matchedStation.address,
                                priceGasolina95 = matchedStation.priceGasolina95,
                                priceDiesel = matchedStation.priceDiesel,
                                name = place.name
                            )
                            gasStations.add(gasStation)
                        } else {
                            marker?.tag = GasStation(
                                latitude = place.geometry.location.lat.toString(),
                                longitude = place.geometry.location.lng.toString(),
                                address = place.vicinity,
                                priceGasolina95 = "N/A",
                                priceDiesel = "N/A",
                                name = place.name
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeFragment", "Error fetching data", e)
            }
        }
    }
    private fun calculateCost(price: String?): String {
        val priceValue = price?.replace(",", ".")?.toDoubleOrNull()
        Log.d("HomeFragment", "Calculating cost. Price: $priceValue, Tank Capacity: $tankCapacity")
        return if (priceValue != null && tankCapacity > 0) {
            val cost = priceValue * tankCapacity
            "%.2f".format(cost)
        } else {
            "N/A"
        }
    }

    fun updateFuelType(fuelType: String) {
        Log.d("HomeFragment", "Fuel type updated to: $fuelType")
        selectedFuelType = fuelType
        getLocation()
    }
    fun updateSearchRadius(radius: Int) {
        Log.d("HomeFragment", "Search radius updated to: $radius")
        searchRadius = radius
        getLocation()
    }

    fun getGasStations(): List<GasStation> = gasStations
}