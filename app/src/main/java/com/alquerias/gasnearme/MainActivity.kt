package com.alquerias.gasnearme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import android.widget.EditText

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var placesApiService: PlacesApiService
    private var searchRadius: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        placesApiService = retrofit.create(PlacesApiService::class.java)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()

            R.id.fuel_type -> {
                showFuelTypePopupMenu()
                return true
            }
            R.id.nav_gasList -> {
                showGasStationList()
                return true
            }
            R.id.nav_tankCap -> {
                showTankCapacityDialog()
                return true
            }
            R.id.map_search -> {
                showRadiusInputDialog()
                return true
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showFuelTypePopupMenu() {
        val popupMenu = PopupMenu(this, findViewById(R.id.fuel_type))
        popupMenu.menuInflater.inflate(R.menu.fuel_type_popup, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleFuelTypeSelection(menuItem)
            true
        }
        popupMenu.show()
    }
    private fun showRadiusInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Search Radius")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_set_radius, null)
        val editTextRadius = view.findViewById<EditText>(R.id.editTextRadius)
        editTextRadius.setText(searchRadius.toString())

        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, _ ->
            val radiusInput = editTextRadius.text.toString()
            searchRadius = radiusInput.toIntOrNull() ?: 5000
            updateSearchRadius(searchRadius)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun updateSearchRadius(radius: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is HomeFragment) {
            fragment.updateSearchRadius(radius)
        }
    }

    private fun showGasStationList() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is HomeFragment) {
            val gasStations = fragment.getGasStations()
            val intent = Intent(this, GasStationList::class.java)

            val bundle = Bundle()
            bundle.putSerializable("gasStations", ArrayList(gasStations))
            intent.putExtras(bundle)

            startActivity(intent)
        }
    }


    private fun handleFuelTypeSelection(menuItem: MenuItem) {
        val fuelType = when (menuItem.itemId) {
            R.id.fuel_gasoline_98 -> "Gasolina 95"
            R.id.fuel_gasoline_95 -> "Gasolina 98"
            R.id.fuel_diesel -> "Diesel"
            else -> "Gasolina 95"
        }

        // Pass the selected fuel type to the fragment to update the map
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is HomeFragment) {
            fragment.updateFuelType(fuelType)
        } else {
            Log.e("MainActivity", "HomeFragment not found")
        }
    }
    private fun showTankCapacityDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.fuel_capacity, null)
        val etTankCapacity = dialogView.findViewById<EditText>(R.id.etTankCapacity)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val savedCapacity = sharedPreferences.getInt("tank_capacity", 0)
        etTankCapacity.setText(savedCapacity.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val newCapacity = etTankCapacity.text.toString().toIntOrNull()
            if (newCapacity != null) {
                sharedPreferences.edit().putInt("tank_capacity", newCapacity).apply()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
