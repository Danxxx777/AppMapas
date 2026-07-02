package com.uteq.appmapas

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.slider.Slider
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Asynchtask {

    private var mapa: GoogleMap? = null
    private var lat: Double = -0.9656154
    private var lng: Double = -79.4684
    private var radio: Double = 1.0
    private var circulo: Circle? = null
    private var markerCentro: Marker? = null
    private val markers = mutableListOf<Marker>()

    private lateinit var txtLat: EditText
    private lateinit var txtLong: EditText
    private lateinit var sliderRadio: Slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtLat = findViewById(R.id.txtLat)
        txtLong = findViewById(R.id.txtLong)
        sliderRadio = findViewById(R.id.sliderRadio)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sliderRadio.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                radio = slider.value.toDouble()
                updateInterfaz()
            }
            override fun onStopTrackingTouch(slider: Slider) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.uiSettings.isZoomControlsEnabled = true

        val ubicacion = LatLng(lat, lng)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))

        mapa?.setOnCameraIdleListener {
            val center = mapa?.cameraPosition?.target
            if (center != null) {
                lat = center.latitude
                lng = center.longitude
                updateInterfaz()
            }
        }

        mapa?.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        updateInterfaz()
    }

    private fun updateInterfaz() {
        txtLat.setText(String.format("%.7f", lat))
        txtLong.setText(String.format("%.4f", lng))

        // Dibujar Círculo
        circulo?.remove()
        val circleOptions = CircleOptions()
            .center(LatLng(lat, lng))
            .radius(radio * 200) // Ajustado para que se vea como en la foto
            .strokeColor(Color.RED)
            .fillColor(Color.argb(70, 150, 50, 50))
        circulo = mapa?.addCircle(circleOptions)

        // Marcador central (el naranja de la foto de tu pana)
        markerCentro?.remove()
        markerCentro = mapa?.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("Tu ubicación")
        )

        // Llamar WebService (Diapo 6)
        val datos = HashMap<String, String>()
        val url = "http://3.231.146.17/turismo10022025/lugar_turistico/json_getlistadoMapa?lat=$lat&lng=$lng&radio=${radio / 10.0}"
        val ws = WebService(url, datos, this, this)
        ws.execute("GET")
    }

    override fun processFinish(result: String) {
        try {
            // Limpiar marcadores turísticos anteriores
            for (marker in markers) marker.remove()
            markers.clear()

            val jsonObj = JSONObject(result)
            val jsonLista = jsonObj.getJSONArray("data")
            
            for (i in 0 until jsonLista.length()) {
                val lugar = jsonLista.getJSONObject(i)
                val marker = mapa?.addMarker(
                    MarkerOptions()
                        .position(LatLng(lugar.getDouble("lat"), lugar.getDouble("lng")))
                        .title(lugar.getString("nombre"))
                )
                if (marker != null) markers.add(marker)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error JSON: ${e.message}")
        }
    }
}
