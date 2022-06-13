package com.example.robosog

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    val FINE_LOCATION_RO = 101
    val ACCESS_WIFI_RO = 110
    lateinit var button_start :Button
    lateinit var button_stop :Button
    lateinit var Ssid : TextView
    lateinit var Password : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.robosog.R.layout.activity_main)

        button_start = findViewById(com.example.robosog.R.id.button)
        button_stop = findViewById(com.example.robosog.R.id.button_close)
        Ssid = findViewById(com.example.robosog.R.id.SSID)
        Password = findViewById(com.example.robosog.R.id.password)


        button_start.setOnClickListener{
            checkForPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, "location",FINE_LOCATION_RO)

            checkForPermissions(android.Manifest.permission.ACCESS_WIFI_STATE, "wifi_state",ACCESS_WIFI_RO)

            turnOnHotspot()
        }

        button_stop.setOnClickListener{
            turnOffHotspot()
            Ssid.text = "your ssid will appear here"
            Password.text = " your Password"
        }





    }






    private fun checkForPermissions(permission:String, name:String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            when{
                ContextCompat.checkSelfPermission(applicationContext,permission)==PackageManager.PERMISSION_GRANTED ->{
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name ,requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)

            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String){
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(applicationContext,"$name permission refused",Toast.LENGTH_SHORT).show()
        }else{
                Toast.makeText(applicationContext,"$name permission granted",Toast.LENGTH_SHORT).show()
            }
    }
        when (requestCode){
            FINE_LOCATION_RO ->innerCheck("location")
            ACCESS_WIFI_RO -> innerCheck("wifi_state")
        }
    }
    private  fun showDialog(permission: String,name: String,requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("permission required")
            setPositiveButton("OK"){ dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission),requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }


    private var mReservation: LocalOnlyHotspotReservation? = null

    private fun turnOnHotspot() {
        Log.d("clicked on", "turn on hotspot")
        val manager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        manager.startLocalOnlyHotspot(object : LocalOnlyHotspotCallback() {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onStarted(reservation: LocalOnlyHotspotReservation) {

                super.onStarted(reservation)
                mReservation = reservation
                if (mReservation!=null){
                    Log.d("ssid", mReservation!!.wifiConfiguration.toString())
                    if(mReservation!!.wifiConfiguration != null){
                        Log.d("ssid", mReservation!!.wifiConfiguration!!.SSID)
                        Ssid.text = mReservation!!.wifiConfiguration!!.SSID
                        Log.d("ssidpassword", mReservation!!.wifiConfiguration!!.preSharedKey)
                        Password.text = mReservation!!.wifiConfiguration!!.preSharedKey
                    }else{
                        Log.d("ssid", "wificongiuration null")
                    }
                }else{
                    Log.d("ssid", "mReservation is null")
                }
            }

            override fun onStopped() {
                super.onStopped()
                Log.d("stoppedthewifi", "onStopped: ")
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                Log.d("wififailed", "onFailed: ")
            }
        }, Handler())
    }

    private fun turnOffHotspot() {
        if (mReservation != null) {
            mReservation!!.close()
        }
    }



}