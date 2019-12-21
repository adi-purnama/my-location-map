package com.adipurnama.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.adipurnama.R
import kotlinx.android.synthetic.main.mylocation_map_activity.*
import java.io.IOException
import java.util.*


@Suppress("DEPRECATION")
class MylocationMap : AppCompatActivity() ,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveCanceledListener,
    OnMapReadyCallback,
    GoogleMap.OnMyLocationChangeListener{

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mGoogleMap: GoogleMap
    private var myCoordinate: LatLng?=null
    private var isCameraMove=false
    private var isEditCoordinate=false
    private var xtitle=""
    private var xlat: Double? = 0.0
    private var xlong: Double? = 0.0
    private var xfixedlat:Double=0.0
    private var xfixedlong:Double=0.0
    private lateinit var lastLocation: Location
    private val ijinLokasiCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mylocation_map_activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        xtitle=intent.getStringExtra("title")?:""
        xlat = intent.getDoubleExtra("xlat", 0.toDouble())
        xlong = intent.getDoubleExtra("xlong", 0.toDouble())
        xfixedlat = intent.getDoubleExtra("xfixedlat", 0.toDouble())
        xfixedlong = intent.getDoubleExtra("xfixedlong", 0.toDouble())
        MapsInitializer.initialize(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        savemap.setOnClickListener { saveMap() }
        exit.setOnClickListener { finish() }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.setOnCameraMoveListener(this)
        mGoogleMap.setOnCameraIdleListener(this)
        mGoogleMap.setOnMyLocationChangeListener(this)
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), ijinLokasiCode)
            return
        }
        mGoogleMap.isMyLocationEnabled = true
        mGoogleMap.uiSettings.isZoomControlsEnabled=true
        mGoogleMap.addMarker(MarkerOptions()
            .position(LatLng(xfixedlat,xfixedlong))
            .title(xtitle))
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = if (xlat==0.toDouble() && xlong==0.toDouble()){
                    LatLng(location.latitude, location.longitude)
                }else{
                    LatLng(xlat!!.toDouble(), xlong!!.toDouble())
                }
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                xlat = location.latitude
                xlong = location.longitude
            }
        }
    }

    override fun onCameraMove() {
        if (isEditCoordinate) {
            isEditCoordinate = false
            return
        }

        isCameraMove = true
        val coordinate = mGoogleMap.cameraPosition.target
        setCoordinateInput(coordinate)
    }

    override fun onCameraIdle() {
        isCameraMove = false
    }

    override fun onCameraMoveCanceled() {
        isCameraMove = false
    }

    private fun setCoordinateInput(coordinate: LatLng){
        xlat=coordinate.latitude
        xlong=coordinate.longitude
    }

    override fun onMyLocationChange(location: Location){
        if (myCoordinate != null) return
        myCoordinate = LatLng(location.latitude, location.longitude)
        setMapCoordinate(myCoordinate!!)
    }
    private fun setMapCoordinate(coordinateParam: LatLng) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinateParam))
        setCoordinateInput(coordinateParam)
    }

    /*use for save address name and location*/
    private fun saveMap(){
        if (xlat!=null && xlong!=null) {
            val geoCoder = Geocoder(this, Locale.getDefault())
            try {
                /*get location address*/
                var xaddress=""
                val address = geoCoder.getFromLocation(xlat!!.toDouble(), xlong!!.toDouble(), 1)
                if (address.size > 0) {
                    xaddress=address[0].getAddressLine(0)
                }
                val intent = Intent()
                intent.putExtra("xlatitude", xlat!!)
                intent.putExtra("xlongitude", xlong!!)
                intent.putExtra("xalamat", xaddress)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } catch (e: IOException) {
                finish()
                // Handle IOException
            } catch (e: NullPointerException) {
                finish()
                // Handle NullPointerException
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mGoogleMap)
            Toast.makeText(
                this, "GPS On, Start update your location",
                Toast.LENGTH_LONG
            ).show()
        }else{
            Toast.makeText(this, "Please to active your GPS",
                Toast.LENGTH_LONG).show()
        }
    }
}