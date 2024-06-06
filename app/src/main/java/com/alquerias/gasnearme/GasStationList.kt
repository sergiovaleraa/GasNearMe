package com.alquerias.gasnearme

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GasStationList : AppCompatActivity() {

    private lateinit var adapter: GasStationAdapter
    private lateinit var gasStations: List<GasStation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_gas_station_list)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        gasStations = intent.extras?.getSerializable("gasStations") as? List<GasStation> ?: emptyList()

        val btnGasoline: Button = findViewById(R.id.btnGasoline)
        val btnDiesel: Button = findViewById(R.id.btnDiesel)

        btnGasoline.setOnClickListener {
            updateList("gasolina95")
        }

        btnDiesel.setOnClickListener {
            updateList("diesel")
        }

        updateList("gasolina95")
    }

    private fun updateList(fuelType: String) {
        val sortedGasStations = when (fuelType) {
            "diesel" -> gasStations.sortedBy { it.priceDiesel?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE }
            else -> gasStations.sortedBy { it.priceGasolina95?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE }
        }

        adapter = GasStationAdapter(sortedGasStations, fuelType)
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }
}
