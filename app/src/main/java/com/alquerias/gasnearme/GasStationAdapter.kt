package com.alquerias.gasnearme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GasStationAdapter(
    private val gasStations: List<GasStation>,
    private val selectedFuelType: String
) : RecyclerView.Adapter<GasStationAdapter.GasStationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GasStationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gas_station, parent, false)
        return GasStationViewHolder(view)
    }

    override fun onBindViewHolder(holder: GasStationViewHolder, position: Int) {
        val gasStation = gasStations[position]
        holder.tvName.text = gasStation.name
        val price = when (selectedFuelType) {
            "diesel" -> gasStation.priceDiesel
            "gasolina95" -> gasStation.priceGasolina95
            else -> "N/A"
        }
        holder.tvPrice.text = "Precio: $price"
    }

    override fun getItemCount() = gasStations.size

    class GasStationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPriceGasolina95)
    }
}
