package cs446.dbc.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cs446.dbc.DBCApplication
import cs446.dbc.models.BusinessCardModel

class BluetoothActionActivity : ComponentActivity() {
    private lateinit var bluetoothRepository : BluetoothRepository
    private var outCard : BusinessCardModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothRepository = (application as DBCApplication).container.bluetoothRepository
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            outCard = intent.getParcelableExtra("outCard", BusinessCardModel::class.java)
        } else {
            outCard = intent.getParcelableExtra("outCard") as BusinessCardModel?
        }
        fgPermLauncher.launch(BluetoothRepository.foregroundPermissions)
    }

    // Nyet, all the annoying ass launchers

    private fun startShare() : Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isLocationEnabled) return false

        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        bluetoothRepository.startSharing(outCard!!)
        return true
    }

    private val enableLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        startShare() // May still fail but who cares in this case, we did ask for perms
        finish()
    }

    private val enableDiscoverableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_CANCELED) {
                bluetoothRepository.startReceiving()
            }
            finish()
        }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) {
                finish()
                return@registerForActivityResult
            }

            if (outCard == null) {
                enableDiscoverableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1800)
                })
            } else {
                if (startShare()) {
                    finish()
                } else {
                    // Try to share first, if fails then ask for location permissions
                    // This one is weird
                    enableLocationLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
        }

    // Foreground perms need to be given before BG perms
    private val bgPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!bluetoothRepository.checkPermissions()) { // All perms should be given at this point
            finish()
            return@registerForActivityResult
        }

        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private val fgPermLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        bgPermLauncher.launch(BluetoothRepository.backgroundPermissions[0])
    }
}