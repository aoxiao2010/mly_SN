package com.example.administrator.neuroelectricstimulator

import android.app.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.equipmentinformation.*
import kotlinx.android.synthetic.main.manualequipment.*
import java.util.zip.CheckedOutputStream
import android.util.Xml
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.childequipment.*
import kotlinx.android.synthetic.main.mine_main.*
import kotlinx.android.synthetic.main.treatment_fragment.*
import org.xmlpull.v1.XmlPullParser
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.treatposition.*
import java.util.zip.Inflater
import android.widget.BaseAdapter
import android.widget.*
import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.content.DialogInterface
import android.util.Log
import java.io.*


/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class ManualequipmentFragment: ListFragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var i:Int = 0
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private val REQUEST_ENABLE_BT =1

    private var mScanning = false
    var mHandler = Handler()
    private  val SCAN_PERIOD :Long = 5000;
    private var mProc: ProtocolProc? = ProtocolProc()
    private val MSG_CMD_3 = 13
    private val MSG_CMD_101=101
    private var boolConn:Boolean=false
    private var boolConn1:Boolean=false
    private var popupMenu: PopupMenu? = null
    private var progressDialog:ProgressDialog?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.manualequipment,container,false)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity

        //设置蓝牙扫描的回调函数
        mActivity!!.mble!!.setScanCallBack(mLeScanCallback)
        // Initializes list view adapter.
        mLeDeviceListAdapter = LeDeviceListAdapter(activity)
        setListAdapter(mLeDeviceListAdapter)

        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_CMD_101 ->{
                        var vl = msg.obj as Int
                        if (vl==1){
                            val presData = presValue(mActivity!!.cfID!!.toInt())
                            if (presData == null){
                                Toast.makeText(activity, R.string.loadPresError, Toast.LENGTH_SHORT).show()
                            }else{
                                if (boolConn==true){
                                    mProc?.send_cmd_3(mActivity!!.mble,presData, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                                }else if (boolConn1==true){
                                    mProc?.send_cmd_3(mActivity!!.mble,presData, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                                }
                            }
                        }else{
                            Toast.makeText(activity,R.string.dev_con_failed,Toast.LENGTH_SHORT).show()
                        }
                    }
                    MSG_CMD_3 -> {
                        val info3 = msg.obj as ProtocolProc.ExtraInfo
                        val id = String.format("%d", 0xFF and info3.PresID.toInt() )
                        val freq = String.format("%d",0xFF and info3.Freq.toInt())
                        val _new_freq = (0xFF and info3.newFreq[0].toInt()) + (0xFF and info3.newFreq[1].toInt()) * 256
                        val newFreq = String.format("%d", _new_freq)
                        val _pwm = (0xFF and info3.PWM[0].toInt()) + (0xFF and info3.PWM[1].toInt()) * 256
                        val pwm = String.format("%.2f", _pwm * 0.05)
                        val type = String.format("%d", 0xFF and info3.Type.toInt())
                        val _on = (0xFF and info3.OnTime[0].toInt()) + (0xFF and info3.OnTime[1].toInt()) * 256
                        val on = String.format("%.1f", _on * 0.1)
                        val _off = (0xFF and info3.OffTime[0].toInt()) + (0xFF and info3.OffTime[1].toInt()) * 256
                        val off = String.format("%.1f", _off * 0.1)
                        val overal = String.format("%d", 0xFF and info3.Overal.toInt())
                        val current3 = String.format("%.2f", (0xFF and info3.Current) * 0.05)

                        progressDialog!!.cancel()
                        val panel_info3 = (RString(R.string.presID) + id + "\n\n" +
                                RString(R.string.Freq) + newFreq + "(Hz)" + "\n\n" + RString(R.string.PWM) +
                                pwm + RString(R.string.miliSecond) + "\n\n" +
                                RString(R.string.pulseType) + type + "\n\n" + RString(R.string.onTime) +
                                on + RString(R.string.Second) + "\n\n" +
                                RString(R.string.offTime) + off + RString(R.string.Second) + "\n\n" +
                                RString(R.string.OveralTime) + overal + RString(R.string.minute)
                                + "\n\n")
                        AlertDialog.Builder(activity)
                                .setTitle(RString(R.string.PresReturnInfo))
                                .setMessage(panel_info3)
                                .setPositiveButton(RString(R.string.OK),  DialogInterface.OnClickListener { dialog, which ->
                                       if (xmlSave()){
                                            //mActivity?.changeFragment2(EquipmentFragment(),ManualequipmentFragment())
                                           mActivity!!.cfID=null
                                           mActivity!!.cfName=null
                                           mActivity!!.cfAcpoint=null
                                           mActivity?.changeFragment(EquipmentFragment())
                                       }else{
                                           Toast.makeText(activity,"设备添加错误！",Toast.LENGTH_SHORT).show()
                                       }
                                })
                                .show()
                    }
                }
            }
        }
        mProc!!.init_sys(mHandler,activity)

    }

    private fun RString(id: Int): String {
        return resources.getString(id)
    }

    // 扫描的结果
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        if (device.name!=null){
            var ad1 :String=device.name
            if (ad1=="MedKinetic Electroceuticals"  ){
                var bool:Boolean=false
                for (item in holder!!.iterator()){
                    if(item.address==device.address){
                        bool=true
                        break
                    }
                }
                if (bool==false){
                    mLeDeviceListAdapter!!.addDevice(BleModel(device.address,device.name))
                    setListViewHeight()
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    fun setListViewHeight() {
       var count:Int= mLeDeviceListAdapter!!.count
       var height=0
       var i: Int
       i = 0
       while (i < count){
           var temp=mLeDeviceListAdapter!!.getView(i,null,listView)
           temp.measure(0,0)
           height += temp.getMeasuredHeight()
           i++
       }
        var params:ViewGroup.LayoutParams=listView.layoutParams
        params.height =height+200
        listView.setLayoutParams(params)

    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long){
        val device = mLeDeviceListAdapter!!.getDevice(position) ?: return

        mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_NOTSELECT
        val address = mLeDeviceListAdapter!!.getDevice(position)!!.address
        val tv =rootView!!.findViewById<EditText>(R.id.MAC)
        tv.setText(address.toString())
        if (mScanning==true){
            scanLeDevice(false)        //停止蓝牙扫描
        }
        mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_SELECT
        val textView = v.findViewById<View>(R.id.status) as TextView
        textView.text = mLeDeviceListAdapter!!.mLeDevices[position].statusDescription
    }

    private inner class LeDeviceListAdapter(context: Context) : BaseAdapter() {
         val mLeDevices: ArrayList<BleModel>
         val mLeAddress: ArrayList<String>
         val mInflator: LayoutInflater

        init {
            mLeDevices = ArrayList()
            mLeAddress = ArrayList()
            mInflator = LayoutInflater.from(context)//MainActivity.this.getLayoutInflater();
        }

        fun getDevice(position: Int): BleModel? {
            return mLeDevices[position]
        }

        fun clear() {
            for (bleModel in mLeDevices) {
                if (bleModel.gatt != null) {
                    bleModel.gatt.disconnect()
                }
            }
            mLeDevices.clear()
            mLeAddress.clear()
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mLeDevices.size
        }

        override fun getView(i: Int, view: View?, parent: ViewGroup): View {
            var view = view

            val viewHolder: ViewHolder
            // General ListView optimization code.
            if (view == null) {
                //                view = mInflator.inflate(R.layout.activity_main, null);
                view = mInflator.inflate(R.layout.list, null)
                viewHolder = ViewHolder()
                viewHolder.nameTextView = view!!.findViewById<View>(R.id.name) as TextView
                viewHolder.addressTextView = view.findViewById<View>(R.id.mac) as TextView
                viewHolder.statusTextView = view.findViewById<View>(R.id.status) as TextView
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val bleModel = mLeDevices[i]
            viewHolder.nameTextView!!.text = bleModel.name
            viewHolder.addressTextView!!.text = bleModel.address
            viewHolder.statusTextView!!.text = bleModel.statusDescription
            return view
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        fun addDevice(device: BleModel) {
            if (!mLeAddress.contains(device.address)) {
                mLeDevices.add(device)
                mLeAddress.add(device.address)
            }
        }
    }

    internal class ViewHolder {
        var nameTextView: TextView? = null
        var addressTextView: TextView? = null
        var statusTextView: TextView? = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val tv =rootView!!.findViewById<EditText>(R.id.prescriptiondata)
        tv.setText(mActivity!!.cfName+" -- "+mActivity!!.cfAcpoint)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //增加蓝牙搜索菜单 2018.06.12 mly
        popupMenu = PopupMenu(activity,rootView?.findViewById(R.id.toolbar_manualequipment))
        popupMenu?.menuInflater?.inflate(R.menu.menu_bleresearch,popupMenu?.menu)
        //增加蓝牙搜索菜单 2018.06.12 mly
        toolbar_manualequipment.setNavigationOnClickListener{
            mActivity?.changeFragment(MainFragment())
        }
        manual_equipment_research.setOnClickListener {
            //判断本地蓝牙是否已打开 调用系统API去打开蓝牙
            if (!mActivity!!.mble!!.isOpened) {
                val openIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(openIntent, REQUEST_ENABLE_BT)
            }
            if (mScanning==false){
                scanLeDevice(true)        //开始蓝牙扫描
            }
            //popupMenu?.show()  //显示弹出菜单
        }
        //监听弹出菜单事件
        popupMenu?.setOnMenuItemClickListener {item ->
            when(item.itemId){
                R.id.researching->{

                }
            }
            false
        }

        MAC.setCursorVisible(false)
        MAC.setFocusable(false)
        MAC.setFocusableInTouchMode(false)

        prescriptiondata.setCursorVisible(false)
        prescriptiondata.setFocusable(false)
        prescriptiondata.setFocusableInTouchMode(false)
        prescriptiondata.setOnClickListener({
            mActivity!!.btnCFShow=true
            mActivity!!.changeFragment1(PrescriptionFragment(),ManualequipmentFragment())
        })

        return_back2.setOnClickListener {
            mActivity?.changeFragment(EquipmentFragment())
        }
        xmlRead()
        xmlPresRead()
        if (holder.size==0){
            boolConn=true
        }
        if (holder.size==1){
            boolConn1=true
        }
        submit.setOnClickListener {
              if (holder.size==2){
                  Toast.makeText(activity,"添加设备不能超过2台！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              if (number.text.toString()==""){
                  Toast.makeText(activity,"产品编号不能为空！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              if (MAC.text.toString()==""){
                  Toast.makeText(activity,"MAC地址不能为空！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              if (prescriptiondata.text.toString()=="" || mActivity!!.cfID==null){
                  Toast.makeText(activity,"处方信息不能为空！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              if (equipmentmemory.text.toString()==""){
                  Toast.makeText(activity,"内存容量不能为空！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              var bool:Boolean=false
              for (item in holder!!.iterator()){
                  if(item.address==MAC.text.toString()){
                      bool=true
                      break
                  }
              }
              if (bool==true){
                  Toast.makeText(activity,"此设备信息已经添加！",Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }

              progressDialog= ProgressDialog(activity)
              progressDialog!!.setMessage(RString(R.string.dev_connecting))
              progressDialog!!.show()
              if (boolConn==true){
                  if(mActivity!!.mStatus!=1 ){
                      mActivity!!.connNum=0
                      Conn(MAC.text.toString())
                  }
              }else if  (boolConn1==true){
                 if( mActivity!!.mStatus1!=1){
                     mActivity!!.connNum=1
                     Conn(MAC.text.toString())
                 }
              }
        }

    }

    private var holder = ArrayList<EquipmentHolder>()
    private fun xmlSave():Boolean{
        var tmp=EquipmentHolder()
        tmp.id=holder.size+1
        tmp.address=MAC.text.toString()
        tmp.cfid=mActivity!!.cfID!!.toInt()
        tmp.name=mActivity!!.cfName
        tmp.pres=mActivity!!.cfAcpoint
        tmp.capacity=equipmentmemory.text.toString()//"容量"
        tmp.prodCode=number.text.toString()//"产品编号"
        holder.add(tmp)
        if (xmlWrite()){
            return true
        }


        return false
    }

    private fun xmlWrite():Boolean{
        try{
            var file= File(activity.getFilesDir(),"equipment.xml")
            if (file.exists()){
                file.delete()
            }
            var fos = FileOutputStream(file);
            // 获得一个序列化工具
            val serializer = Xml.newSerializer()
            serializer.setOutput(fos, "utf-8")
            // 设置文件头
            serializer.startDocument("utf-8", true)
            serializer.startTag(null, "enames")
            var num:Int=0
            for (item in holder!!.iterator()){
                num=num+1
                serializer.startTag(null, "ename")
                serializer.attribute(null, "id",num.toString())

                serializer.startTag(null, "name")
                serializer.text(item.name)
                serializer.endTag(null, "name")

                serializer.startTag(null, "cfid")
                serializer.text(item.cfid.toString())
                serializer.endTag(null, "cfid")

                serializer.startTag(null, "address")
                serializer.text(item.address)
                serializer.endTag(null, "address")

                serializer.startTag(null, "pres")
                serializer.text(item.pres)
                serializer.endTag(null, "pres")

                serializer.startTag(null, "capacity")
                serializer.text(item.capacity)
                serializer.endTag(null, "capacity")

                serializer.startTag(null, "prodCode")
                serializer.text(item.prodCode)
                serializer.endTag(null, "prodCode")

                serializer.endTag(null, "ename")
            }
            serializer.endTag(null, "enames")
            serializer.endDocument()
            fos.flush()
            fos.close()
            return true
        }catch (e: IOException){
            return false
        }
        return false
    }
    private fun xmlRead():Boolean{
        try{
            var file= File(activity.getFilesDir(),"equipment.xml")
            var fis = FileInputStream(file);

            var parser=Xml.newPullParser()
            parser.setInput(fis, "utf-8")
            var eventType = parser.getEventType()
            var id:String?=null
            var name:String?=null
            var address:String?=null
            var pres:String?=null
            var cfId:String?=null
            var capacity:String?=null
            var prodCode:String?=null
            while (eventType != XmlPullParser.END_DOCUMENT){
                var tagName = parser.getName()
                when (eventType){
                    XmlPullParser.START_TAG ->{
                        if ("enames".equals(tagName)) {
                        } else if ("ename".equals(tagName)) {
                            id = parser.getAttributeValue(null, "id")
                        } else if ("name".equals(tagName)) {
                            name = parser.nextText()
                        }else if ("address".equals(tagName)) {
                            address = parser.nextText()
                        } else if ("pres".equals(tagName)) {
                            pres = parser.nextText()
                        }else if ("capacity".equals(tagName)) {
                            capacity = parser.nextText()
                        }else if ("prodCode".equals(tagName)) {
                            prodCode = parser.nextText()
                        }else if ("cfid".equals(tagName)) {
                            cfId = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG ->{
                        if (id!=null){
                            var tmp=EquipmentHolder()
                            tmp.id=Integer.parseInt(id)
                            tmp.name=name
                            tmp.address=address
                            tmp.pres=pres
                            tmp.cfid=Integer.parseInt(cfId)
                            tmp.capacity=capacity
                            tmp.prodCode=prodCode
                            holder.add(tmp)
                        }
                        id=null
                        name=null
                        address=null
                        pres=null
                        cfId=null
                        capacity=null
                        prodCode=null
                    }
                }
                eventType = parser.next()
            }
            return true
        }catch (e: IOException){
            return false
        }
        return true
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // SCAN_PERIOD 秒后停止扫描
            mHandler.postDelayed(cancelScan, SCAN_PERIOD)

            mScanning = true
            mActivity!!.mble!!.startLeScan()    //开始蓝牙扫描
        } else {
            //取消停止扫描的线程
            mHandler.removeCallbacks(cancelScan)
            mScanning = false
            mActivity!!.mble!!.stopLeScan()    //停止蓝牙扫描
        }
        //        invalidateOptionsMenu();

    }

    internal var cancelScan: Runnable = object : Runnable {
        override fun run() {
            mActivity!!.mble!!.stopLeScan()
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            mActivity!!.mble!!.startLeScan()
            mHandler.postDelayed(this, SCAN_PERIOD)
            //            invalidateOptionsMenu();
        }
    }

    private var presHolder = ArrayList<PrescriptionHolder>()
    //读取处方
    private fun xmlPresRead():Boolean{
        try{
            var file= File(activity.getFilesDir(),"prescripion.xml")
            var fis = FileInputStream(file);

            var parser= Xml.newPullParser()
            parser.setInput(fis, "utf-8")
            var eventType = parser.getEventType()
            var id:String?=null
            var name:String?=null
            var acpoint:String?=null
            var freq:String?=null
            var pulseWidth:String?=null
            var onTime:String?=null
            var offTime:String?=null
            var strength:String?=null
            var pulseDirection:String?=null
            var pointCode:String?=null
            while (eventType != XmlPullParser.END_DOCUMENT){
                var tagName = parser.getName()
                when (eventType){
                    XmlPullParser.START_TAG ->{
                        if ("users".equals(tagName)) {
                        } else if ("item".equals(tagName)) {
                            id = parser.getAttributeValue(null, "id")
                        } else if ("name".equals(tagName)) {
                            name = parser.nextText()
                        }else if ("acpoint".equals(tagName)) {
                            acpoint = parser.nextText()
                        } else if ("freq".equals(tagName)) {
                            freq = parser.nextText()
                        }else if ("pulseWidth".equals(tagName)) {
                            pulseWidth = parser.nextText()
                        }else if ("onTime".equals(tagName)) {
                            onTime = parser.nextText()
                        }else if ("offTime".equals(tagName)) {
                            offTime = parser.nextText()
                        }else if ("strength".equals(tagName)) {
                            strength = parser.nextText()
                        }else if ("pulseDirection".equals(tagName)) {
                            pulseDirection = parser.nextText()
                        }else if ("pointCode".equals(tagName)) {
                            pointCode = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG ->{
                        if (id!=null){
                            var tmp=PrescriptionHolder()
                            tmp.id=Integer.parseInt(id)
                            tmp.name=name
                            tmp.acpoint=acpoint
                            tmp.freq=freq
                            tmp.pulseWidth=pulseWidth
                            tmp.onTime=onTime
                            tmp.offTime=offTime
                            tmp.strength=strength
                            tmp.pulseDirection=pulseDirection
                            tmp.pointCode=pointCode
                            presHolder.add(tmp)
                        }
                        id=null
                        name=null
                        acpoint=null
                        freq=null
                        pulseWidth=null
                        onTime=null
                        offTime=null
                        strength=null
                        pulseDirection=null
                        pointCode=null
                    }
                }
                eventType = parser.next()
            }
            return true
        }catch (e: IOException){
            return false
        }
        return true
    }
    private  fun presValue(cf_id:Int): ByteArray?{
        for (item in presHolder!!.iterator()){
            if (cf_id==item.id){
                return getPresBytes(item)
                break
            }
        }
        return null
    }
    private fun getPresBytes(tmp:PrescriptionHolder): ByteArray? {
//        val position = fromIDtoPosition()
//        if (position == -1) {
//            return null
//        }
        val len = 40
        val result = ByteArray(len)
        for (i in 0 until len) {
            result[i] = 0
        }
        result[1] = 0x55
        result[0] = result[1]
        result[2] = 3


        //val tmp = data!!.get(position) tmp.ID
        result[3] = (0xff and tmp.id).toByte()//编号
        //result[4]=(byte)(0xff&tmp.Freq);
        val tmpFreq = int2bytes2(Integer.parseInt(tmp.freq))//脉冲频率
        result[4] = tmpFreq[0]
        result[5] = tmpFreq[1]
        //here from ms to us and unit is 50 us
        val pulseWidth = (Integer.parseInt(tmp.pulseWidth) * 20).toInt()//脉冲宽度
        val tmpWidth = int2bytes2(pulseWidth)
        result[6] = tmpWidth[0]
        result[7] = tmpWidth[1]
        result[8] = (0xff and Integer.parseInt(tmp.pulseDirection)).toByte()//刺激种类
        val onTime = (Integer.parseInt(tmp.onTime) * 2).toInt() //刺激时间
        val tmpOntime = int2bytes2(onTime)
        result[9] = tmpOntime[0]
        result[10] = tmpOntime[1]

        val offTime = (Integer.parseInt(tmp.offTime) * 3).toInt() //间歇时间
        val tmpOffTime = int2bytes2(offTime)
        result[11] = tmpOffTime[0]
        result[12] = tmpOffTime[1]

        result[13] = (0xff and 60).toByte() //刺激总时长
        //int current=(int) (20*tmp.current);
        //result[13]=(byte)(0xff&current);
        var sum = 0
        for (i in 2..len - 4) {
            sum += 0xFF and result[i].toInt()
        }
        sum %= 256


        result[len - 3] = sum.toByte()
        result[len - 2] = 0xAA.toByte()
        result[len - 1] = 0xAA.toByte()
        return result
    }

    private fun int2bytes2(from: Int): ByteArray {
        val result = ByteArray(2)
        result[0] = (0xff and from).toByte()
        result[1] = (0xff00 and from shr 8).toByte()
        return result
    }


    fun Conn(address: String){

        Handler().post(Runnable {
            try{
                var i: Int
                i = 0
                while (i < 5){

                    if (mActivity!!.connNum == 0) {
                        if (mActivity!!.mble!!.connectDevice(address, mActivity!!.bleCallBack))
                        //连接蓝牙设备
                            break
                    }
                    if (mActivity!!.connNum == 1) {
                        if (mActivity!!.mble!!.connectDevice1(address, mActivity!!.bleCallBack1))
                        //连接蓝牙设备
                            break
                    }

                    try {
                        Thread.sleep(500, 0)//200ms
                    } catch (e: Exception) {

                    }
                    i++
                }

                val newMsg = Message()
                Log.d("teST", "I VALUE IS :" + i)
                if (i == 5) {
                    //addLogText("",Color.RED,0);
                    // connecctionStatus(RString(R.string.dev_con_failed));
                    if (mActivity!!.connNum == 0) {
                        mActivity!!.mStatus = 0
                    }
                    if (mActivity!!.connNum == 1) {
                        mActivity!!.mStatus1 =0
                    }
                    newMsg.what = MSG_CMD_101
                    newMsg.obj = 0
                    mHandler?.sendMessage(newMsg)
                }
                try {
                    Thread.sleep(200, 0)//200ms
                } catch (e: Exception) {

                }

                if (mActivity!!.mble!!.wakeUpBle(address)) {
                    if (mActivity!!.connNum == 0 && mActivity!!.mble!!.mBluetoothDeviceAddress.size>0) {
                        mActivity!!.mStatus = 1
                    }
                    if (mActivity!!.connNum == 1 && mActivity!!.mble!!.mBluetoothDeviceAddress.size>1) {
                        mActivity!!.mStatus1 = 1
                    }
                    mActivity!!.connNum++
                    newMsg.what = MSG_CMD_101
                    newMsg.obj = 1
                    mHandler?.sendMessage(newMsg)
                } else {
                    if (mActivity!!.connNum == 0) {
                        mActivity!!.mStatus = 0
                    }
                    if (mActivity!!.connNum == 1) {
                        mActivity!!.mStatus1 = 0
                    }
                    newMsg.what = MSG_CMD_101
                    newMsg.obj = 0
                    mHandler?.sendMessage(newMsg)
                }
            }catch (e:Exception){
                var str=e.toString()
            }

        })

    }



}