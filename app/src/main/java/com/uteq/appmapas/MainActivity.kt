package com.uteq.appmapas

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val IP_BASE = "URL"
        private const val INITIAL_LAT = -1.012389
        private const val INITIAL_LNG = -79.469222
        private const val INITIAL_RADIO = 500.0
    }

    private var mapa: GoogleMap? = null
    private var lat: Double = INITIAL_LAT
    private var lng: Double = INITIAL_LNG
    private var radio: Double = INITIAL_RADIO
    
    private var circulo: Circle? = null
    private val markers = mutableListOf<Marker>()
    
    private lateinit var txtLat: EditText
    private lateinit var txtLong: EditText
    private lateinit var sliderRadio: Slider
    private lateinit var spnCategoria: Spinner
    private lateinit var spnSubcategoria: Spinner
    private lateinit var requestQueue: RequestQueue
    private lateinit var fabMapType: com.google.android.material.floatingactionbutton.FloatingActionButton

    private var lastLatSearch = 0.0
    private var lastLngSearch = 0.0
    private var lastRadioSearch = 0.0
    private var lastSubCatSearch = ""
    private val subCatIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupWindowInsets()
        initViews()
        setupListeners()
        
        requestQueue = Volley.newRequestQueue(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        fetchCategorias()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        txtLat = findViewById(R.id.txtLat)
        txtLong = findViewById(R.id.txtLong)
        sliderRadio = findViewById(R.id.sliderRadio)
        spnCategoria = findViewById(R.id.spnCategoria)
        spnSubcategoria = findViewById(R.id.spnSubcategoria)
        fabMapType = findViewById(R.id.fabMapType)
    }

    private fun setupListeners() {
        sliderRadio.addOnChangeListener { _, value, _ ->
            radio = value.toDouble()
            updateInterfaz()
        }
        
        spnSubcategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateInterfaz()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        fabMapType.setOnClickListener {
            mapa?.let { m ->
                m.mapType = if (m.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    GoogleMap.MAP_TYPE_SATELLITE
                } else {
                    GoogleMap.MAP_TYPE_NORMAL
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
            
            setOnCameraIdleListener {
                cameraPosition.target.let { center ->
                    lat = center.latitude
                    lng = center.longitude
                    updateInterfaz()
                }
            }
            
            setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                true
            }
        }
        updateInterfaz()
    }

    private fun fetchCategorias() {
        val url = "$IP_BASE/categoria/getlistadoCB"
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            val listNames = mutableListOf("TODOS")
            val ids = mutableListOf("")
            for (i in 0 until response.length()) {
                val obj = response.getJSONObject(i)
                listNames.add(obj.getString("descripcion"))
                ids.add(obj.getString("id"))
            }
            setupSpinner(spnCategoria, listNames) { position ->
                if (position == 0) { // Opción "TODOS"
                    subCatIds.clear()
                    setupSpinner(spnSubcategoria, listOf("TODOS"))
                    updateInterfaz()
                } else {
                    fetchSubcategorias(ids[position])
                }
            }
        }, { error -> Log.e("MainActivity", "Error Categorias: ${error.message}") })
        requestQueue.add(request)
    }

    private fun fetchSubcategorias(idCat: String) {
        val url = "$IP_BASE/subcategoria/getlistadoCB/$idCat"
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            val listNames = mutableListOf("TODOS")
            subCatIds.clear()
            subCatIds.add("")
            for (i in 0 until response.length()) {
                val obj = response.getJSONObject(i)
                listNames.add(obj.getString("descripcion"))
                subCatIds.add(obj.getString("id"))
            }
            setupSpinner(spnSubcategoria, listNames)
        }, { error -> Log.e("MainActivity", "Error Subcategorias: ${error.message}") })
        requestQueue.add(request)
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, onSelected: ((Int) -> Unit)? = null) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter
        onSelected?.let { callback ->
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    callback(p2)
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun updateInterfaz() {
        val googleMap = mapa ?: return
        val idSubCat = if (subCatIds.isNotEmpty() && spnSubcategoria.selectedItemPosition >= 0) {
            subCatIds[spnSubcategoria.selectedItemPosition]
        } else ""

        val moveDist = Math.abs(lat - lastLatSearch) + Math.abs(lng - lastLngSearch)
        if (moveDist < 0.0001 && radio == lastRadioSearch && idSubCat == lastSubCatSearch) return

        lastLatSearch = lat
        lastLngSearch = lng
        lastRadioSearch = radio
        lastSubCatSearch = idSubCat

        txtLat.setText(String.format(Locale.ROOT, "%.6f", lat))
        txtLong.setText(String.format(Locale.ROOT, "%.6f", lng))

        drawSearchArea(googleMap)
        fetchLugares(idSubCat)
    }

    private fun drawSearchArea(googleMap: GoogleMap) {
        val center = LatLng(lat, lng)
        circulo?.remove()
        circulo = googleMap.addCircle(CircleOptions()
            .center(center)
            .radius(radio)
            .strokeColor(Color.RED)
            .fillColor(Color.argb(70, 150, 50, 50)))
    }

    private fun fetchLugares(idSubCat: String) {
        var url = "$IP_BASE/lugar_turistico/json_getlistadoMapa?lat=$lat&lng=$lng&radio=${radio / 1000.0}"
        if (idSubCat.isNotEmpty()) url += "&idsubcategoria=$idSubCat"

        val request = StringRequest(Request.Method.GET, url, { response ->
            processFinish(response, idSubCat)
        }, { error -> Log.e("MainActivity", "Volley Error: ${error.message}") })
        requestQueue.add(request)
    }

    private fun processFinish(result: String, selectedSubId: String) {
        try {
            markers.forEach { it.remove() }
            markers.clear()

            val dataArray = JSONObject(result).getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val lugar = dataArray.getJSONObject(i)
                val lugarSubId = getSubCategoryId(lugar)

                if (selectedSubId.isNotEmpty() && lugarSubId.isNotEmpty() && lugarSubId != selectedSubId) continue

                val pos = LatLng(lugar.getDouble("lat"), lugar.getDouble("lng"))
                val direccion = getLugarDireccion(lugar)

                mapa?.addMarker(MarkerOptions()
                    .position(pos)
                    .title(lugar.getString("nombre"))
                    .snippet(direccion))?.let { markers.add(it) }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error JSON: ${e.message}")
        }
    }

    private fun getSubCategoryId(lugar: JSONObject): String {
        return when {
            lugar.has("idsubcategoria") -> lugar.getString("idsubcategoria")
            lugar.has("id_subcategoria") -> lugar.getString("id_subcategoria")
            lugar.has("subcategoria_id") -> lugar.getString("subcategoria_id")
            else -> ""
        }
    }

    private fun getLugarDireccion(lugar: JSONObject): String {
        return when {
            lugar.optString("direccion").isNotBlank() -> lugar.getString("direccion")
            lugar.optString("ubicacion").isNotBlank() -> lugar.getString("ubicacion")
            lugar.optString("referencia").isNotBlank() -> lugar.getString("referencia")
            else -> ""
        }
    }
}
