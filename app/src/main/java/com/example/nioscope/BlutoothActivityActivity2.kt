package com.example.nioscope

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Handler
import android.os.Message
import kotlinx.android.synthetic.main.activity_blutooth_activity2.*


//https://stackoverflow.com/questions/14228289/android-pair-devices-via-bluetooth-programmatically/14254202
class BluetoothDemo : AppCompatActivity() {
    var arrayListpaired: ArrayList<String>? = null
    var adapter: ArrayAdapter<String>? = null
    var detectedAdapter:ArrayAdapter<String>? = null
    var handleSeacrh: HandleSeacrh? = null
    var bdDevice: BluetoothDevice? = null
    var bdClass: BluetoothClass? = null
    var arrayListPairedBluetoothDevices: ArrayList<BluetoothDevice>? = null
    private var clicked: ButtonClicked? = null
    var listItemClickedonPaired: ListItemClickedonPaired? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    var arrayListBluetoothDevices: ArrayList<BluetoothDevice>? = null
    var listItemClicked: ListItemClicked? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blutooth_activity2)
        arrayListpaired = ArrayList()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        clicked = ButtonClicked()
        handleSeacrh = HandleSeacrh()
        arrayListPairedBluetoothDevices = ArrayList()
        /*
         * the above declaration is just for getting the paired bluetooth devices;
         * this helps in the removing the bond between paired devices.
         */
        listItemClickedonPaired = ListItemClickedonPaired()
        arrayListBluetoothDevices = ArrayList()
        adapter = ArrayAdapter(this@BluetoothDemo, android.R.layout.simple_list_item_1,
            arrayListpaired!!
        )
        detectedAdapter =
            ArrayAdapter(this@BluetoothDemo, android.R.layout.simple_list_item_single_choice)
        listViewDetected?.setAdapter(detectedAdapter)
        listItemClicked = ListItemClicked()
        detectedAdapter?.notifyDataSetChanged()
        listViewPaired?.setAdapter(adapter)

    }

    override fun onStart() {
        // TODO Auto-generated method stub
        super.onStart()
        getPairedDevices()
        buttonOn?.setOnClickListener(clicked)
        buttonSearch?.setOnClickListener(clicked)
        buttonDesc?.setOnClickListener(clicked)
        buttonOff?.setOnClickListener(clicked)
        listViewDetected?.setOnItemClickListener(listItemClicked)
        listViewPaired?.setOnItemClickListener(listItemClickedonPaired)
    }

    private fun getPairedDevices() {
        val pairedDevice = bluetoothAdapter?.getBondedDevices()
        if (pairedDevice!!.size > 0) {
            for (device in pairedDevice!!) {
                arrayListpaired?.add(device.name + "\n" + device.address)
                arrayListPairedBluetoothDevices?.add(device)
            }
        }
        adapter?.notifyDataSetChanged()
    }

    inner class ListItemClicked : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // TODO Auto-generated method stub
            bdDevice = arrayListBluetoothDevices?.get(position)
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The dvice : " + bdDevice.toString())
            /*
             * here below we can do pairing without calling the callthread(), we can directly call the
             * connect(). but for the safer side we must usethe threading object.
             */
            //callThread();
            //connect(bdDevice);
            var isBonded: Boolean? = false
            try {
                isBonded = createBond(bdDevice!!)
                if (isBonded!!) {
                    //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                    //adapter.notifyDataSetChanged();
                    getPairedDevices()
                    adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //connect(bdDevice);
            Log.i("Log", "The bond is created: " + isBonded!!)
        }
    }

    inner class ListItemClickedonPaired : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            bdDevice = arrayListPairedBluetoothDevices?.get(position)
            try {
                val removeBonding = removeBond(bdDevice!!)
                if (removeBonding!!) {
                    arrayListpaired?.removeAt(position)
                    adapter?.notifyDataSetChanged()
                }


                Log.i("Log", "Removed" + removeBonding!!)
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
    }


    private fun connect(bdDevice: BluetoothDevice): Boolean {
        var bool: Boolean? = false
        try {
            Log.i("Log", "service method is called ")
            val cl = Class.forName("android.bluetooth.BluetoothDevice")
            val par = arrayOf<Class<*>>()
            val method = cl.getMethod("createBond", *par)
            val args = arrayOf<Any>()
            bool =
                method.invoke(bdDevice) as Boolean//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (e: Exception) {
            Log.i("Log", "Inside catch of serviceFromDevice Method")
            e.printStackTrace()
        }

        return bool!!
    };


    @Throws(Exception::class)
    fun removeBond(btDevice: BluetoothDevice): Boolean {
        val btClass = Class.forName("android.bluetooth.BluetoothDevice")
        val removeBondMethod = btClass.getMethod("removeBond")
        val returnValue = removeBondMethod.invoke(btDevice) as Boolean
        return returnValue
    }


    @Throws(Exception::class)
    fun createBond(btDevice: BluetoothDevice): Boolean {
        val class1 = Class.forName("android.bluetooth.BluetoothDevice")
        val createBondMethod = class1.getMethod("createBond")
        val returnValue = createBondMethod.invoke(btDevice) as Boolean
        return returnValue
    }


    internal inner class ButtonClicked : View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.buttonOn -> onBluetooth()
                R.id.buttonSearch -> {
                    arrayListBluetoothDevices?.clear()
                    startSearching()
                }
                R.id.buttonDesc -> makeDiscoverable()
                R.id.buttonOff -> offBluetooth()
                else -> {
                }
            }
        }
    }


    private val myReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val msg = Message.obtain()
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show()

                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                try {
                    //device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                } catch (e: Exception) {
                    Log.i("Log", "Inside the exception: ")
                    e.printStackTrace()
                }

                if (arrayListBluetoothDevices!!.size < 1)
                // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the arraylist.
                    detectedAdapter?.add(device!!.name + "\n" + device.address)
                    arrayListBluetoothDevices?.add(device)
                    detectedAdapter?.notifyDataSetChanged()
                } else {
                    var flag =
                        true    // flag to indicate that particular device is already in the arlist or not
                    for (i in arrayListBluetoothDevices!!.indices) {
                        if (device!!.address == arrayListBluetoothDevices?.get(i)?.address) {
                            flag = false
                        }
                    }
                    if (flag == true) {
                        detectedAdapter?.add(device!!.name + "\n" + device.address)
                        arrayListBluetoothDevices?.add(device)
                        detectedAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun startSearching() {
//        Log.i("Log", "in the start searching method")
//        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        this@BluetoothDemo.registerReceiver(myReceiver, intentFilter)
//        bluetoothAdapter?.startDiscovery()


        val list = bluetoothAdapter?.bondedDevices
        for ( device in list!!){
            detectedAdapter?.add(device.name + "\n" + device.address)
            arrayListBluetoothDevices?.add(device)
        }
        detectedAdapter?.notifyDataSetChanged()



    }

    private fun onBluetooth() {
        if (!bluetoothAdapter!!.isEnabled()) {
            bluetoothAdapter?.enable()
            Log.i("Log", "Bluetooth is Enabled")
        }
    }

    private fun offBluetooth() {
        if (bluetoothAdapter!!.isEnabled()) {
            bluetoothAdapter?.disable()
        }
    }

    private fun makeDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
        Log.i("Log", "Discoverable ")
    }

    inner class HandleSeacrh : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                111 -> {
                }

                else -> {
                }
            }
        }
    }

}
