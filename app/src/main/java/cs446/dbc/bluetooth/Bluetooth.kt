package cs446.dbc.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService

class Bluetooth(context: Context) {
    // setup bluetooth
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java);
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    init {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not support :(", Toast.LENGTH_SHORT).show()
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val BLUETOOTH_RESULT_CODE = 5
                ActivityCompat.requestPermissions(
                    (Activity) context,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    BLUETOOTH_RESULT_CODE
                )
                val permReqLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                        if (isGranted) {
                            startActivity(enableBtIntent)
                        } else {

                        }
                    }
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
        }
    }
}