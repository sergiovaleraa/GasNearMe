package com.alquerias.gasnearme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DataFragment : Fragment(R.layout.fragment_data) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name")
        val address = arguments?.getString("address")
        val priceGasoline95 = arguments?.getString("priceGasoline95")
        val priceDiesel = arguments?.getString("priceDiesel")
        val cost = arguments?.getString("cost")
        val costDiesel = arguments?.getString("costDiesel")

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvPriceGasoline95: TextView = view.findViewById(R.id.tvPriceGasoline95)
        val tvPriceDiesel: TextView = view.findViewById(R.id.tvPriceDiesel)
        val tvCost: TextView = view.findViewById(R.id.tvLabelGasolineCost)
        val tvDieselCost: TextView = view.findViewById(R.id.tvLabelDieselCost)

        tvName.text = name
        tvAddress.text = address
        tvPriceGasoline95.text = "Gasolina 95: $priceGasoline95"
        tvPriceDiesel.text = "Diesel: $priceDiesel"
        tvCost.text = "Precio del tanque lleno: $cost"
        tvDieselCost.text = "Precio del tanque lleno: $costDiesel"
    }

    companion object {
        fun newInstance(
            name: String,
            address: String,
            priceGasoline95: String?,
            priceDiesel: String?,
            cost: String?,
            costDiesel: String?
        ): DataFragment {
            val fragment = DataFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putString("address", address)
            args.putString("priceGasoline95", priceGasoline95)
            args.putString("priceDiesel", priceDiesel)
            args.putString("cost", cost)
            args.putString("costDiesel", costDiesel)
            fragment.arguments = args
            return fragment
        }
    }
}