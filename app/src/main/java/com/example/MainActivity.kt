package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.PrayerScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Emerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrayerQuranViewModel

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var viewModel: PrayerQuranViewModel
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private var rotationSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocation()
        } else {
            Toast.makeText(this, "Location permission is required for accurate GPS alerts.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init view model
        viewModel = ViewModelProvider(this)[PrayerQuranViewModel::class.java]

        // Load sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setContent {
            MyApplicationTheme {
                MainAppLayout(
                    viewModel = viewModel,
                    onRequestLocation = { requestLocation() }
                )
            }
        }

        // Initially request location for GPS calculations
        requestLocation()
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthInRadians = orientation[0]
            val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            val cleanAzimuth = (azimuthInDegrees + 360f) % 360f
            viewModel.setCompassAzimuth(cleanAzimuth)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                hasGravity = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                hasGeomagnetic = true
            }

            if (hasGravity && hasGeomagnetic) {
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val azimuthInRadians = orientation[0]
                    val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                    val cleanAzimuth = (azimuthInDegrees + 360f) % 360f
                    viewModel.setCompassAzimuth(cleanAzimuth)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun requestLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            val provider = when {
                isGpsEnabled -> LocationManager.GPS_PROVIDER
                isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
                else -> null
            }

            if (provider != null) {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        viewModel.setLocation(loc.latitude, loc.longitude, "GPS Detected Coordinates")
                    }

                    // Request a live fine update
                    locationManager.requestSingleUpdate(provider, object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            viewModel.setLocation(location.latitude, location.longitude, "GPS Location Detected")
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }, null)
                }
            } else {
                Toast.makeText(this, "Please enable your GPS services to calculate accurate times.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun MainAppLayout(
    viewModel: PrayerQuranViewModel,
    onRequestLocation: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(DarkBg),
        bottomBar = {
            NavigationBar(
                containerColor = DarkBg,
                tonalElevation = 12.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { viewModel.selectTab("dashboard") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Prayers") },
                    label = { Text("Prayers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = Emerald,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Emerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == "qibla",
                    onClick = { viewModel.selectTab("qibla") },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Qibla Compass") },
                    label = { Text("Qibla") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = Emerald,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Emerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_qibla")
                )
                NavigationBarItem(
                    selected = selectedTab == "quran",
                    onClick = { viewModel.selectTab("quran") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Quran") },
                    label = { Text("Quran") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = Emerald,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Emerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_quran")
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { viewModel.selectTab("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Emerald,
                        unselectedIconColor = TextSecondary,
                        selectedTextColor = Emerald,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Emerald.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> PrayerScreen(viewModel = viewModel, onRequestLocation = onRequestLocation)
                "qibla" -> QiblaScreen(viewModel = viewModel)
                "quran" -> QuranScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
