package cs446.dbc.bluetooth

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import androidx.annotation.RequiresApi
import cs446.dbc.models.BusinessCardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.parcelableCreator
import java.lang.ref.WeakReference

// SSOT for Bluetooth stuff, unfortunately permissions are managed in a seperate model
// so I can't perfectly integrate it in here

class CardReceiveDelegate {
    open fun receiveCard(card: BusinessCardModel) {

    }
}

class BluetoothRepository(private val app: Application) {
    companion object {
        val foregroundPermissions = if (Build.VERSION.SDK_INT <= 30) arrayOf(
            permission.BLUETOOTH, permission.BLUETOOTH_ADMIN, permission.POST_NOTIFICATIONS
        ) else arrayOf(
            permission.BLUETOOTH_SCAN,
            permission.BLUETOOTH_ADVERTISE,
            permission.BLUETOOTH_CONNECT,
            permission.POST_NOTIFICATIONS,
            permission.ACCESS_FINE_LOCATION,
            permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
        )

        val backgroundPermissions = if (Build.VERSION.SDK_INT <= 30) arrayOf() else arrayOf(
            permission.ACCESS_BACKGROUND_LOCATION
        )

        // Used to force False when StateFlow may be used
        private val _falseStateFlow = MutableStateFlow<Boolean>(false)
        private val falseStateFlow = _falseStateFlow.asStateFlow()
    }

    private val bluetoothManager: BluetoothManager =
        app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    // For OPs to work: We need
    // - bluetooth Support
    // - Bluetooth Enabled
    // - Bluetooth Discoverability
    // - Bluetooth Permissions (Needs to be checked every time)

    val bluetoothSupported = bluetoothAdapter != null

    private val _bluetoothEnabled = MutableStateFlow<Boolean>(bluetoothAdapter?.isEnabled ?: false)
    val bluetoothEnabled =
        if (bluetoothSupported) _bluetoothEnabled.asStateFlow() else falseStateFlow

    val receiveDelegate: WeakReference<CardReceiveDelegate?> = WeakReference(null)

    fun checkPermissions(): Boolean {
        // This checks if none of the permissions are not denied, because it allows vac. truth
        return foregroundPermissions.all {
            app.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        } && backgroundPermissions.all {
            app.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }


    @SuppressLint("MissingPermission") // Java perms are diff from
    private val _bluetoothDiscoverable = MutableStateFlow<Boolean>(
        if (checkPermissions()) ((bluetoothAdapter?.scanMode
            ?: -1) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) else false
    )
    val bluetoothDiscoverable =
        if (bluetoothSupported) _bluetoothDiscoverable.asStateFlow() else falseStateFlow

    init {
        if (bluetoothSupported) {
            // Set up intent receivers to update values of
            val stateChangeIntentFilter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            }

            val changeReceiver = object : BroadcastReceiver() {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                            val newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                            _bluetoothEnabled.value = newState == BluetoothAdapter.STATE_ON
                        }

                        BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                            val newState = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)
                            _bluetoothDiscoverable.value =
                                newState == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                        }
                    }
                }
            }

            app.registerReceiver(changeReceiver, stateChangeIntentFilter)
        }
    }


    fun startSharing(outCard: BusinessCardModel) {
        val outParcel = Parcel.obtain()
        outCard.writeToParcel(outParcel, 0)
        val outBytes: ByteArray = outParcel.marshall()
        outParcel.recycle()

        app.startForegroundService(Intent(app, BluetoothShareService::class.java).apply {
            putExtra("outBytes", outBytes)
        })
    }

    fun stopSharing() {
        app.stopService(Intent(app, BluetoothShareService::class.java))
    }

    fun startReceiving() {
        app.startForegroundService(Intent(app, BluetoothReceiveService::class.java))
    }

    fun stopReceiving() {
        app.stopService(Intent(app, BluetoothReceiveService::class.java))
    }
}