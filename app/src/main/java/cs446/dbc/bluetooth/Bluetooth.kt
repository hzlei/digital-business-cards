package cs446.dbc.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
class Bluetooth(context: Context): AppCompatActivity() {
    // setup bluetooth
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java);
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    init {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not support :(", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(context, "Bluetooth exists", Toast.LENGTH_SHORT).show()
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, "Bluetooth is disabled", Toast.LENGTH_SHORT).show()
            val requestMultiplePermissionsLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    isGranted ->
                    if (isGranted.getOrElse("BLUETOOTH") { false }) {
                        Toast.makeText(
                            context,
                            "Bluetooth Permission Already Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                       Toast.makeText(context, "Sorry, but we don't have the permission necessary for bluetooth", Toast.LENGTH_SHORT).show()
                    }
                }
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE))
            }
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

//            ContextCompat.startActivity(context, enableBtIntent, ActivityOptionsCompat.makeBasic().toBundle())
            // First check if we even have permission to use bluetooth
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                val BLUETOOTH_RESULT_CODE = 5
//                val permissions = mutableSetOf(
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.BLUETOOTH_ADMIN,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_ADVERTISE
//                )
//                ActivityCompat.requestPermissions(
//                    context as Activity,
//                    permissions.toTypedArray(),
//                    BLUETOOTH_RESULT_CODE
//                )
//                val permReqLauncher =
//                    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//                        if (isGranted) {
//                            startActivity(enableBtIntent)
//                        } else {
//
//                        }
//                    }
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//            }
        }
        else {
            Toast.makeText(context, "Bluetooth already enabled!", Toast.LENGTH_SHORT).show()
        }
    }
}