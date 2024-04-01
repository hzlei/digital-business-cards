package cs446.dbc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import cs446.dbc.bluetooth.BluetoothRepository

class AppContainer(app : Application) {
    val bluetoothRepository = BluetoothRepository(app)
}

class DBCApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {

        val mChannel = NotificationChannel("BT", "Bluetooth", NotificationManager.IMPORTANCE_HIGH)
        mChannel.description = "Bluetooth Notification Channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        super.onCreate()
        container = AppContainer(this)
    }
}
