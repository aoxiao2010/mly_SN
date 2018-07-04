package com.example.administrator.neuroelectricstimulator

import android.app.Application
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.firstfragment.*
import java.text.DecimalFormat
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.util.Xml
import android.content.Intent
import android.widget.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-06-25
 */

class FirstFragment: Fragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    private var x: Int = 0
    private var y: Int = 0
    private var click_left: Float = 0f
    private var click_top: Float = 0f
    private var click_right: Float = 0f
    private var click_bottom: Float = 0f

    private var standard_left:Int = 0
    private var standard_top:Int = 0
    private var standard_right:Int = 0
    private var standard_bottom:Int = 0

    private var standard_x: Float = 0f
    private var standard_y: Float = 0f
    private var standard_r: Float = 0f
    private var click_x: Float = 0f
    private var click_y: Float = 0f

    private var pointPosition: String = ""
    private var bitmap1: Bitmap? = null

    var time = "01:00"
    var electric = "2.0"
    private var count: Int = 0  //控制开关状态  0关闭  2开启
    private var isBluetooth: Boolean = false  //蓝牙连接

    private var strBW:String?=""
    private var strAddress:String?=""
    private var vPnum:Int?=0
    private var progressDialog: ProgressDialog?=null
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
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private var lv: ListView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.firstfragment,container,false)
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
                        if (vl==1 || vl==2){
                        }
                        change_electric(view!!)
                    }
                    MSG_CMD_0 -> {
                        dealMsg0(msg)
                        change_electric(view!!)
                    }
                    MSG_CMD_1 ->{
                        val info3 = msg.obj as ProtocolProc.ExtraInfo
                        val newCurrent = info3.Current
                        val cha = info3.channelType
                        val connNum = info3.ConnNum
                        if (connNum == 0) {
                            if (cha.toInt() == 0) {
                                electric_index = newCurrent
                                if (electric_index > electric_max)
                                    electric_index = electric_max
                                if (electric_index < 1)
                                    electric_index = 1
                                change_electric(view!!)
                            }
                        }
                        if (connNum == 1) {
                            if (cha.toInt() == 0) {
                                electric_index3 = newCurrent
                                if (electric_index3 > electric_max)
                                    electric_index3 = electric_max
                                if (electric_index3 < 1)
                                    electric_index3 = 1
                                change_electric(view!!)
                            }
                        }
                    }
                    MSG_CMD_7 ->{
                        val connNum7 = (msg.obj as ProtocolProc.ExtraInfo).ConnNum
                        if (connNum7 == 0) {
                           mActivity!!.StimuStatus = (msg.obj as ProtocolProc.ExtraInfo).StimuStatus.toInt()
                            change_electric(view!!)
                        }
                        if (connNum7 == 1) {
                            mActivity!!.StimuStatus1 = (msg.obj as ProtocolProc.ExtraInfo).StimuStatus.toInt()
                            change_electric(view!!)
                        }
                    }
                }
            }
        }
        mProc!!.init_sys(mHandler,activity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        var vID:Int=if (arguments != null) arguments!!.getInt("vId") else 0
        vPnum=if (arguments != null) arguments!!.getInt("vPosition") else 0
        if(vID!=0){
            xmlRead(vID)
        }
        val textView =rootView!!.findViewById<TextView>(R.id.txtFirstName) as TextView
        textView.setText(strBW )

        //设置蓝牙扫描的回调函数
        mActivity!!.mble!!.setScanCallBack(mLeScanCallback)
        mLeDeviceListAdapter = LeDeviceListAdapter()
        lv =rootView!!.findViewById<ListView>(R.id.fist_list)
        lv!!.setAdapter(mLeDeviceListAdapter)
        lv!!.setOnItemClickListener(SetListView())

        if (mActivity?.mStatus==1){
            if (mLeDeviceListAdapter!!.mLeDevices.size>0){
                mLeDeviceListAdapter!!.mLeDevices[0].status=BleModel.BLE_CONNETED
                mLeDeviceListAdapter!!.notifyDataSetChanged()
            }
            if (mActivity!!.mble != null) {
                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
                    mProc!!.send_cmd_0(mActivity?.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                }
            }
        }
        if (mActivity?.mStatus1==1){
            if (mLeDeviceListAdapter!!.mLeDevices.size>0){
                mLeDeviceListAdapter!!.mLeDevices[0].status=BleModel.BLE_CONNETED
                mLeDeviceListAdapter!!.notifyDataSetChanged()
            }
            if (mActivity!!.mble != null) {
                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
                    mProc!!.send_cmd_0(mActivity?.mble, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                }
            }
        }


        image_equipment1.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                val clickX = event.x
                val clickY = event.y
                val instance = Math.sqrt((clickX-click_x)*(clickX-click_x)+(clickY-click_y)*(clickY-click_y).toDouble())

                var df = DecimalFormat("###0.0")
                if(clickX>click_left && clickX<click_right && clickY>click_top && clickY<click_bottom)
                {
                    pointPosition = "left"
                    if (vPnum==0){
                        if (mActivity!!.mStatus==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.StimuStatus != 2){
                                    Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
                                }else{
                                    if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
                                        mProc!!.send_cmd_1(mActivity!!.mble, electric_index, false, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }else if (vPnum==1){
                        if (mActivity!!.mStatus1==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.StimuStatus1 != 2){
                                    Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
                                }else{
                                    if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
                                        mProc!!.send_cmd_1(mActivity!!.mble, electric_index3, false, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                    //electric = df.format(electric.toFloat() - 0.1).toString()
                    //change_electric(view!!)
                }else if(clickX>380*x/548 && clickX<470*x/548 && clickY>280*y/705 && clickY<350*y/705)
                {
                    pointPosition = "right"
                    if (vPnum==0){
                        if (mActivity!!.mStatus==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.StimuStatus != 2){
                                    Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
                                }else{
                                    if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
                                        mProc!!.send_cmd_1(mActivity!!.mble, electric_index, true, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }else if (vPnum==1){
                        if (mActivity!!.mStatus1==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.StimuStatus1 != 2){
                                    Toast.makeText(activity,  R.string.currentModFailed1, Toast.LENGTH_SHORT).show()
                                }else{
                                    if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
                                        mProc!!.send_cmd_1(mActivity!!.mble, electric_index3, true, 0, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                    //electric = df.format(electric.toFloat() + 0.1).toString()
                    //change_electric(view!!)
                }else if(clickX>400 && clickX<460 && clickY>200 && clickY<260){
                    Toast.makeText(activity,"蓝牙正在连接",Toast.LENGTH_SHORT).show()
                    isBluetooth = true
                    change_electric(view!!)
                }else if(instance>=0 && instance <= standard_r){
                    pointPosition = "down"
                    //change_electric(view!!)
                    if (vPnum==0){
                        if (mActivity!!.mStatus==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 0) {
                                    if (count==0){
                                        count=2
                                    }else{
                                        count=0
                                    }
                                    mProc!!.send_cmd_7(mActivity!!.mble, count, mActivity!!.mble!!.mBluetoothDeviceAddress[0])
                                }
                            }
                        }else{
                            Toast.makeText(activity, R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }else if (vPnum==1){
                        if (mActivity!!.mStatus1==1){
                            if (mActivity!!.mble != null) {
                                if (mActivity!!.mble!!.mBluetoothDeviceAddress.size > 1) {
                                    if (count==0){
                                        count=2
                                    }else{
                                        count=0
                                    }
                                    mProc!!.send_cmd_7(mActivity!!.mble, count, mActivity!!.mble!!.mBluetoothDeviceAddress[1])
                                }
                            }
                        }else{
                            Toast.makeText(activity,  R.string.dev_con_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }

//            if(event.action == MotionEvent.ACTION_UP){
//                pointPosition = "usual"
//                change_electric(view!!)
//            }
            true
        }
    }

    fun firstView(){
        bitmap1 = BitmapFactory.decodeResource(resources,
                R.mipmap.es_unlink).copy(Bitmap.Config.ARGB_8888, true)

//        val bitmap2 = (resources.getDrawable(
//                R.drawable.dot) as BitmapDrawable).bitmap

        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(bitmap1)
        val canvas = Canvas(newBitmap)
        var paint = Paint()

        val img_width= image_equipment1.width
        val img_height = image_equipment1.height

        x= img_width
        y = img_height

        paint = Paint()
        paint.color = Color.RED


        val bitmap_clock = BitmapFactory.decodeResource(resources,
                R.mipmap.clock).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_clock,100f,120f,paint)

        var bitmap_switch: Bitmap? = null
        if(count  == 0){
            bitmap_switch = BitmapFactory.decodeResource(resources,
                    R.mipmap.switch_off).copy(Bitmap.Config.ARGB_8888, true)
        }
        else
            bitmap_switch = BitmapFactory.decodeResource(resources,
                    R.mipmap.switch_open).copy(Bitmap.Config.ARGB_8888, true)

        canvas.drawBitmap(bitmap_switch,100f,200f,paint)

        val bitmap_electricty = BitmapFactory.decodeResource(resources,
                R.mipmap.batteryfull).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_electricty,400f,120f,paint)

        val bitmap_bluetooth = BitmapFactory.decodeResource(resources,
                R.mipmap.bluetooth_disabled).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_bluetooth,400f,200f,paint)

        paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 30f
        paint.color = Color.WHITE
        canvas.drawText(time,160f,150f,paint)

        var electric = "2.0"
        paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 50f
        paint.color = Color.WHITE
        canvas.drawText(electric,250f,200f,paint)

        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        image_equipment1.setImageBitmap(newBitmap)
    }

    fun change_electric(v: View) {
        // 防止出现Immutable bitmap passed to Canvas constructor错误
//        bitmap1 = BitmapFactory.decodeResource(resources,
//                R.mipmap.es_link).copy(Bitmap.Config.ARGB_8888, true)

        if(pointPosition == "left")
            bitmap1 = BitmapFactory.decodeResource(resources,
                    R.mipmap.es_left).copy(Bitmap.Config.ARGB_8888, true)
        else if(pointPosition == "right")
            bitmap1 = BitmapFactory.decodeResource(resources,
                    R.mipmap.es_right).copy(Bitmap.Config.ARGB_8888, true)
        else if(pointPosition == "down")
            bitmap1 = BitmapFactory.decodeResource(resources,
                    R.mipmap.es_down).copy(Bitmap.Config.ARGB_8888, true)
//        else if(pointPosition == "usual")
//            bitmap1 = BitmapFactory.decodeResource(resources,
//                    R.mipmap.es_link).copy(Bitmap.Config.ARGB_8888, true)
        if (mActivity?.mStatus1==1 || mActivity?.mStatus==1){
            bitmap1 = BitmapFactory.decodeResource(resources,
            R.mipmap.es_link).copy(Bitmap.Config.ARGB_8888, true)
           if (mLeDeviceListAdapter!!.mLeDevices.size>0){
               mLeDeviceListAdapter!!.mLeDevices[0].status=BleModel.BLE_CONNETED
               mLeDeviceListAdapter!!.notifyDataSetChanged()
           }
        }else{
            bitmap1 = BitmapFactory.decodeResource(resources,
            R.mipmap.es_unlink).copy(Bitmap.Config.ARGB_8888, true)
        }


        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(bitmap1)
        val canvas = Canvas(newBitmap)
        var paint = Paint()
        val w = bitmap1!!.width
        val h = bitmap1!!.height

        val img_width= image_equipment1.width
        val img_height = image_equipment1.height

        x= img_width
        y = img_height

        paint = Paint()
        paint.color = Color.RED


        val bitmap_clock = BitmapFactory.decodeResource(resources,
                R.mipmap.clock).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_clock,100f,120f,paint)

        //修改开关
        var bitmap_switch: Bitmap? = null
        if (vPnum==0){
            if(mActivity!!.StimuStatus == 2){
                count=2
                bitmap_switch = BitmapFactory.decodeResource(resources,
                        R.mipmap.switch_open).copy(Bitmap.Config.ARGB_8888, true)
            }
            else{
                count=0
                bitmap_switch = BitmapFactory.decodeResource(resources,
                        R.mipmap.switch_off).copy(Bitmap.Config.ARGB_8888, true)
            }
        }else if (vPnum==1){
            if(mActivity!!.StimuStatus1 == 2){
                count=2
                bitmap_switch = BitmapFactory.decodeResource(resources,
                        R.mipmap.switch_open).copy(Bitmap.Config.ARGB_8888, true)
            }
            else{
                count=0
                bitmap_switch = BitmapFactory.decodeResource(resources,
                        R.mipmap.switch_off).copy(Bitmap.Config.ARGB_8888, true)
            }
        }else{
            bitmap_switch = BitmapFactory.decodeResource(resources,
                    R.mipmap.switch_off).copy(Bitmap.Config.ARGB_8888, true)
        }
        canvas.drawBitmap(bitmap_switch,100f,200f,paint)


        val bitmap_electricty = BitmapFactory.decodeResource(resources,
                R.mipmap.batteryfull).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_electricty,400f,120f,paint)

        var bitmap_bluetooth: Bitmap? = null
        if(isBluetooth){
            bitmap_bluetooth = BitmapFactory.decodeResource(resources,
                    R.mipmap.bluetooth_connected).copy(Bitmap.Config.ARGB_8888, true)
        }
        else
            bitmap_bluetooth = BitmapFactory.decodeResource(resources,
                    R.mipmap.bluetooth_disabled).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_bluetooth,400f,200f,paint)


        paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 30f
        paint.color = Color.WHITE
        canvas.drawText(time,160f,150f,paint)

        //修改电流
        paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 50f
        paint.color = Color.WHITE
        if (vPnum==0){
            canvas.drawText(updateCurrent(electric_index).toString(),250f,200f,paint)
        }else if (vPnum==1){
            canvas.drawText(updateCurrent(electric_index3).toString(),250f,200f,paint)
        }else{
            canvas.drawText(20.toString(),250f,200f,paint)
        }


        rectangleProportions(w,h,80,280,170,350)
        clickRectangle(img_width,img_height)
        rectangleProportions(w,h,380,280,470,350)

        circleProportions(w,h,280f,430f,30f)
        clickCircle(img_width,img_height,280f,430f,30f)


        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        image_equipment1.setImageBitmap(newBitmap)
    }

    fun rectangleProportions(width: Int,height: Int,left: Int,top: Int,right: Int,bottom: Int){
        standard_left = left*width/548
        standard_top = top*height/705
        standard_right = right*width/548
        standard_bottom = bottom*height/705
    }

    fun clickRectangle(x:Int,y:Int){
        click_left = 80f*x/548
        click_top = 280f*y/705
        click_right = 170f*x/548
        click_bottom = 350f*y/705
    }

    fun circleProportions(width: Int,height: Int,cx: Float,cy: Float,r: Float){
        standard_x = cx*width/548
        standard_y = cy*height/705
        standard_r = r
    }

    fun clickCircle(width: Int,height: Int,cx: Float,cy: Float,r: Float){
        click_x = cx*width/548
        click_y = cy*height/705
        standard_r = r
    }


    private fun xmlRead(equipId:Int){

        try{
            var file= File(activity!!.getFilesDir(),"equipment.xml")
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
                            if (Integer.parseInt(id)==equipId){
                                strAddress=address
                                strBW=pres
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
        }catch (e: IOException){

        }

    }

    private fun RString(id: Int): String {
        return resources.getString(id)
    }


    private fun dealMsg0(msg: Message) {
        val info = msg.obj as ProtocolProc.ExtraInfo

        val connNum = info.ConnNum
        val channel = info.channelType

        if (connNum == 0) {
            mActivity!!.StimuStatus =info.StimuStatus .toInt()
            electric_index = info.Current
        } else {
            mActivity!!.StimuStatus1 =info.StimuStatus .toInt()
            electric_index3 = info.Current
        }
    }
    private fun updateCurrent(steps: Int):String {
        var steps = steps
        if (steps < 1) steps = 1
        if (steps > electric_max) steps = electric_max
        val display_value = steps * electric_multiplier
        return String.format("%.2f", display_value)
    }



    fun Conn(address: String,position:Int){

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
                        progressDialog!!.dismiss()
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
                    newMsg.what = MSG_CMD_100
                    newMsg.obj = 0
                    mHandler?.sendMessage(newMsg)
                    mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNETED_FAILED
                }
                try {
                    Thread.sleep(200, 0)//200ms
                    progressDialog!!.dismiss()
                } catch (e: Exception) {

                }

                if (mActivity!!.mble!!.wakeUpBle(address)) {
                    newMsg.what = MSG_CMD_100
                    if (mActivity!!.connNum == 0) {
                        newMsg.obj = 1
                        mActivity!!.mStatus = 1
                    }
                    if (mActivity!!.connNum == 1) {
                        newMsg.obj = 2
                        mActivity!!.mStatus1 = 1
                    }
                    mProc!!.send_cmd_0(mActivity!!.mble, address)
                    mActivity!!.connNum++
                    mHandler?.sendMessage(newMsg)
                    mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNETED
                } else {
                    if (mActivity!!.connNum == 0) {
                        mActivity!!.mStatus = 0
                    }
                    if (mActivity!!.connNum == 1) {
                        mActivity!!.mStatus1 = 0
                    }
                    newMsg.what = MSG_CMD_100
                    newMsg.obj = 0
                    mHandler?.sendMessage(newMsg)
                    mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNETED_FAILED
                }
            }catch (e:Exception){
                var str=e.toString()
            }

        })

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

    // 扫描的结果
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        if (device.name!=null){
            var ad1 :String=device.name
            if (ad1=="MedKinetic Electroceuticals" && device.address==strAddress ){
                mLeDeviceListAdapter!!.addDevice(BleModel(device.address,device.name))
                setListViewHeight()
                mLeDeviceListAdapter!!.notifyDataSetChanged()
            }
        }

    }

    fun setListViewHeight() {
        var count:Int= mLeDeviceListAdapter!!.count
        var height=0
        var i: Int
        i = 0
        while (i < count){
            var temp=mLeDeviceListAdapter!!.getView(i,null,lv as ListView)
            temp.measure(0,0)
            height += temp.getMeasuredHeight()
            i++
        }
        var params:ViewGroup.LayoutParams=lv!!.layoutParams
        params.height =height+200
        lv!!.setLayoutParams(params)


    }

    override fun onResume() {
        super.onResume()
        //判断本地蓝牙是否已打开 调用系统API去打开蓝牙
        if (!mActivity!!.mble!!.isOpened) {
            val openIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(openIntent, REQUEST_ENABLE_BT)
        }
        if (vPnum==0){
            if (mActivity!!.mStatus!=1){
                scanLeDevice(true)        //开始蓝牙扫描
            }
        }else if (vPnum==1){
            if (mActivity!!.mStatus1!=1){
                scanLeDevice(true)        //开始蓝牙扫描
            }
        }
    }

    private inner class SetListView : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int,
                                 id: Long) {

            val device = mLeDeviceListAdapter!!.getDevice(position) ?: return
            val address = mLeDeviceListAdapter!!.getDevice(position)!!.address

            if (strAddress==address){
                if (vPnum==0){
                    if (mActivity!!.mStatus!=1){
                        progressDialog= ProgressDialog(activity)
                        progressDialog!!.setMessage(RString(R.string.dev_connecting))
                        progressDialog!!.show()
                        mActivity!!.connNum=0
                        Conn(strAddress.toString(),position)
                    }else{
                        Toast.makeText(activity, R.string.dev_con_OK, Toast.LENGTH_SHORT).show()
                    }
                }else if (vPnum==1){
                    if (mActivity!!.mStatus1!=1){
                        progressDialog= ProgressDialog(activity)
                        progressDialog!!.setMessage(RString(R.string.dev_connecting))
                        progressDialog!!.show()
                        mActivity!!.connNum=1
                        Conn(strAddress.toString(),position)
                    }else{
                        Toast.makeText(activity,  R.string.dev_con_OK, Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(activity,"连接设备最多只能连接两台!",Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(activity, R.string.dev_con_MACNull, Toast.LENGTH_SHORT).show()
            }
            mLeDeviceListAdapter!!.mLeDevices[position].status=BleModel.BLE_CONNECTING

            if (mScanning==true){
                scanLeDevice(false)        //停止蓝牙扫描
            }
            val textView = view.findViewById<View>(R.id.status) as TextView
            textView.text = mLeDeviceListAdapter!!.mLeDevices[position].statusDescription
        }
    }

    private inner class LeDeviceListAdapter() : BaseAdapter() {
        val mLeDevices: ArrayList<BleModel>
        val mLeAddress: ArrayList<String>
        val mInflator: LayoutInflater

        init {
            mLeDevices = ArrayList()
            mLeAddress = ArrayList()
            mInflator = LayoutInflater.from(activity)//MainActivity.this.getLayoutInflater();
        }

        fun getDevice(position: Int): BleModel? {
            return mLeDevices[position]
        }

        fun clear() {
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


}