package com.uteq.appmapas

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapa: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.uiSettings.isZoomControlsEnabled = true

        val ubicacion = LatLng(-1.0126167, -79.4672262)
        /*
        val camPos = CameraPosition.Builder()
            .target(ubicacion)
            .zoom(18.5f)
            .bearing(45f)
            .tilt(65f)
            .build()
        val camUpd = CameraUpdateFactory.newCameraPosition(camPos)
        googleMap.animateCamera(camUpd)
        */
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 17f))

        val lineas = PolylineOptions()
            .add(LatLng(-1.013466, -79.467421))
            .add(LatLng(-1.012305, -79.467356))
            .add(LatLng(-1.011929, -79.471851))
            .add(LatLng(-1.013203, -79.471885))
            .add(LatLng(-1.013466, -79.467421))
            .color(Color.RED)

        googleMap.addPolyline(lineas)
        googleMap.addMarker(MarkerOptions()
            .position(LatLng(-1.0125223047554048, -79.46993149612423))
            .title("Marcador en Ecuador"))
    }
}
