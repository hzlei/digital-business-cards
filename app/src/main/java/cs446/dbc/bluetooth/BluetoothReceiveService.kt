package cs446.dbc.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.R
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import cs446.dbc.DBCApplication
import cs446.dbc.models.BusinessCardModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.parcelableCreator
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

class BluetoothReceiveService : Service() {
    companion object {
        private const val APPNAME = "DBC"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val NOTIF_CHANNEL = "BT"
        private const val STOP_ACTION = "DBCSTOPSERVICE"
        private const val SERVICE_ID = 43
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var bluetoothRepository: BluetoothRepository
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var activeConnSemaphore = Semaphore(3)
    private var receiveSocket: BluetoothServerSocket? = null
    private var connectThread: Thread? = null
    private var transferQueue = SynchronousQueue<Runnable>()
    private var transferThreadPool = ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS, transferQueue)

    private val recvCount = AtomicInteger(0)

    // Stub left here for later
    private fun onReceived(outBytes: ByteArray) {
        val directory = application.getExternalFilesDir(null)
        val delegate = bluetoothRepository.receiveDelegate.get() ?: return

        val parcel = Parcel.obtain()
        parcel.unmarshall(outBytes, 0, outBytes.size)
        parcel.setDataPosition(0) // This is extremely important!
        val card : BusinessCardModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(BusinessCardModel::class.java.classLoader, BusinessCardModel::class.java)!!
        } else {
            parcel.readParcelable(BusinessCardModel::class.java.classLoader)!!
        }
        // Save images if they came with
        if (card!!.front != "") {
            val imgFile = File(directory, card.front)
            val imgBytes = ByteArray(parcel.readInt())
            parcel.readByteArray(imgBytes)
            imgFile.writeBytes(imgBytes)
        }
        if (card!!.back != "") {
            val imgFile = File(directory, card.back)
            val imgBytes = ByteArray(parcel.readInt())
            parcel.readByteArray(imgBytes)
            imgFile.writeBytes(imgBytes)
        }
        delegate.receiveCard(card)
    }

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        bluetoothRepository = (application as DBCApplication).container.bluetoothRepository
        bluetoothManager =
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter!!
    }

    fun cleanStopSelf() {
        receiveSocket?.close()
        connectThread?.interrupt()
        transferQueue.clear()
        transferThreadPool.shutdownNow()
        receiveSocket = null
        connectThread = null
        stopSelf()
    }

    private fun createStopIntent(): PendingIntent? {
        return PendingIntent.getService(
            this@BluetoothReceiveService.application,
            0,
            Intent(
                this@BluetoothReceiveService.application, BluetoothReceiveService::class.java
            ).apply {
                action = BluetoothReceiveService.STOP_ACTION
            },
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onDestroy() {
        cleanStopSelf()

        super.onDestroy()
    }


    private val notifBuilder = NotificationCompat.Builder(
        this@BluetoothReceiveService, BluetoothReceiveService.NOTIF_CHANNEL
    )

    private fun startNotification() {
        notificationManager.notify(
            BluetoothReceiveService.SERVICE_ID, notifBuilder.apply {
                setSmallIcon(cs446.dbc.R.drawable.placeholder_notif)
                setContentTitle("Receiving Business Cards")
                setContentText("Haven't Received Any Cards")
                setDeleteIntent(createStopIntent())
                addAction(
                    com.google.android.material.R.drawable.mtrl_ic_cancel,
                    "Stop",
                    createStopIntent()
                )
                setOnlyAlertOnce(true)
            }.build()
        )
    }

    private fun updateNotification() {
        notificationManager.notify(
            BluetoothReceiveService.SERVICE_ID, notifBuilder.apply {
                setContentText("Received ${recvCount.get()} Cards")
            }.build()
        )
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == BluetoothReceiveService.STOP_ACTION) {
            cleanStopSelf()
            return START_NOT_STICKY
        }

        if (receiveSocket != null) return START_NOT_STICKY // Already running, don't do nothing.

        if (!bluetoothRepository.checkPermissions()) {
            cleanStopSelf()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            SERVICE_ID,
            NotificationCompat.Builder(this, NOTIF_CHANNEL).build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )

        recvCount.set(0)
        startNotification()

        receiveSocket =
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APPNAME, APP_UUID)
        connectThread = object : Thread() {
            override fun run() {
                while (!interrupted()) {
                    if (!bluetoothRepository.checkPermissions()) {
                        cleanStopSelf()
                        return
                    }

                    try {
                        activeConnSemaphore.acquire()
                        receiveSocket?.accept().also { sock: BluetoothSocket? ->
                            if (sock == null) {
                                activeConnSemaphore.release()
                            } else {
                                transferThreadPool.execute {
                                    try {
                                        var byteBuffer = ByteArray(1024)

                                        sock.inputStream.read(byteBuffer, 0, 4)
                                        var remainingBytes: Int =
                                            ((byteBuffer[3].toInt() shl 24) or (byteBuffer[2].toInt() and 0xff shl 16) or (byteBuffer[1].toInt() and 0xff shl 8) or (byteBuffer[0].toInt() and 0xff))

                                        var numBytes: Int
                                        var allBytes = byteArrayOf()

                                        while (remainingBytes > 0) {
                                            try {
                                                numBytes = sock.inputStream.read(byteBuffer)
                                                remainingBytes -= numBytes
                                            } catch (e: IOException) {
                                                Log.v("e", e.toString())
                                                break
                                            } catch (e: InterruptedException) {
                                                return@execute
                                            }

                                            allBytes += byteBuffer.slice(0..<numBytes)
                                        }
                                        sock.outputStream.write(0)
                                        onReceived(allBytes)
                                        recvCount.getAndAdd(1)
                                        updateNotification()
                                    } finally {
                                        sock.close()
                                        activeConnSemaphore.release()
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                    } catch (e: InterruptedException) {
                        activeConnSemaphore.release()
                        cleanStopSelf()
                        return
                    }
                }
            }
        }.apply {
            start()
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}