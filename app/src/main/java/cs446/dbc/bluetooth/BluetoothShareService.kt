package cs446.dbc.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Parcel
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.material.R
import cs446.dbc.DBCApplication
import cs446.dbc.models.BusinessCardModel
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Semaphore
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class BTRecord(
    var device: BluetoothDevice,
    var received: AtomicBoolean = AtomicBoolean(false), // Whether this device already received the stuff
)

class BluetoothShareService : Service() {
    companion object {
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val NOTIF_CHANNEL = "BT"
        private const val STOP_ACTION = "DBCSTOPSERVICE"
        private const val SERVICE_ID = 44

        private const val CYCLE_WAIT = 5000L // MS

        private const val MSG_NEW_SHARE = 0 // Used to start looper
        private const val MSG_START_DISCOVERY = 1
        private const val MSG_NEW_DEVICE_FOUND = 2
        private const val MSG_DISCOVERY_STOPPED = 3
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var bluetoothRepository: BluetoothRepository
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var workThread: HandlerThread? = null
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        bluetoothRepository = (application as DBCApplication).container.bluetoothRepository
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter!!
    }

    private fun cleanSelf() {
        serviceLooper?.quit()
        workThread?.interrupt()
        try {
            unregisterReceiver(serviceHandler?.receiver)
        } catch (e: IllegalArgumentException) {
            // Not registered, who care
        }
    }

    private fun createStopIntent(): PendingIntent? {
        return PendingIntent.getService(
            this@BluetoothShareService.application,
            0,
            Intent(this@BluetoothShareService.application, BluetoothShareService::class.java).apply {
                action = STOP_ACTION
            },
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        cleanSelf()

        if (intent.action == STOP_ACTION || !bluetoothRepository.checkPermissions()) {
            stopSelf()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            SERVICE_ID,
            NotificationCompat.Builder(this, NOTIF_CHANNEL).build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )

        workThread = HandlerThread("BluetoothService", Process.THREAD_PRIORITY_FOREGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper).apply {
                obtainMessage().also { msg ->
                    msg.arg1 = MSG_NEW_SHARE
                    msg.data = Bundle().apply {
                        putParcelable("outCard", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.extras!!.getParcelable("outCard", BusinessCardModel::class.java)!!
                        } else {
                            intent.extras!!.getParcelable("outCard")!!
                        })
                    }
                    this.sendMessage(msg)
                }

                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                registerReceiver(this.receiver, filter, null, this)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        private val shareableDevices = HashMap<String, BTRecord>()
        private val shareCount = AtomicInteger(0)
        private lateinit var outBytes: ByteArray
        private val notifBuilder = NotificationCompat.Builder(
            this@BluetoothShareService, NOTIF_CHANNEL
        )

        private fun startNotification() {
            notificationManager.notify(
                SERVICE_ID, notifBuilder.apply {
                    setSmallIcon(cs446.dbc.R.drawable.placeholder_notif)
                    setContentTitle("Sharing a Business Card")
                    setContentText("Starting Now")
                    setDeleteIntent(createStopIntent())
                    addAction(R.drawable.mtrl_ic_cancel, "Stop", createStopIntent())
                    setOnlyAlertOnce(true)
                }.build()
            )
        }

        private fun updateNotification() {
            notificationManager.notify(
                SERVICE_ID, notifBuilder.apply {
                    setContentText("Shared Card ${shareCount.get()} Times")
                }.build()
            )
        }

        @SuppressLint("MissingPermission")
        override fun handleMessage(msg: Message) {
            when (msg.arg1) {
                MSG_NEW_SHARE -> newShare(msg)
                MSG_START_DISCOVERY -> startDiscovery()
                MSG_NEW_DEVICE_FOUND -> addNewDevice(msg)
                MSG_DISCOVERY_STOPPED -> startTransfers()
            }
        }

        // MSG Handlers
        private fun newShare(msg: Message) {
            obtainMessage().also { newMsg ->
                val outCard : BusinessCardModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    msg.data.getParcelable("outCard", BusinessCardModel::class.java)!!
                } else {
                    msg.data.getParcelable("outCard")!!
                }

                val directory = application.getExternalFilesDir(null)
                val outParcel = Parcel.obtain()

                var frontBytes : ByteArray? = null
                var backBytes : ByteArray? = null

                if (outCard!!.front != "") {
                    Log.v("HELP", outCard!!.front)
                    val imageFile = File(directory, outCard!!.front)
                    if (imageFile.exists()) {
                        frontBytes = imageFile.readBytes()
                    } else {
                        outCard!!.front = ""
                    }
                }
                if (outCard!!.back != "") {
                    Log.v("HELP", outCard!!.back)
                    val imageFile = File(directory, outCard!!.back)
                    if (imageFile.exists()) {
                        backBytes = imageFile.readBytes()
                    } else {
                        outCard!!.back = ""
                    }
                }

                outParcel.writeParcelable(outCard!!, 0)
                frontBytes?.apply {
                    outParcel.writeInt(this.size)
                    outParcel.writeByteArray(this)
                }
                backBytes?.apply {
                    outParcel.writeInt(this.size)
                    outParcel.writeByteArray(this)
                }

                outBytes = outParcel.marshall()
                outParcel.recycle()

                shareCount.set(0)
                startNotification()

                newMsg.arg1 = MSG_START_DISCOVERY
                sendMessage(newMsg)
            }
        }

        @SuppressLint("MissingPermission")
        private fun startDiscovery() {
            if (!bluetoothRepository.checkPermissions()) {
                this@BluetoothShareService.stopSelf()
            }

            // Still discovering? Just skip first discovery
            if (bluetoothAdapter.isDiscovering) return

            if (!bluetoothAdapter.startDiscovery()) {
                this@BluetoothShareService.stopSelf()
            }
        }

        @SuppressLint("MissingPermission")
        private fun addNewDevice(msg: Message) {
            if (!bluetoothRepository.checkPermissions()) {
                this@BluetoothShareService.stopSelf()
            }

            val device: BluetoothDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    msg.data.getParcelable("device", BluetoothDevice::class.java)
                } else {
                    msg.data.getParcelable("device")
                }

            device ?: return

            // We're ignoring un-named devices

            device.name ?: return

            val record = shareableDevices.getOrPut(device.address) {
                BTRecord(device)
            }
            record.device = device
        }

        // Only time interrupts matter, because this step take stupid long
        @SuppressLint("MissingPermission")
        private fun startTransfers() {
            if (!bluetoothRepository.checkPermissions()) {
                this@BluetoothShareService.stopSelf()
            }
            val transferSemaphore = Semaphore(0) // Used to count number of transfers complete
            val transferQueue = SynchronousQueue<Runnable>()
            val transferThreadPool = ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS, transferQueue)
            shareableDevices.forEach {
                if (it.value.received.get()) {
                    transferSemaphore.release()
                    return@forEach
                }

                transferThreadPool.execute {
                    var connSock: BluetoothSocket? = null
                    try {
                        connSock =
                            it.value.device.createInsecureRfcommSocketToServiceRecord(APP_UUID)
                        try {
                            connSock.connect()

                            connSock.outputStream.write(
                                byteArrayOf(
                                    (outBytes.size shr 0).toByte(),
                                    (outBytes.size shr 8).toByte(),
                                    (outBytes.size shr 16).toByte(),
                                    (outBytes.size shr 24).toByte()
                                )
                            )
                            connSock.outputStream.write(outBytes)
                            // I don't actually read anything here, I just want to wait for
                            // the receiver to close
                            connSock.inputStream.read()
                            it.value.received.set(true)
                            shareCount.addAndGet(1)
                        } catch (e: Exception) {
                            Log.v("e", e.toString())
                            // Just eat, there is not a lot of errors I care about rn
                        }
                    } catch (e: IOException) {
                        // Rare Failure which means BT not working
                        this@BluetoothShareService.stopSelf()
                    } catch (e: InterruptedException) {
                        // Just close and stop transfers
                    } finally {
                        connSock?.close()
                        transferSemaphore.release()
                    }
                }
            }

            try {
                transferSemaphore.acquire(shareableDevices.size)
            } catch (e: InterruptedException) {
                transferThreadPool.shutdownNow() // Throws interrupts into the threads
                return
            }

            updateNotification()

            obtainMessage().also { newMsg ->
                newMsg.arg1 = MSG_START_DISCOVERY
                sendMessageDelayed(newMsg, CYCLE_WAIT)
            }
        }

        // Used to queue up processing of bluetooth events
        @SuppressLint("MissingPermission")
        val receiver = object : BroadcastReceiver() {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onReceive(context: Context, intent: Intent) {
                if (!bluetoothRepository.checkPermissions()) {
                    this@BluetoothShareService.stopSelf()
                }

                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        this@ServiceHandler.obtainMessage().also { msg ->
                            msg.arg1 = MSG_DISCOVERY_STOPPED
                            serviceHandler!!.sendMessage(msg)
                        }
                    }

                    BluetoothDevice.ACTION_FOUND -> {
                        this@ServiceHandler.obtainMessage().also { msg ->
                            msg.arg1 = MSG_NEW_DEVICE_FOUND
                            msg.data = Bundle().apply {
                                putParcelable(
                                    "device", intent.getParcelableExtra(
                                        BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                                    )!!
                                )
                            }
                            serviceHandler!!.sendMessage(msg)
                        }
                    }
                }
            }
        }
    }
}