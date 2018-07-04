package com.example.administrator.neuroelectricstimulator

import android.app.AlertDialog
import android.app.Fragment
import android.app.ListFragment
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.manualequipment.*
import kotlinx.android.synthetic.main.starttreat.*
import kotlinx.android.synthetic.main.starttreat2.*
import kotlinx.android.synthetic.main.treatment_fragment.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-03
 */
class StarttreatFragment: Fragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    var mHandler = Handler()
    private var mProc: ProtocolProc? = ProtocolProc()
    private val MSG_CMD_0 = 10
    private val MSG_CMD_7 = 17
    private val MSG_CMD_1 = 11
    private val MSG_CMD_100=100
    private val electric_multiplier = 0.05.toFloat()
    private var electric_index = 20
    private var electric_index3 = 20
    private val electric_max = 190

    private val REQUEST_ENABLE_BT =1
    private  val SCAN_PERIOD :Long = 5000;
    private var mScanning = true
    //private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var popupMenu: PopupMenu? = null
    private var progressDialog: ProgressDialog?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.starttreat,container,false)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        mHandler = object : Handler(){
            override fun handleMessage(msg: Message){
                when (msg.what){
                    MSG_CMD_0 -> {
                        val info = msg.obj as ProtocolProc.ExtraInfo
                        val connNum = info.ConnNum
                        var str:String=""
                        if (connNum==0){
                            str="android:switcher:" + R.id.viewpager + ":0"
                        }else{
                            str="android:switcher:" + R.id.viewpager + ":1"
                        }
                        var frag=mActivity!!.getSupportFragmentManager().findFragmentByTag(str)as FirstFragment
                        if (frag != null) {
                            val newMsg = Message()
                            newMsg.what = MSG_CMD_0
                            newMsg.obj = msg.obj
                            frag?.mHandler?.sendMessage(newMsg)
                        }
                    }
                    MSG_CMD_1 -> {
                        val info = msg.obj as ProtocolProc.ExtraInfo
                        val connNum = info.ConnNum
                        var str:String=""
                        if (connNum==0){
                            str="android:switcher:" + R.id.viewpager + ":0"
                        }else{
                            str="android:switcher:" + R.id.viewpager + ":1"
                        }
                        var frag=mActivity!!.getSupportFragmentManager().findFragmentByTag(str)as FirstFragment
                        if (frag != null) {
                            val newMsg = Message()
                            newMsg.what = MSG_CMD_1
                            newMsg.obj = msg.obj
                            frag?.mHandler?.sendMessage(newMsg)
                        }
                    }
                    MSG_CMD_7 -> {
                        val info = msg.obj as ProtocolProc.ExtraInfo
                        val connNum = info.ConnNum
                        var str:String=""
                        if (connNum==0){
                            str="android:switcher:" + R.id.viewpager + ":0"
                        }else{
                            str="android:switcher:" + R.id.viewpager + ":1"
                        }
                        var frag=mActivity!!.getSupportFragmentManager().findFragmentByTag(str)as FirstFragment
                        if (frag != null) {
                            val newMsg = Message()
                            newMsg.what = MSG_CMD_7
                            newMsg.obj = msg.obj
                            frag?.mHandler?.sendMessage(newMsg)
                        }
                    }
                }
            }
        }


        //设置蓝牙扫描的回调函数
//        mActivity!!.mble!!.setScanCallBack(mLeScanCallback)
        // Initializes list view adapter.
        //mLeDeviceListAdapter = LeDeviceListAdapter(activity)
        //setListAdapter(mLeDeviceListAdapter)

//        mHandler = object : Handler() {
//            override fun handleMessage(msg: Message) {
//                when (msg.what) {
//                    MSG_CMD_100 ->{
//                        progressDialog!!.dismiss()
//                        var vl = msg.obj as Int
//                        if (vl==1){
//                            if (mActivity!!.mStatus==1){
//                                txtStatus?.text=RString(R.string.dev_connected1)
//                                ImgConn.setImageResource(R.drawable.sa)
//                                Toast.makeText(activity, R.string.dev_connected, Toast.LENGTH_SHORT).show()
//                            }else{
//                                txtStatus?.text=RString(R.string.dev_connNO)
//                                Toast.makeText(activity, R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                        if (vl==2){
//                            if (mActivity!!.mStatus1==1){
//                                txtStatus1?.text=RString(R.string.dev_connected1)
//                                ImgConn1.setImageResource(R.drawable.sa)
//                                Toast.makeText(activity, R.string.dev_connected, Toast.LENGTH_SHORT).show()
//                            }else{
//                                txtStatus1?.text=RString(R.string.dev_connNO)
//                                Toast.makeText(activity, R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                    MSG_CMD_0 -> {
//                        dealMsg0(msg)
//                    }
//                    MSG_CMD_1 ->{
//                        val info3 = msg.obj as ProtocolProc.ExtraInfo
//                        val newCurrent = info3.Current
//                        val cha = info3.channelType
//                        val connNum = info3.ConnNum
//                        if (connNum == 0) {
//                            if (cha.toInt() == 0) {
//                                electric_index = newCurrent
//                                if (electric_index > electric_max)
//                                    electric_index = electric_max
//                                if (electric_index < 1)
//                                    electric_index = 1
//                                updateCurrent(electric_index)
//                            }
//                        }
//                        if (connNum == 1) {
//                            if (cha.toInt() == 0) {
//                                electric_index3 = newCurrent
//                                if (electric_index3 > electric_max)
//                                    electric_index3 = electric_max
//                                if (electric_index3 < 1)
//                                    electric_index3 = 1
//                                updateCurrent3(electric_index3)
//                            }
//                        }
//                    }
//                    MSG_CMD_7 ->{
//                        val connNum7 = (msg.obj as ProtocolProc.ExtraInfo).ConnNum
//                        if (connNum7 == 0) {
//                           mActivity!!.StimuStatus = (msg.obj as ProtocolProc.ExtraInfo).StimuStatus.toInt()
//                            updateButton()
//                        }
//                        if (connNum7 == 1) {
//                            mActivity!!.StimuStatus1 = (msg.obj as ProtocolProc.ExtraInfo).StimuStatus.toInt()
//                            updateButton1()
//                        }
//                    }
//                }
//            }
//        }
//        mProc!!.init_sys(mHandler,activity)

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar_starttreat.setOnClickListener{
            mActivity?.changeFragment(TreatmentFragment())
        }

//        //增加蓝牙搜索菜单 2018.06.12 mly
//        popupMenu = PopupMenu(activity,rootView?.findViewById(R.id.add_ble_research))
//        popupMenu?.menuInflater?.inflate(R.menu.menu_bleresearch,popupMenu?.menu)
//        toolbar_treatment.setNavigationOnClickListener{
//            mActivity?.changeFragment(MainFragment())
//        }
//        add_ble_research.setOnClickListener {
//            popupMenu?.show()  //显示弹出菜单
//        }
//        //监听弹出菜单事件
//        popupMenu?.setOnMenuItemClickListener {item ->
//            when(item.itemId){
//                R.id.researching->mActivity?.changeFragment(ManualequipmentFragment())
//            }
//            false
//        }

//        xmlRead()

//
//        if (mActivity?.mStatus==1){
//            ImgConn.setImageResource(R.drawable.sa)
//            txtStatus?.text=RString(R.string.dev_connected1)
//            if (mActivity!!.mble != null) {
//                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
//                    mProc!!.send_cmd_0(mActivity?.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
//                }
//            }
//        }
//        if (mActivity?.mStatus1==1){
//            ImgConn1.setImageResource(R.drawable.sa)
//            txtStatus1?.text=RString(R.string.dev_connected1)
//            if (mActivity!!.mble != null) {
//                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
//                    mProc!!.send_cmd_0(mActivity?.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
//                }
//            }
//        }
//        var btnImg=rootView?.findViewById<ImageView>(R.id.ImgConn)
//        btnImg!!.setOnClickListener(this)
//        var btnImg1=rootView?.findViewById<ImageView>(R.id.ImgConn1)
//        btnImg1!!.setOnClickListener(this)
//        var btnMac=rootView?.findViewById<Button>(R.id.btnMac)
//        btnMac!!.setOnClickListener(this)
//        var btnMac1=rootView?.findViewById<Button>(R.id.btnMac1)
//        btnMac1!!.setOnClickListener(this)
//        var btnAdd=rootView?.findViewById<Button>(R.id.btnAdd)
//        btnAdd!!.setOnClickListener(this)
//        var btnAdd1=rootView?.findViewById<Button>(R.id.btnAdd1)
//        btnAdd1!!.setOnClickListener(this)
//        var btnReduce=rootView?.findViewById<Button>(R.id.btnReduce)
//        btnReduce!!.setOnClickListener(this)
//        var btnReduce1=rootView?.findViewById<Button>(R.id.btnReduce1)
//        btnReduce1!!.setOnClickListener(this)

//        bluetoothStart.setOnClickListener{
//            //判断本地蓝牙是否已打开 调用系统API去打开蓝牙
//            if (!mActivity!!.mble!!.isOpened) {
//                val openIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(openIntent, REQUEST_ENABLE_BT)
//            }
//            scanLeDevice(true)        //开始蓝牙扫描
//        }

       // mActivity!!.initData()
        initData1()
        viewpager.adapter = mActivity!!.adapter
    }

    fun initData1() {
        var list = java.util.ArrayList<android.support.v4.app.Fragment>()
        var vId=ArrayList<Int>()

        for(i in 0 until mActivity!!.equip_array.size){
            list?.add(FirstFragment())
            vId!!.add(mActivity!!.equip_array[i])
        }
        //初始化adapter
        //mActivity!!.adapter = FragmentAdapter(mActivity!!.supportFragmentManager, list)//mActivity!!.fragmentManager

        mActivity!!.adapter = FragmentAdapter(activity,mActivity!!.supportFragmentManager,list,vId)



        //将适配器和ViewPager结合
        //supportFragmentManager.findFragmentById(R.id.lin_treat).view!!.findViewById<ViewPager>(R.id.viewpager).adapter = adapter
        //viewpager.setAdapter(adapter)
    }


//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.ImgConn ->{
//                if (txtAddress?.text==""){
//                    Toast.makeText(activity, R.string.dev_con_MACNull, Toast.LENGTH_SHORT).show()
//                }else{
//                    progressDialog= ProgressDialog(activity)
//                    progressDialog!!.setMessage(RString(R.string.dev_connecting))
//                    progressDialog!!.show()
//                    txtStatus?.text=RString(R.string.dev_connecting1)
//                    if (mActivity!!.mStatus!=1){
//                        mActivity!!.connNum=0
//                        Conn(txtAddress?.text.toString(),1)
//                    }
//                }
//            }
//            R.id.ImgConn1 ->{
//                if (txtAddress1?.text==""){
//                    Toast.makeText(activity, R.string.dev_con_MACNull, Toast.LENGTH_SHORT).show()
//                }else{
//                    progressDialog= ProgressDialog(activity)
//                    progressDialog!!.setMessage(RString(R.string.dev_connecting))
//                    progressDialog!!.show()
//                    txtStatus1?.text=RString(R.string.dev_connecting1)
//                    if (mActivity!!.mStatus1!=1){
//                        mActivity!!.connNum=1
//                        Conn(txtAddress1?.text.toString(),1)
//                    }
//                }
//            }
//            R.id.btnMac ->{
//                if (mActivity!!.mStatus==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
//                            var cmdWord:Int=0
//                            if (btnMac?.text=="Start"){
//                                cmdWord=2
//                            }
//                            mProc!!.send_cmd_7(mActivity!!.mble, cmdWord, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity, R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btnMac1 ->{
//                if (mActivity!!.mStatus1==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
//                            var cmdWord:Int=0
//                            if (btnMac1?.text=="Start"){
//                                cmdWord=2
//                            }
//                            mProc!!.send_cmd_7(mActivity!!.mble, cmdWord, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btnAdd ->{
//                if (mActivity!!.mStatus==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.StimuStatus != 2){
//                            Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
//                        }else{
//                            if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
//                                mProc!!.send_cmd_1(mActivity!!.mble, electric_index, true, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
//                            }
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btnReduce ->{
//                if (mActivity!!.mStatus==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.StimuStatus != 2){
//                            Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
//                        }else{
//                            if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
//                                mProc!!.send_cmd_1(mActivity!!.mble, electric_index, false, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
//                            }
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btnAdd1 ->{
//                if (mActivity!!.mStatus1==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.StimuStatus1 != 2){
//                            Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
//                        }else{
//                            if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
//                                mProc!!.send_cmd_1(mActivity!!.mble, electric_index3, true, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
//                            }
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//            R.id.btnReduce1 ->{
//                if (mActivity!!.mStatus1==1){
//                    if (mActivity!!.mble != null) {
//                        if (mActivity!!.StimuStatus1 != 2){
//                            Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
//                        }else{
//                            if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
//                                mProc!!.send_cmd_1(mActivity!!.mble, electric_index3, false, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
//                            }
//                        }
//                    }
//                }else{
//                    Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

//    private fun dealMsg0(msg: Message) {
//        val info = msg.obj as ProtocolProc.ExtraInfo
//
//        val connNum = info.ConnNum
//        val channel = info.channelType
//
//        if (connNum == 0) {
//            mActivity!!.StimuStatus =info.StimuStatus .toInt()
//            updateButton()
//            electric_index = info.Current
//            updateCurrent(electric_index)
//        } else {
//            mActivity!!.StimuStatus1 =info.StimuStatus .toInt()
//            updateButton1()
//            electric_index3 = info.Current
//            updateCurrent3(electric_index3)
//        }
//    }
//    private fun updateCurrent(steps: Int) {
//        var steps = steps
//        if (steps < 1) steps = 1
//        if (steps > electric_max) steps = electric_max
//        val tv =rootView?.findViewById<TextView>(R.id.txtMA)
//        val display_value = steps * electric_multiplier
//        val display = String.format("%.2f", display_value)
//        tv?.text = "A:" + display + "mA"
//    }
//    private fun updateCurrent3(steps: Int) {
//        var steps = steps
//        if (steps < 1) steps = 1
//        if (steps > electric_max) steps = electric_max
//        val tv =rootView?.findViewById<TextView>(R.id.txtMA1)
//        val display_value = steps * electric_multiplier
//        val display = String.format("%.2f", display_value)
//        tv?.text = "A:" + display + "mA"
//    }
//    private fun updateButton() {
//        if (mActivity!!.StimuStatus == 2)
//        {
//            btnMac.setBackgroundColor(Color.RED)
//            btnMac.text="Stop"
//        }
//        if (mActivity!!.StimuStatus == 0 || mActivity!!.StimuStatus == 0xFF) {
//            btnMac.setBackgroundColor(Color.GREEN)
//            btnMac.text="Start"
//        }
//
//    }
//    private fun updateButton1() {
//
//        if (mActivity!!.StimuStatus1 == 2)
//        {
//            btnMac1.setBackgroundColor(Color.RED)
//            btnMac1.text="Stop"
//        }
//        if (mActivity!!.StimuStatus1 == 0 || mActivity!!.StimuStatus1 == 0xFF) {
//            btnMac1.setBackgroundColor(Color.GREEN)
//            btnMac1.text="Start"
//        }
//
//    }

//    private fun xmlRead(){
//
//        try{
//            var file= File(activity.getFilesDir(),"equipment.xml")
//            var fis = FileInputStream(file);
//
//            var parser=Xml.newPullParser()
//            parser.setInput(fis, "utf-8")
//            var eventType = parser.getEventType()
//            var id:String?=null
//            var name:String?=null
//            var address:String?=null
//            var pres:String?=null
//            var cfId:String?=null
//            var capacity:String?=null
//            var prodCode:String?=null
//            var equipNum:Int=0
//            while (eventType != XmlPullParser.END_DOCUMENT){
//                var tagName = parser.getName()
//                when (eventType){
//                    XmlPullParser.START_TAG ->{
//                        if ("enames".equals(tagName)) {
//                        } else if ("ename".equals(tagName)) {
//                            id = parser.getAttributeValue(null, "id")
//                        } else if ("name".equals(tagName)) {
//                            name = parser.nextText()
//                        }else if ("address".equals(tagName)) {
//                            address = parser.nextText()
//                        } else if ("pres".equals(tagName)) {
//                            pres = parser.nextText()
//                        }else if ("capacity".equals(tagName)) {
//                            capacity = parser.nextText()
//                        }else if ("prodCode".equals(tagName)) {
//                            prodCode = parser.nextText()
//                        }else if ("cfid".equals(tagName)) {
//                            cfId = parser.nextText()
//                        }
//                    }
//                    XmlPullParser.END_TAG ->{
//                        if (id!=null){
//                            var bool:Boolean=false
//                            for(i in 0 until mActivity!!.equip_array.size){
//                                if (Integer.parseInt(id)==mActivity!!.equip_array[i]){
//                                    bool=true
//                                }
//                            }
//                            if (bool==true){
//                                if (equipNum==0){
//                                    equip_1.setVisibility(View.VISIBLE)//.setVisibility(View.INVISIBLE)
//                                    txtAddress.text=address
//                                    txtBW1.text=pres
//                                    bool=false
//                                }
//                                if (equipNum==1){
//                                    equip_2.setVisibility(View.VISIBLE)
//                                    txtAddress1.text=address
//                                    txtBW2.text=pres
//                                    bool=false
//                                }
//                                equipNum++
//                            }
//                        }
//                        id=null
//                        name=null
//                        address=null
//                        pres=null
//                        cfId=null
//                        capacity=null
//                        prodCode=null
//                    }
//                }
//                eventType = parser.next()
//            }
//        }catch (e: IOException){
//
//        }
//
//    }

//    private fun scanLeDevice(enable: Boolean) {
//        if (enable) {
//            // SCAN_PERIOD 秒后停止扫描
//            mHandler.postDelayed(cancelScan, SCAN_PERIOD)
//
//            mScanning = true
//            mActivity!!.mble!!.startLeScan()    //开始蓝牙扫描
//        } else {
//            //取消停止扫描的线程
//            mHandler.removeCallbacks(cancelScan)
//            mScanning = false
//            mActivity!!.mble!!.stopLeScan()    //停止蓝牙扫描
//        }
//        //        invalidateOptionsMenu();
//
//    }
//
//    internal var cancelScan: Runnable = object : Runnable {
//        override fun run() {
//            mActivity!!.mble!!.stopLeScan()
//            try {
//                Thread.sleep(500)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//
//            mActivity!!.mble!!.startLeScan()
//            mHandler.postDelayed(this, SCAN_PERIOD)
//            //            invalidateOptionsMenu();
//        }
//    }

    // 扫描的结果
//    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
//        if (device.name!=null){
//            var ad1 :String=device.name
//            if (ad1=="MedKinetic Electroceuticals"  ){
//                if (device.address==txtAddress.text.toString() && mActivity!!.mStatus==1){
//                }else if (device.address==txtAddress1.text.toString() && mActivity!!.mStatus1==1){
//                }else{
//                    mLeDeviceListAdapter!!.addDevice(BleModel(device.address,device.name))
//                    mLeDeviceListAdapter!!.notifyDataSetChanged()
//                }
//            }
//        }
//
//    }
//    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long){
//        val device = mLeDeviceListAdapter!!.getDevice(position) ?: return
//        val address = mLeDeviceListAdapter!!.getDevice(position)!!.address
//        val tv =rootView!!.findViewById<TextView>(R.id.txtAddress)
//        val tv1 =rootView!!.findViewById<TextView>(R.id.txtAddress1)
//        if (address!=tv.text.toString() && address!=tv1.text.toString()){
//            Toast.makeText(activity, R.string.dev_con_MACNull, Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (address==tv.text.toString() && mActivity!!.mStatus==1){
//            Toast.makeText(activity, R.string.dev_con_OK, Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (address==tv1.text.toString() && mActivity!!.mStatus1==1){
//            Toast.makeText(activity,  R.string.dev_con_OK, Toast.LENGTH_SHORT).show()
//            return
//        }
//        mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNECTING
//        if (mActivity!!.mStatus!=1){
//            if (tv.text!=address){
//                tv1.text=tv.text
//            }
//            tv.text=address
//        }else if (mActivity!!.mStatus1!=1){
//            tv1.text=address
//        }
//        if (mActivity!!.mStatus!=1){
//            progressDialog= ProgressDialog(activity)
//            progressDialog!!.setMessage(RString(R.string.dev_connecting))
//            progressDialog!!.show()
//            txtStatus?.text=RString(R.string.dev_connecting1)
//            mActivity!!.connNum=0
//            Conn(txtAddress?.text.toString(),1)
//        }else if (mActivity!!.mStatus1!=1){
//            progressDialog= ProgressDialog(activity)
//            progressDialog!!.setMessage(RString(R.string.dev_connecting))
//            progressDialog!!.show()
//            txtStatus1?.text=RString(R.string.dev_connecting1)
//            mActivity!!.connNum=1
//            Conn(txtAddress1?.text.toString(),1)
//        }
//
//        if (mScanning==true){
//            scanLeDevice(false)        //停止蓝牙扫描
//        }
//        mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNETED
//        val textView = v.findViewById<View>(R.id.status) as TextView
//        textView.text = mLeDeviceListAdapter!!.mLeDevices[position].statusDescription
//    }

//    private inner class LeDeviceListAdapter(context: Context) : BaseAdapter() {
//        val mLeDevices: ArrayList<BleModel>
//        val mLeAddress: ArrayList<String>
//        val mInflator: LayoutInflater
//
//        init {
//            mLeDevices = ArrayList()
//            mLeAddress = ArrayList()
//            mInflator = LayoutInflater.from(context)//MainActivity.this.getLayoutInflater();
//        }
//
//        fun getDevice(position: Int): BleModel? {
//            return mLeDevices[position]
//        }
//
//        fun clear() {
//            for (bleModel in mLeDevices) {
//                if (bleModel.gatt != null) {
//                    bleModel.gatt.disconnect()
//                }
//            }
//            mLeDevices.clear()
//            mLeAddress.clear()
//            notifyDataSetChanged()
//        }
//
//        override fun getCount(): Int {
//            return mLeDevices.size
//        }
//
//        override fun getView(i: Int, view: View?, parent: ViewGroup): View {
//            var view = view
//
//            val viewHolder: ViewHolder
//            // General ListView optimization code.
//            if (view == null) {
//                //                view = mInflator.inflate(R.layout.activity_main, null);
//                view = mInflator.inflate(R.layout.list, null)
//                viewHolder = ViewHolder()
//                viewHolder.nameTextView = view!!.findViewById<View>(R.id.name) as TextView
//                viewHolder.addressTextView = view.findViewById<View>(R.id.mac) as TextView
//                viewHolder.statusTextView = view.findViewById<View>(R.id.status) as TextView
//                viewHolder.sendRecvLayout = view.findViewById<View>(R.id.send_recv) as LinearLayout
//                viewHolder.sendTextView = view.findViewById<View>(R.id.send_text) as TextView
//                viewHolder.recvTextView = view.findViewById<View>(R.id.recv_text) as TextView
//                view.tag = viewHolder
//            } else {
//                viewHolder = view.tag as ViewHolder
//            }
//
//            val bleModel = mLeDevices[i]
//            viewHolder.nameTextView!!.text = bleModel.name
//            viewHolder.addressTextView!!.text = bleModel.address
//            viewHolder.statusTextView!!.text = bleModel.statusDescription
//
//            if (bleModel.sendData == null && bleModel.recvData == null) {
//                viewHolder.sendRecvLayout!!.visibility = View.GONE
//            } else {
//                viewHolder.sendRecvLayout!!.visibility = View.VISIBLE
//                var sendData = "发送:"
//                var recvData = "接收:"
//                if (bleModel.sendData != null) {
//                    sendData += bleModel.sendData
//                }
//                if (bleModel.recvData != null) {
//                    recvData += bleModel.recvData
//                }
//
//                viewHolder.sendTextView!!.text = sendData
//                viewHolder.recvTextView!!.text = recvData
//            }
//
//
//
//            return view
//        }
//
//        override fun getItemId(position: Int): Long {
//            return 0
//        }
//
//        override fun getItem(position: Int): Any? {
//            return null
//        }
//
//        fun addDevice(device: BleModel) {
//            if (!mLeAddress.contains(device.address)) {
//                mLeDevices.add(device)
//                mLeAddress.add(device.address)
//            }
//        }
//    }
//
//    internal class ViewHolder {
//        var nameTextView: TextView? = null
//        var addressTextView: TextView? = null
//        var statusTextView: TextView? = null
//        var sendRecvLayout: LinearLayout? = null
//        var sendTextView: TextView? = null
//        var recvTextView: TextView? = null
//    }
//
//    private fun RString(id: Int): String {
//        return resources.getString(id)
//    }
//
//    fun Conn(address: String,type:Int){
//
//        Handler().post(Runnable {
//            try{
//                var i: Int
//                i = 0
//                while (i < 5){
//
//                    if (mActivity!!.connNum == 0) {
//                        if (mActivity!!.mble!!.connectDevice(address, mActivity!!.bleCallBack))
//                        //连接蓝牙设备
//                            break
//                    }
//                    if (mActivity!!.connNum == 1) {
//                        if (mActivity!!.mble!!.connectDevice1(address, mActivity!!.bleCallBack1))
//                        //连接蓝牙设备
//                            break
//                    }
//
//                    try {
//                        Thread.sleep(500, 0)//200ms
//                        progressDialog!!.dismiss()
//                    } catch (e: Exception) {
//
//                    }
//                    i++
//                }
//
//                val newMsg = Message()
//                Log.d("teST", "I VALUE IS :" + i)
//                if (i == 5) {
//                    //addLogText("",Color.RED,0);
//                    // connecctionStatus(RString(R.string.dev_con_failed));
//                    if (mActivity!!.connNum == 0) {
//                        mActivity!!.mStatus = 0
//                    }
//                    if (mActivity!!.connNum == 1) {
//                        mActivity!!.mStatus1 =0
//                    }
//                    newMsg.what = MSG_CMD_100
//                    newMsg.obj = 0
//                    mHandler?.sendMessage(newMsg)
//                }
//                try {
//                    Thread.sleep(200, 0)//200ms
//                    progressDialog!!.dismiss()
//                } catch (e: Exception) {
//
//                }
//
//                if (mActivity!!.mble!!.wakeUpBle(address)) {
//                    newMsg.what = MSG_CMD_100
//                    if (mActivity!!.connNum == 0) {
//                        newMsg.obj = 1
//                        mActivity!!.mStatus = 1
//                    }
//                    if (mActivity!!.connNum == 1) {
//                        newMsg.obj = 2
//                        mActivity!!.mStatus1 = 1
//                    }
//                    mProc!!.send_cmd_0(mActivity!!.mble, address)
//                    mActivity!!.connNum++
//                    mHandler?.sendMessage(newMsg)
//                } else {
//                    if (mActivity!!.connNum == 0) {
//                        mActivity!!.mStatus = 0
//                    }
//                    if (mActivity!!.connNum == 1) {
//                        mActivity!!.mStatus1 = 0
//                    }
//                    newMsg.what = MSG_CMD_100
//                    newMsg.obj = 0
//                    mHandler?.sendMessage(newMsg)
//                }
//            }catch (e:Exception){
//                var str=e.toString()
//            }
//
//        })
//
//    }
//



}