package com.example.nioscope

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_blutooth_activity1.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*





private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)



//https://www.youtube.com/watch?v=eg-t_rhDSoM
//https://developer.android.com/guide/topics/connectivity/bluetooth
class BlutoothActivity1 : AppCompatActivity() {


    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected = false
        lateinit var m_address: String
    }
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blutooth_activity1)

        m_address = intent.getStringExtra(EXTRA_ADDRESS) ?: ""
//        object : Thread() {
//            override fun run() {
//                ConnectToDevice(this@BlutoothActivity1).execute()
//            }
//        }.start()
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        object : Thread() {
            override fun run() {
                AcceptThread().run()
            }
        }.start()
        buttonSendData.setOnClickListener { sendCommand(textViewCommand.text.toString()) }
        buttonDisconnect.setOnClickListener { disconnect() }
        buttonRecieveData.setOnClickListener { receiveCommand() }
    }

    private fun sendCommand(input: String){
        try {
            m_bluetoothSocket?.outputStream?.write(input.toByteArray())
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun receiveCommand(){
        try {
            val inputStream = m_bluetoothSocket?.getInputStream()
            val buffer = ByteArray(256)
            var bytes: Int
            while (true){
                try {
                    bytes = inputStream!!.read(buffer)
                    val message = String(buffer, 0, bytes)
                    print(message)
                }catch (e: Exception){
                    e.printStackTrace()
                    break
                }
            }

        }catch (e: Throwable){
            e.printStackTrace()
        }
    }

    private fun disconnect(){
        try {
            m_bluetoothSocket?.close()
            m_bluetoothSocket = null
            m_isConnected = true

        }catch (e: IOException){
            e.printStackTrace()
        }
        finish()
    }

    private class ConnectToDevice(context: Context): AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        lateinit var context: Context

        init {
            this.context = context
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket?.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }


    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            m_bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Simjo", m_myUUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("Message", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }


                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e("Message", "Could not close the connect socket", e)
            }
        }
    }


    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(m_myUUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            m_bluetoothAdapter.cancelDiscovery()

            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("Message", "Could not close the client socket", e)
            }
        }
    }


    fun manageMyConnectedSocket(socket: BluetoothSocket){
        object : Thread() {
            override fun run() {
                val s = "ssss"
            }
        }.start()

    }

    // The Handler that gets information back from the BluetoothChatService
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val m = msg
            print(m)
        }
    }



}

class MyBluetoothService(
    // handler that gets info from Bluetooth service
    private val handler: Handler
) {

    fun connect(socket: BluetoothSocket){
        ConnectedThread(socket).run()
    }
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}
