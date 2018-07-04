package com.example.administrator.neuroelectricstimulator

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.app.ListFragment
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.net.sip.SipSession
import android.opengl.Visibility
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.ShowableListMenu
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.PopupMenu.OnMenuItemClickListener
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.childequipment.*
import kotlinx.android.synthetic.main.equipmentinformation.*
import kotlinx.android.synthetic.main.prescriptioninformation.*
import android.util.DisplayMetrics
import android.util.Xml
import android.view.ViewStub
import org.xmlpull.v1.XmlPullParser
import android.bluetooth.BluetoothAdapter
import android.os.Environment
import android.os.Handler
import android.os.Message
import kotlinx.android.synthetic.main.manualequipment.*
import kotlinx.android.synthetic.main.treatposition.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class EquipmentFragment: ListFragment(){
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var popupMenu: PopupMenu? = null
    var layout: LinearLayout? = null
    private var mListAdapter: LeListAdapter? = null
    private var progressDialog:ProgressDialog?=null
    var mHandler = Handler()
    private val MSG_CMD_4 = 14
    private val MSG_CMD_5 = 15
    private val MSG_CMD_100=100
    private var mProc: ProtocolProc? = ProtocolProc()

    private val pageLength = 32

    private var fileTmpPointer = 0
    private var total_pages: Int = 0
    private var pageCount = 0
    private var fileTmpBuffer =ByteArray(0)

    private var fileTmpPointer1 = 0
    private var total_pages1: Int = 0
    private var pageCount1 = 0
    private var fileTmpBuffer1=ByteArray(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.equipmentinformation,container,false)
        rootView?.isClickable = true
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_CMD_100 ->{
                        progressDialog!!.dismiss()
                        var vl = msg.obj as Int
                        if (vl==1){
                            if(mActivity!!.mStatus==1 && mListAdapter!!.count>0){
                                mListAdapter!!.getDevice(0)!!.conn=true
                            }
                            if(mActivity!!.mStatus1==1 && mListAdapter!!.count>1){
                                mListAdapter!!.getDevice(1)!!.conn=true
                            }
                            mListAdapter!!.notifyDataSetChanged();
                        }else{
                            Toast.makeText(activity,R.string.dev_con_failed,Toast.LENGTH_SHORT).show()
                        }
                    }
                    MSG_CMD_4 -> {
                        val pages = msg.obj as Int
                        if (pages==0){
                            Toast.makeText(activity,R.string.download0,Toast.LENGTH_SHORT).show()
                        }else{
                            val connNum4 = msg.arg1.toInt()

                            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                            progressDialog!!.setCancelable(true)
                            progressDialog!!.setMessage(RString(R.string.strDownload))
                            progressDialog!!.max =pages
                            progressDialog!!.setProgress(0)
                            progressDialog!!.show()

                            if (connNum4 == 0){
                                total_pages = pages
                                fileTmpBuffer =ByteArray(pages * pageLength)
                                for (i in 0 until pages * pageLength) {
                                    fileTmpBuffer[i] = 55
                                }
                                fileTmpPointer = 0
                                mProc!!.send_cmd_5(mActivity!!.mble, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                            }
                            if (connNum4 == 1){
                                total_pages1 = pages
                                fileTmpBuffer1 = ByteArray(pages * pageLength)
                                for (i in 0 until pages * pageLength) {
                                    fileTmpBuffer1[i] = 55
                                }
                                fileTmpPointer1 = 0
                                mProc!!.send_cmd_5(mActivity!!.mble, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                            }
                        }

                    }
                    MSG_CMD_5 -> {
                        val connNum5 = msg.arg1.toInt()
                        if (connNum5 == 0){
                            pageCount++
                            val tmp = msg.obj as ByteArray
                            appendBuffer(tmp)
                            progressDialog!!.incrementProgressBy(pageCount)
                            updateDownloadStatus(total_pages, pageCount, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                            if (pageCount < total_pages)
                                mProc!!.send_cmd_5(mActivity!!.mble, pageCount, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                            else {
                                pageCount = 0
                                mProc!!.send_cmd_6(mActivity!!.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                            }
                        }
                        if (connNum5 == 1){
                            pageCount1++
                            val tmp = msg.obj as ByteArray
                            appendBuffer1(tmp)
                            progressDialog!!.incrementProgressBy(pageCount1)
                            updateDownloadStatus1(total_pages1, pageCount1, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                            if (pageCount1 < total_pages1)
                                mProc!!.send_cmd_5(mActivity!!.mble, pageCount1, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                            else {
                                pageCount1 = 0
                                mProc!!.send_cmd_6(mActivity!!.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                            }
                        }
                    }
                }
            }
        }
        mProc!!.init_sys(mHandler,activity)

    }

    private fun appendBuffer(tmp: ByteArray) {
        if (fileTmpPointer + tmp.size > fileTmpBuffer!!.size)
            return
        for (i in fileTmpPointer until fileTmpPointer + tmp.size) {
            fileTmpBuffer[i] = tmp[i - fileTmpPointer]
        }
        fileTmpPointer += tmp.size
    }
    private fun appendBuffer1(tmp: ByteArray) {
        if (fileTmpPointer1 + tmp.size > fileTmpBuffer1!!.size)
            return
        for (i in fileTmpPointer1 until fileTmpPointer1 + tmp.size) {
            fileTmpBuffer1[i] = tmp[i - fileTmpPointer1]
        }
        fileTmpPointer1 += tmp.size
    }
    private fun updateDownloadStatus(pages: Int, currentPage: Int, address: String) {

        if (currentPage == pages) {
            writeToFile(fileTmpBuffer, address)
            fileTmpPointer = 0
        }
    }
    private fun updateDownloadStatus1(pages: Int, currentPage: Int, address: String) {
        if (currentPage == pages) {
            writeToFile(fileTmpBuffer1, address)
            fileTmpPointer1 = 0
        }
    }
    private fun writeToFile(fileBuffer: ByteArray?, address: String) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Toast.makeText(activity, "SD Card Error Detected!!", Toast.LENGTH_SHORT).show()
            return
        }
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmm")

        val fileName = simpleDateFormat.format(calendar.time) + "-" + address
        val file = File(Environment.getExternalStorageDirectory(), fileName)
        try {
            val os = FileOutputStream(file)
            os.write(fileBuffer!!)
            os.flush()
            os.close()
            Toast.makeText(activity, "Log Saved", Toast.LENGTH_SHORT).show()
            progressDialog!!.dismiss()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        popupMenu = PopupMenu(activity,rootView?.findViewById(R.id.add_equipment))
        popupMenu?.menuInflater?.inflate(R.menu.menu2,popupMenu?.menu)

        //获取屏幕的宽度和高度
        val dm = resources.displayMetrics
        val w_screen = dm.widthPixels
        val h_screen = dm.heightPixels

        mActivity = activity as MainActivity
        mListAdapter=LeListAdapter(activity)
        xmlRead(0)
        if(mActivity!!.mStatus==1 && mListAdapter!!.count>0){
            mListAdapter!!.getDevice(0)!!.conn=true
        }
        if(mActivity!!.mStatus1==1 && mListAdapter!!.count>1){
            mListAdapter!!.getDevice(1)!!.conn=true
        }

        setListAdapter(mListAdapter)

//        if (mActivity!!.btnShow==true){
//            listView.descendantFocusability
//        }

        if (mActivity!!.btnShow==true){
            submit2.visibility=View.VISIBLE
        }
        submit2.setOnClickListener({
            mActivity?.changeFragment(TreatpositionFragment())
        })

        toolbar_equipment.setNavigationOnClickListener{
            mActivity?.changeFragment(MainFragment())
        }

        add_equipment.setOnClickListener {
            popupMenu?.show()  //显示弹出菜单
        }

        //监听弹出菜单事件
        val integrator = IntentIntegrator(activity)
        popupMenu?.setOnMenuItemClickListener {item ->
            when(item.itemId){
                R.id.manual ->{
                    if (mActivity!!.btnShow==true){
                        Toast.makeText(activity, R.string.isAddEquip, Toast.LENGTH_LONG).show()
                    }else{
                        mActivity?.changeFragment1(ManualequipmentFragment(),EquipmentFragment())
                    }
                }
                R.id.flicking -> {
                    integrator.initiateScan()
                }
            }
            false
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            val result = scanResult.contents
            Log.e("HYN", result)
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show()
        }
    }

    //添加fragment
    fun addFragment(fragment: Fragment){
//        val fragmentManager = childFragmentManager
//        val transaction = fragmentManager.beginTransaction();
//        transaction.add(R.id.parent_equipment,fragment).commit()
//        //transaction.show(fragment)
//        transaction.commitAllowingStateLoss()
////        fragmentManager.executePendingTransactions()

        Toast.makeText(activity,"hhahh",Toast.LENGTH_SHORT).show()
    }

    private fun xmlRead(type:Int):Boolean{
        try{
            var file= File(activity.getFilesDir(),"equipment.xml")
            var fis = FileInputStream(file);

            var parser= Xml.newPullParser()
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
                            if (type==0){
                                mListAdapter!!.addValue(EquipmentHolder(Integer.parseInt(id),name,address,pres,capacity,prodCode,Integer.parseInt(cfId),false))
                            }else{
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
    private var holder = ArrayList<EquipmentHolder>()
    private fun xmlWrite(position: Int):Boolean{
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
            holder.removeAt(position)
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
    private fun RString(id: Int): String {
        return resources.getString(id)
    }


    override fun onDestroy() {
        mActivity!!.btnShow=false
        super.onDestroy()
    }

    private inner class LeListAdapter(context: Context) : BaseAdapter() {
         val mLeDevices: ArrayList<EquipmentHolder>
         val mInflator: LayoutInflater
         var isCheck:Boolean=false
         var mCurrentItem:Int=0
        init {
            mLeDevices = ArrayList()
            mInflator = LayoutInflater.from(context)//MainActivity.this.getLayoutInflater();
        }

        fun getDevice(position: Int): EquipmentHolder? {
            return mLeDevices[position]
        }

        fun clear() {
            mLeDevices.clear()
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
                view = mInflator.inflate(R.layout.childequipment, null)
                viewHolder = ViewHolder()
                viewHolder.cfIDTextView = view!!.findViewById<View>(R.id.txtcf_ID) as TextView
                viewHolder.cfBWTextView = view!!.findViewById<View>(R.id.txtcf_BW) as TextView
                viewHolder.cfNameTextView = view.findViewById<View>(R.id.txtcfName) as TextView
                viewHolder.addressTextView = view.findViewById<View>(R.id.txtMAC) as TextView
                viewHolder.statusTextView = view.findViewById<View>(R.id.txtStaus) as TextView
                viewHolder.capacityTextView = view.findViewById<View>(R.id.txt_Capacity) as TextView

                viewHolder.delBtn=view.findViewById<View>(R.id.btnDelete) as TextView
                viewHolder.downBtn=view.findViewById<View>(R.id.btnDownload) as TextView
                viewHolder.imageBtn=view.findViewById<View>(R.id.imageBtn) as ImageView

                viewHolder.childLayout=view.findViewById<View>(R.id.child_equipmentLayout) as LinearLayout
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }


            val equip = mLeDevices[i]
            viewHolder.cfIDTextView!!.text = equip.cfid.toString()
            viewHolder.addressTextView!!.text = equip.address
            viewHolder.cfBWTextView!!.text = equip.pres
            viewHolder.cfNameTextView!!.text = equip.name
            viewHolder.capacityTextView!!.text =equip.capacity
            if (equip.conn==true){
                viewHolder.imageBtn!!.setImageResource(R.drawable.es01)
                viewHolder.statusTextView!!.text=RString(R.string.strStatusText)
            }else{
                viewHolder.imageBtn!!.setImageResource(R.drawable.es02)
                viewHolder.statusTextView!!.text=RString(R.string.dev_connNO)
            }
            if (mActivity!!.btnShow==true) {
                if (mCurrentItem==i && isCheck){
                    viewHolder.childLayout!!.setBackgroundResource(R.color.lightGrey)
                }else{
                    viewHolder.childLayout!!.setBackgroundResource(R.color.white)
                }
                viewHolder.imageBtn!!.setOnClickListener({
                    mActivity!!.equipId=equip.id
                    setClick(true)
                    setCurrentItem(i)
                    notifyDataSetChanged()
                })
            }else{
                viewHolder.delBtn!!.setOnClickListener({
                    val address = equip.address
                    var num:Int=0
                    var bool:Boolean=false
                    var j : Int=0
                    while (num<mActivity!!.mble!!.mBluetoothDeviceAddress.size){
                        if (mActivity!!.mble!!.mBluetoothDeviceAddress.get(num)==address){
                            bool=true
                            j=num
                        }
                        num++
                    }
                    val panel_info3 =RString(R.string.strEquipDel) +address+RString(R.string.strEquipDel1)
                    AlertDialog.Builder(activity)
                            .setTitle(RString(R.string.delEquip))
                            .setMessage(panel_info3)
                            .setPositiveButton(RString(R.string.OK),  DialogInterface.OnClickListener { dialog, which ->
                                holder.clear()
                               if (xmlRead(1)){
                                   if (xmlWrite(i)){
                                       mListAdapter!!.delValue(i)
                                       notifyDataSetChanged()
                                       Toast.makeText(activity,R.string.strEquipDelOK,Toast.LENGTH_SHORT).show()
                                       if (bool==true){
                                           mActivity!!.mble!!.disconnectDevice1(mActivity!!.mble!!.connectionQueue.get(j),j)
                                           mActivity!!.mble!!.disconnectLocalDevice(mActivity!!.mble!!.connectionQueue.get(j))
                                           if(j==0){
                                               mActivity!!.mStatus=0
                                           }else if (j==1){
                                               mActivity!!.mStatus1=0
                                           }

//                                           mActivity!!.mble!!.connectionQueue.removeAt(j)
//                                           mActivity!!.mble!!.mBluetoothDeviceAddress.removeAt(j)
//                                           if (mActivity!!.mble!!.connectionQueue.size>=1){
//                                               mActivity!!.mStatus1=0
//                                           }else if (mActivity!!.mble!!.connectionQueue.size>=0){
//                                               mActivity!!.mStatus=0
//                                           }
                                       }
                                   }else{
                                       Toast.makeText(activity,R.string.strEquipDelFail,Toast.LENGTH_SHORT).show()
                                   }
                               }else{
                                   Toast.makeText(activity,R.string.strEquipDelFail,Toast.LENGTH_SHORT).show()
                               }

                            })
                            .show()
                })

                viewHolder.downBtn!!.setOnClickListener({
                    if (mActivity!!.StimuStatus==2 && i==0){
                        Toast.makeText(activity,R.string.strequipCon2,Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (mActivity!!.StimuStatus1==2 && i==1){
                        Toast.makeText(activity,R.string.strequipCon2,Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    var address:String=""
                    if (mActivity?.mStatus==1 && i==0 && mActivity!!.mble != null){
                        if (mActivity!!.mble!!.mBluetoothDeviceAddress.size>0){
                            address= mActivity!!.mble!!.mBluetoothDeviceAddress[0]
                        }
                    }
                    if (mActivity?.mStatus1==1 && i==1 && mActivity!!.mble != null){
                        if (mActivity!!.mble!!.mBluetoothDeviceAddress.size>1){
                            address= mActivity!!.mble!!.mBluetoothDeviceAddress[1]
                        }
                    }
                    progressDialog= ProgressDialog(mInflator.context)
                    if (address!=""){
                        mProc!!.send_cmd_4(mActivity!!.mble, address)
                    }else{
                        Toast.makeText(activity,R.string.strequipCon3,Toast.LENGTH_SHORT).show()
                    }

                })

                viewHolder.imageBtn!!.setOnClickListener({
                    if (!mActivity!!.mble!!.isOpened){
                        Toast.makeText(activity,R.string.strBlue,Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (mActivity?.mStatus==1 && i==0){
                        Toast.makeText(activity,R.string.dev_connected,Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (mActivity?.mStatus1==1 && i==1){
                        Toast.makeText(activity,R.string.dev_connected,Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    progressDialog= ProgressDialog(mInflator.context)
                    progressDialog!!.setMessage(RString(R.string.dev_connecting))
                    progressDialog!!.show()

                    if (mActivity?.mStatus!=1 && i==0){
                        mActivity!!.connNum=0
                        Conn(viewHolder.addressTextView!!.text.toString(),1)
                    }else if (mActivity?.mStatus1!=1 && i==1){
                        mActivity!!.connNum=1
                        Conn(viewHolder.addressTextView!!.text.toString(),1)

                    }else{
                        Toast.makeText(activity,R.string.strequipCon1,Toast.LENGTH_SHORT).show()
                    }

                })
            }
            return view
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        fun setClick(click: Boolean) {
           this.isCheck = click
        }
        fun setCurrentItem(currentItem: Int) {
            this.mCurrentItem = currentItem
        }

        fun addValue(equip: EquipmentHolder) {
            mLeDevices.add(equip)
        }
        fun delValue(position: Int) {
            mLeDevices.removeAt(position)
        }
    }

        internal class ViewHolder {
        var cfIDTextView: TextView? = null
        var cfBWTextView: TextView? = null
        var cfNameTextView: TextView? = null
        var addressTextView: TextView? = null
        var statusTextView: TextView? = null
        var capacityTextView:TextView?=null
        var imageBtn:ImageView?=null
        var delBtn: TextView? = null
        var downBtn: TextView? = null
        var childLayout:LinearLayout?=null
    }




    fun Conn(address: String,type:Int){

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
                    if (type==1){
                        newMsg.what = MSG_CMD_100
                        newMsg.obj = 0
                        mHandler?.sendMessage(newMsg)
                    }
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
                    if (type==1){
                        mProc!!.send_cmd_0(mActivity!!.mble, address)
                    }
                    mActivity!!.connNum++
                    if (type==1){
                        newMsg.what = MSG_CMD_100
                        newMsg.obj = 1
                        mHandler?.sendMessage(newMsg)
                    }
                } else {
                    if (mActivity!!.connNum == 0) {
                        mActivity!!.mStatus = 0
                    }
                    if (mActivity!!.connNum == 1) {
                        mActivity!!.mStatus1 = 0
                    }
                    if (type==1){
                        newMsg.what = MSG_CMD_100
                        newMsg.obj = 0
                        mHandler?.sendMessage(newMsg)
                    }
                }
            }catch (e:Exception){
                var str=e.toString()
            }

        })

    }





}