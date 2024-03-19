package cs446.dbc.bluetooth

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.core.R
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import cs446.dbc.DBCApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.UUID
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.Semaphore

class BluetoothReceiveService : Service() {
    companion object {
        private const val APPNAME = "DBC"
        private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val NOTIF_CHANNEL = "BT"
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

    // Stub left here for later
    private fun onReceived(outBytes : ByteArray) {
        notificationManager.notify(
            SERVICE_ID, NotificationCompat.Builder(this@BluetoothReceiveService, NOTIF_CHANNEL).apply {
                setSmallIcon(cs446.dbc.R.drawable.placeholder_notif)
                setContentTitle("Received ${outBytes.size.toString()}")
                setContentText(outBytes.decodeToString())
            }.build()
        )
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

    override fun onDestroy() {
        cleanStopSelf()

        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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

        notificationManager.notify(
            SERVICE_ID, NotificationCompat.Builder(this@BluetoothReceiveService, NOTIF_CHANNEL).apply {
                setSmallIcon(cs446.dbc.R.drawable.placeholder_notif)
                setContentTitle("Receiving Business Cards")
                setContentText("Sit back and wait")
            }.build()
        )

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
                                        var remainingBytes: Int = (
                                                (byteBuffer[3].toInt() shl 24) or
                                                (byteBuffer[2].toInt() and 0xff shl 16) or
                                                (byteBuffer[1].toInt() and 0xff shl 8) or
                                                (byteBuffer[0].toInt() and 0xff)
                                            )

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