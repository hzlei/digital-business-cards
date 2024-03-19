package cs446.dbc.bluetooth

import cs446.dbc.DBCApplication
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras


class BluetoothViewModel(app: Application, private val bluetoothRepository: BluetoothRepository) :
    AndroidViewModel(app) {
    companion object {
        val foregroundPermissions = BluetoothRepository.foregroundPermissions
        val backgroundPermissions = BluetoothRepository.backgroundPermissions
        val Factory: AndroidViewModelFactory = object : AndroidViewModelFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application: Application = checkNotNull(extras[APPLICATION_KEY])
                return BluetoothViewModel(
                    application, (application as DBCApplication).container.bluetoothRepository
                ) as T
            }
        }
    }

    // Forward some values from Model
    val bluetoothSupported = bluetoothRepository.bluetoothSupported
    val bluetoothEnabled = bluetoothRepository.bluetoothEnabled
    val bluetoothDiscoverable = bluetoothRepository.bluetoothDiscoverable

    fun checkPermissions() : Boolean {
        return bluetoothRepository.checkPermissions()
    }

    fun startSharing(outBytes : ByteArray) {
        bluetoothRepository.startSharing(outBytes)
    }

    fun stopSharing() {
        bluetoothRepository.stopSharing()
    }

    fun startReceiving() {
        bluetoothRepository.startReceiving()
    }

    fun stopReceiving() {
        bluetoothRepository.stopReceiving()
    }
}
