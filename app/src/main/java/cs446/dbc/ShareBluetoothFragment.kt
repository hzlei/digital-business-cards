package cs446.dbc

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cs446.dbc.bluetooth.BluetoothViewModel
import cs446.dbc.databinding.DbcShareBluetoothBinding
import kotlin.random.Random

class ShareBluetoothFragment : Fragment() {
    private val bluetoothVM: BluetoothViewModel by viewModels { BluetoothViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = DbcShareBluetoothBinding.inflate(inflater, container, false)

        binding.button1.setOnClickListener {
            startSharing()
        }

        binding.button2.setOnClickListener {
            bluetoothVM.stopSharing()
        }

        binding.button3.setOnClickListener {
            startReceiving()
        }
        binding.button4.setOnClickListener {
            bluetoothVM.stopReceiving()
        }

        return binding.root
    }

    // These launchers have to be within a fragment and run in this specific order
    private var requestShare = false

    private fun startShare() : Boolean {
        val locationManager = this.activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isLocationEnabled) return false

        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        bluetoothVM.startSharing((1..5000)
            .map { Random.nextInt(0, charPool.size).let { r -> charPool[r] } }
            .joinToString("").encodeToByteArray())
        return true
    }

    private val enableLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            startShare()
        }

    private val enableDiscoverableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_CANCELED) bluetoothVM.startReceiving()
        }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            if (requestShare) {
                if (!startShare()) {
                    enableLocationLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                enableDiscoverableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1800)
                })
            }
        }

    // Foreground perms need to be given before BG perms
    private val bgPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        if (!bluetoothVM.checkPermissions()) return@registerForActivityResult

        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private val fgPermLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        // If foreground perms were not given, this launcher will auto fail
        bgPermLauncher.launch(BluetoothViewModel.backgroundPermissions[0])
    }

    private fun startSharing() {
        requestShare = true
        fgPermLauncher.launch(BluetoothViewModel.foregroundPermissions)
    }

    private fun startReceiving() {
        requestShare = false
        fgPermLauncher.launch(BluetoothViewModel.foregroundPermissions)
    }
}