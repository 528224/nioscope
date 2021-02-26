package com.example.nioscope

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_blutooth_activity.*
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Handler
import android.os.Message
import java.lang.Exception




//https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically/14254202
//https://stackoverflow.com/questions/44593085/android-bluetooth-how-to-read-incoming-data

//https://examples.javacodegeeks.com/android/android-bluetooth-connection-example/

//https://www.youtube.com/watch?v=Oz4CBHrxMMs
val EXTRA_ADDRESS = "address"

class BlutoothActivityActivity : AppCompatActivity() {
    val REQUEST_ENABLE_BT = 999
    var BA: BluetoothAdapter? = null
    private var devices = ArrayList<BluetoothDevice>()
    var isPairedDevices = false
    private var mChatService: BluetoothChatService? = null
    // Message types sent from the BluetoothChatService Handler
    val MESSAGE_STATE_CHANGE = 1
    val MESSAGE_READ = 2
    val MESSAGE_WRITE = 3
    val MESSAGE_DEVICE_NAME = 4
    val MESSAGE_TOAST = 5
    val TOAST = "toast"
    val DEVICE_NAME = "device_name"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blutooth_activity)
        BA = BluetoothAdapter.getDefaultAdapter()
        if (BA == null){
            Toast.makeText(applicationContext, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
        }else if (!BA!!.isEnabled()) {

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

        }



        listView.setOnItemClickListener(OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val device = devices[arg2]
            val name = device.name
            val address = device.address
            print(name)
//            if (isPairedDevices){
//                val intent = Intent(this, BlutoothActivity1::class.java)
//                intent.putExtra(EXTRA_ADDRESS, address)
//                startActivity(intent)
//            }else{
//                pairDevice(device)
//            }

            val intent = Intent(this, BlutoothActivity1::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)

//            if(isPairedDevices){
//                // Get the BLuetoothDevice object
//                val device = BA?.getRemoteDevice(address)
//                // Attempt to connect to the device
//                mChatService?.connect(device)
//            }

        })

        mChatService = BluetoothChatService(this, mHandler)

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

    }


    // The Handler that gets information back from the BluetoothChatService
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                }
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                }
                MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    val mConnectedDeviceName = msg.getData().getString(DEVICE_NAME)
                    Toast.makeText(
                        applicationContext,
                        "Connected to $mConnectedDeviceName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                MESSAGE_TOAST -> Toast.makeText(
                    applicationContext, msg.getData().getString(TOAST),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {

            val message: String
            if (resultCode == Activity.RESULT_OK) {

                message = "Bluetooth is on"

            } else {

                message = "Bluetooth is off"

            }
            val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
            toast.show()

        }

    }


    fun on(v: View) {
        if (BA == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(applicationContext, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
        } else if (!BA!!.isEnabled()) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, 0)
            Toast.makeText(applicationContext, "Turned on", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Already on", Toast.LENGTH_LONG).show()
        }
    }

    fun off(v: View) {
        BA?.disable()
        Toast.makeText(applicationContext, "Turned off", Toast.LENGTH_LONG).show()
    }


    fun visible(v: View) {
        if (BA == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(applicationContext, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
        }
        val getVisible = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        startActivityForResult(getVisible, 0)
    }


    fun listBondedDevices(v: View) {
        if (BA == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(applicationContext, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show()
        }
        devices.clear()
        val devices1 = BA?.getBondedDevices()?.toTypedArray() ?: arrayOf()
        for (device in devices1){
            devices.add(device)
        }

        val list = devices.map { it.name } ?: ArrayList()

        Toast.makeText(applicationContext, "Showing Paired Devices", Toast.LENGTH_SHORT).show()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        listView.setAdapter(adapter)
        isPairedDevices = true
        textView2.text = "Paired Devices"
        listView.visibility = View.VISIBLE

    }

    fun listAvailabelDevices(v: View) {

        devices.clear()
//        BA?.startDiscovery()
        isPairedDevices = false
        textView2.text = "Available Devices"
        listView.visibility = View.GONE

        // start looking for bluetooth devices
        BA?.startDiscovery()

        // Discover new devices
        // Create a BroadcastReceiver for ACTION_FOUND
        val mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // Get the bluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val name = device.name

                    // Get the "RSSI" to get the signal strength as integer,
                    // but should be displayed in "dBm" units
                    val rssi =
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, java.lang.Short.MIN_VALUE)
                            .toInt()

                    if (devices.indexOf(device) < 0){
                        devices.add(device)
                    }
                    val list = devices.map { it.name } ?: ArrayList()
                    val adapter = ArrayAdapter(this@BlutoothActivityActivity, android.R.layout.simple_list_item_1, list)
                    listView.setAdapter(adapter)
                    listView.visibility = View.VISIBLE
                }
            }
        }
        // Register the BroadcastReceiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

    }

    override fun onPause() {
        super.onPause()
        BA?.cancelDiscovery();
    }




    private fun pairDevice(device: BluetoothDevice) {
        val intent = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mPairReceiver, intent)
        try {
            val cArg = arrayOfNulls<Class<*>>(1)
            val method = device.javaClass.getMethod("createBond", *cArg )
            method.invoke(device, null as Array<Any>?)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private val mPairReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state =
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(
                    BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                    BluetoothDevice.ERROR
                )

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    val pa = "Paired"
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    val s = "Unpaired"
                }

            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address


                    if (devices.indexOf(device) < 0){
                        devices.add(device)
                        val list = devices.map { it.name } ?: ArrayList()
                        val adapter = ArrayAdapter(this@BlutoothActivityActivity, android.R.layout.simple_list_item_1, list)
                        listView.setAdapter(adapter)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }



}
