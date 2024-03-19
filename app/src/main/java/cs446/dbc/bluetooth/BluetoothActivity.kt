package cs446.dbc.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cs446.dbc.R

class BluetoothActivity : AppCompatActivity() {

    private var bluetoothService: BluetoothViewModel? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dbc_share_bluetooth) // Ensure you have this layout

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        /*
        bluetoothService = BluetoothService(this) { connected ->
            updateUIOnConnectionStatus(connected)
        }
        */

        // button for starting bt service
        findViewById<Button>(R.id.connectButton)?.setOnClickListener {
            // need logic here to select a device
            // currently a random address
            val deviceAddress = "00:11:22:AA:BB:CC"
            // bluetoothService?.connect(deviceAddress)
        }
    }

    private fun updateUIOnConnectionStatus(connected: Boolean) {
        runOnUiThread {
            if (connected) {
                Toast.makeText(this, "Connected to the device", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Disconnected from the device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // bluetoothService?.stop()
    }
}
