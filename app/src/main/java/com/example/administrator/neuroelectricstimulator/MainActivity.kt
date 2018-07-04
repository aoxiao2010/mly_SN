package com.example.administrator.neuroelectricstimulator

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.graphics.Color.parseColor
import android.app.Fragment
import android.arch.lifecycle.ReportFragment
import android.bluetooth.BluetoothAdapter
import android.graphics.Bitmap
import android.graphics.Point
import android.os.*
import android.provider.CalendarContract
import android.support.constraint.solver.LinearSystem.getMetrics
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.view.ViewPager
import android.support.v7.widget.ActionBarContainer
import android.support.v7.widget.Toolbar
import android.text.TextUtils.replace
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.MenuInflater
import android.widget.*
import com.example.administrator.neuroelectricstimulator.R.id.*
import kotlinx.android.synthetic.main.equipmentinformation.*
import kotlinx.android.synthetic.main.equipmentinformation.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.new_menu.*


/**
 * author:Chance_Zheng.
 * date:  On 2018-04-27
 */


class MainActivity:FragmentActivity(),View.OnClickListener{

    private var fragments: ArrayList<Fragment>? = ArrayList<Fragment>()
    var L: Int = 0  //要添加设备数量
    var submit_main: Boolean = false // 是否点击提交
    var showSubmit:Boolean = false //是否显示提交按钮
    private var currentFragment: Fragment? = null
    var popup: PopupMenu? = null

    var mble: HolloBluetooth?= HolloBluetooth.getInstance(this)
    var mStatus: Int =0
    var mStatus1: Int =0
    var StimuStatus = 0
    var StimuStatus1 = 0

    var mHandler = Handler()
    private val MSG_BLE_DOWN = 52
    private val MSG_NEW_DATA = 53
    private val MSG_BLE_DOWN1 = 54
    private val MSG_NEW_DATA1 = 55
    private val MSG_CMD_100=100
    private val MSG_CMD_101=101
    var connNum:Int = 0
    private var mProc: ProtocolProc? = ProtocolProc()
    private val MSG_CMD_0 = 10
    private val MSG_CMD_7 = 17
    private val MSG_CMD_1 = 11
    private val MSG_CMD_3 = 13
    private val MSG_CMD_4 = 14
    private val MSG_CMD_5 = 15
    var btnShow:Boolean=false
    var btnCFShow:Boolean=false
    var equipId:Int?=null
    var cfID:Int?=null
    var cfName:String?=null
    var cfAcpoint:String?=null
    private val REQUEST_ENABLE_BT =1

    //2018.06.22
    var changeImage: Boolean = false //是否更改适配器中的加载布局
    var increasePoint: Boolean = false //是否增加治疗部位点  取决于治疗部位界面的提交按钮
    var spinnerSelected: String? = null //Spinner控件选中的字符串
    var acupoint: ArrayList<String> = arrayListOf<String>() //添加部位点的数组

    val equip_array: ArrayList<Int> =  arrayListOf<Int>()

    var adapter: FragmentAdapter? = null  //适配器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //ButterKnife.bind(this)

        if (!mble!!.isBleSupported || !mble!!.connectLocalDevice()) {
            Toast.makeText(this, "BLE is not supported on the device", Toast.LENGTH_SHORT).show()
            return
        }

        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_NEW_DATA -> {
                        val data = msg.obj as ByteArray
                        mProc!!.onIncomingData(data, 0)
                    }
                    MSG_NEW_DATA1 -> {
                        val data1 = msg.obj as ByteArray
                        mProc!!.onIncomingData(data1, 1)
                    }
                    MSG_CMD_0 -> {
                        val info = msg.obj as ProtocolProc.ExtraInfo
                        val connNum = info.ConnNum
                        if (connNum == 0){
                            StimuStatus =info.StimuStatus .toInt()
                        }else{
                            StimuStatus1 =info.StimuStatus .toInt()
                        }
                        if (fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)as StarttreatFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_0
                                newMsg.obj = msg.obj
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_100 ->{
                         if (fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)as EquipmentFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_100
                                newMsg.obj = msg.obj
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_101 ->{
                        if (fragmentManager.findFragmentByTag(ManualequipmentFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(ManualequipmentFragment::class.java!!.name)as ManualequipmentFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_101
                                newMsg.obj = msg.obj
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_1 -> {
                        if (fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)as StarttreatFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_1
                                newMsg.obj = msg.obj
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_3 ->{
                        if (fragmentManager.findFragmentByTag(ManualequipmentFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(ManualequipmentFragment::class.java!!.name)as ManualequipmentFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_3
                                newMsg.obj = msg.obj
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_4 -> {
                        if (fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)as EquipmentFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_4
                                newMsg.obj = msg.obj
                                newMsg.arg1=msg.arg1
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_5 -> {
                        if (fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(EquipmentFragment::class.java!!.name)as EquipmentFragment
                            if (frag != null) {
                                val newMsg = Message()
                                newMsg.what = MSG_CMD_5
                                newMsg.obj = msg.obj
                                newMsg.arg1=msg.arg1
                                frag?.mHandler?.sendMessage(newMsg)
                            }
                        }
                    }
                    MSG_CMD_7 -> {
                        if (fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)!=null){
                            var frag= fragmentManager.findFragmentByTag(StarttreatFragment::class.java!!.name)as StarttreatFragment
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
        }
        mProc!!.init_sys(mHandler,this)


        fragment1.setOnClickListener(this)
        fragment2.setOnClickListener(this)
        fragment3.setOnClickListener(this)
        fragment4.setOnClickListener(this)
        fragment5.setOnClickListener(this)

        changeFragment(MainFragment())  //放置初始的Fragment到主界面

        //setupDrawerContent(left_menu)
        initView()
        //initFragment()
        //initSlidingMenu()
        setListener()

    }

    private fun setupDrawerContent(navigationView: NavigationView?) {
        navigationView!!.setNavigationItemSelectedListener(
            object : NavigationView.OnNavigationItemSelectedListener {
                private var mPreMenuItem: MenuItem? = null
                override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                    if (mPreMenuItem != null)
                        mPreMenuItem!!.isChecked = false
                    menuItem.isCheckable=true
                    menuItem.isChecked = true
                    drawerlayout!!.closeDrawers()
                    mPreMenuItem = menuItem
                    return true
                }
            })
    }

    private fun initView(){
        drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,Gravity.LEFT)
    }

    private fun initFragment(){
        fragments?.add(TreatmentFragment())
    }

    private fun setListener(){
        drawerlayout.setDrawerListener(object :DrawerLayout.SimpleDrawerListener(){
            //在这个方法里可以设置动画效果
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            // 菜单打开
            override fun onDrawerOpened(drawerView: View) {

            }

            // 菜单关闭
            override fun onDrawerClosed(drawerView: View) {
                drawerlayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT)
//                supportFragmentManager.inTransaction{
//                    replace(R.id.content_fragment, TreatmentFragment())
//                }

                //show(this@MainActivity,"关闭","guanbi",false,true)
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
    }

    fun image2_Click(v: View?){
        drawerlayout.openDrawer(Gravity.LEFT)
        drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,Gravity.LEFT)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var intent = Intent()
        when(item?.itemId){
            R.id.treatment -> {
                //tv_bar_title.text="治疗"
                startActivity(Intent(MainActivity@this,TreatmentFragment::class.java))
                //item?.setIntent(intent.setClass(this,TreatmentActivity::class.java))
                //item?.intent = Intent(MainActivity@ this, TreatmentActivity::class.java)
                //Toast.makeText(this,"运行了",Toast.LENGTH_LONG).show()
                //setTitle(R.string.treatmentFragment)
            }

            R.id.equipment -> changeFragment1(EquipmentFragment(),MainFragment()) //startActivity(intent.setClass(this,EquipmentFragment::class.java))
            R.id.prescription -> startActivity(intent.setClass(this,PrescriptionFragment::class.java))
            R.id.report -> startActivity(intent.setClass(this,HealthReportFragment::class.java))
        }
        return true
       // return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {

        when (v?.id) {
            R.id.fragment1 -> changeFragment(Mine_mainFragment())
            R.id.fragment2 -> changeFragment(TreatmentFragment())
            R.id.fragment3 -> {
                changeFragment1(EquipmentFragment(),MainFragment())
                submit_main = false
                showSubmit = false
            }
            R.id.fragment4 -> changeFragment(PrescriptionFragment())
            R.id.fragment5 -> changeFragment(HealthReportFragment())
        }
    }

    fun changeFragment(fragment: android.app.Fragment) {
        currentFragment = fragment
        val manager = fragmentManager
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN.toInt()).replace(R.id.main, fragment,fragment::class.java!!.name)
        //fragmentTransaction.hide(currentFragment)
        //fragmentTransaction.addToBackStack(null)  //保存被替换的fragment状态
        fragmentTransaction.commit()
        fragmentTransaction.show(fragment)
        drawerlayout.closeDrawer(Gravity.START)
    }
    //from and to hide show
    fun changeFragment1(toFrag: android.app.Fragment, from:android.app.Fragment) {
        val manager = fragmentManager
        val fragmentTransaction = manager.beginTransaction()
        var froms=fragmentManager.findFragmentByTag(from::class.java!!.name)
        var tos=fragmentManager.findFragmentByTag(toFrag::class.java!!.name)
        if (froms!=null){
            fragmentTransaction.hide(froms)
        }else{
            fragmentTransaction.hide(currentFragment)
        }
        if (tos!=null){
            if (tos.isAdded){
                fragmentTransaction.show(tos)
            }else{
                fragmentTransaction.hide(currentFragment).add(R.id.main, toFrag,toFrag::class.java!!.name )
            }
        }else{
            fragmentTransaction.add(R.id.main, toFrag,toFrag::class.java!!.name )

        }
        //fragmentTransaction.addToBackStack(toFrag::class.java!!.name)
        fragmentTransaction.attach(toFrag)
        fragmentTransaction.commit()
        currentFragment = toFrag
        drawerlayout.closeDrawer(Gravity.START)
    }
    //from and to remove
    fun changeFragment2(toFrag: android.app.Fragment, from:android.app.Fragment) {
        val manager = fragmentManager
        val fragmentTransaction = manager.beginTransaction()
        var froms=fragmentManager.findFragmentByTag(from::class.java!!.name)
        var tos=fragmentManager.findFragmentByTag(toFrag::class.java!!.name)
        fragmentTransaction.remove(froms)
        if (tos!=null){
            if (tos.isAdded){
                fragmentTransaction.show(tos)
            }else{
                fragmentTransaction.add(R.id.main, toFrag,toFrag::class.java!!.name )
            }
        }else{
            fragmentTransaction.add(R.id.main, toFrag,toFrag::class.java!!.name )
        }

        //fragmentTransaction.addToBackStack(toFrag::class.java!!.name)
        fragmentTransaction.attach(toFrag)
        fragmentTransaction.commit()
        currentFragment = toFrag
        drawerlayout.closeDrawer(Gravity.START)
    }

    inline fun FragmentManager.inTransaction(func:FragmentTransaction.()->Unit){
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    fun Context.setViewClick(listener: View.OnClickListener, vararg views: View) {
        for (it in views) {
            it.setOnClickListener(listener)
        }
    }

    //传递L值
    fun setNumber(l:Int){
        this.L = l
    }

    fun getNumebr():Int{
        return L
    }

    //传递是否点击变量
    fun setClicked(click:Boolean){
        this.submit_main = click
    }

    fun getClicked():Boolean{
        return submit_main
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        this.L = outState!!.getInt(L.toString())
    }

    //获取屏幕大小
    fun getAndroiodScreenProperty() {

//        var display = getWindowManager().getDefaultDisplay();
//        var point = Point();
//        display.getSize(point); //getSize()是将屏幕减少后的信息存入其中(Status bar等装饰)
//        var width = point.x;
//        var height = point.y;

        var dm = DisplayMetrics();
        windowManager.defaultDisplay.getMetrics(dm)
        var width = dm.widthPixels;         // 屏幕宽度（像素）
        var height = dm.heightPixels;       // 屏幕高度（像素）
        var density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        var densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        var screenWidth = (width / density);  // 屏幕宽度(dp)
        var screenHeight = (height / density);// 屏幕高度(dp)


        Log.d("h_bl", "屏幕宽度（像素）：" + width);
        Log.d("h_bl", "屏幕高度（像素）：" + height);
        Log.d("h_bl", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        Log.d("h_bl", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        Log.d("h_bl", "屏幕宽度（dp）：" + screenWidth);
        Log.d("h_bl", "屏幕高度（dp）：" + screenHeight);
    }

    /**
     * 自适应图片的ImageView
     * @param context
     * @param image
     * @param source
     */
    fun setImageViewMathParent(context: Activity,
                               image:ImageView , bitmap: Bitmap) {
        //获得屏幕密度
        var displayMetrics = DisplayMetrics();
        context.getWindowManager().getDefaultDisplay()
        .getMetrics(displayMetrics);
        //获得屏幕宽度和图片宽度的比例
        var scalew = displayMetrics.widthPixels / bitmap.getWidth();
        //获得ImageView的参数类
        var vgl=image.getLayoutParams();
        //设置ImageView的宽度为屏幕的宽度
        vgl.width=displayMetrics.widthPixels;
        //设置ImageView的高度
        vgl.height= bitmap.getHeight()*scalew;
        //设置图片充满ImageView控件
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        //等比例缩放
        image.setAdjustViewBounds(true);
        image.setLayoutParams(vgl);
        image.setImageBitmap(bitmap);

        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
        }

    }

    var bleCallBack: HolloBluetooth.OnHolloBluetoothCallBack = object : HolloBluetooth.OnHolloBluetoothCallBack {

        override fun OnHolloBluetoothState(state: Int) {
            if (state == HolloBluetooth.HOLLO_BLE_DISCONNECTED) {
                val msg = Message()
                msg.what = MSG_BLE_DOWN
                mHandler.sendMessage(msg)
            }
        }

        override fun OnReceiveData(recvData: ByteArray) {
            //String para=ConvertData.bytesToHexString(recvData, false);
            val msg = Message()
            msg.what = MSG_NEW_DATA
            msg.obj = recvData
            mHandler.sendMessage(msg)
        }
    }
    var bleCallBack1: HolloBluetooth.OnHolloBluetoothCallBack1 = object : HolloBluetooth.OnHolloBluetoothCallBack1 {

        override fun OnHolloBluetoothState(state: Int) {
            if (state == HolloBluetooth.HOLLO_BLE_DISCONNECTED) {
                val msg = Message()
                msg.what = MSG_BLE_DOWN1
                mHandler.sendMessage(msg)
            }
        }

        override fun OnReceiveData(recvData: ByteArray) {
            //String para=ConvertData.bytesToHexString(recvData, false);
            val msg = Message()
            msg.what = MSG_NEW_DATA1
            msg.obj = recvData
            mHandler.sendMessage(msg)
        }
    }

    fun Conn(address: String,type:Int):Boolean?{
        var bool:Boolean=false

        Handler().post(Runnable {
             try{
                 var i: Int
                 i = 0
                 while (i < 5){

                     if (connNum == 0) {
                         if (mble!!.connectDevice(address, bleCallBack))
                         //连接蓝牙设备
                             break
                     }
                     if (connNum == 1) {
                         if (mble!!.connectDevice1(address, bleCallBack1))
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
                     if (connNum == 0) {
                         mStatus = 0
                     }
                     if (connNum == 1) {
                         mStatus1 =0
                     }
                     bool=false
                     if (type==1){
                         newMsg.what = MSG_CMD_100
                         newMsg.obj = 0
                         mHandler?.sendMessage(newMsg)
                     }else{
                         newMsg.what = MSG_CMD_101
                         newMsg.obj = 0
                         mHandler?.sendMessage(newMsg)
                     }
                 }
                 try {
                     Thread.sleep(200, 0)//200ms
                 } catch (e: Exception) {

                 }

                 if (mble!!.wakeUpBle(address)) {
                     if (connNum == 0 && mble!!.mBluetoothDeviceAddress.size>0) {
                         mStatus = 1
                     }
                     if (connNum == 1 && mble!!.mBluetoothDeviceAddress.size>1) {
                         mStatus1 = 1
                     }
                     if (type==1){
                         mProc!!.send_cmd_0(mble, address)
                     }
                     connNum++
                     bool=true
                     if (type==1){
                         newMsg.what = MSG_CMD_100
                         newMsg.obj = 1
                         mHandler?.sendMessage(newMsg)
                     }else{
                         newMsg.what = MSG_CMD_101
                         newMsg.obj = 1
                         mHandler?.sendMessage(newMsg)
                     }
                 } else {
                     if (connNum == 0) {
                         mStatus = 0
                     }
                     if (connNum == 1) {
                         mStatus1 = 0
                     }
                     bool=false
                     if (type==1){
                         newMsg.what = MSG_CMD_100
                         newMsg.obj = 0
                         mHandler?.sendMessage(newMsg)
                     }else{
                         newMsg.what = MSG_CMD_101
                         newMsg.obj = 0
                         mHandler?.sendMessage(newMsg)
                     }
                 }
             }catch (e:Exception){
                 var str=e.toString()
             }

        })

        return bool
    }

    override fun onDestroy() {
        for (bluetoothGatt in mble!!.connectionQueue) {
            mble!!.disconnectDevice(bluetoothGatt)
            mble!!.disconnectLocalDevice(bluetoothGatt)
        }
        mble!!.connectionQueue.clear()
        mble!!.mBluetoothDeviceAddress.clear()
        super.onDestroy()
    }
    override fun onResume() {
        //判断本地蓝牙是否已打开 调用系统API去打开蓝牙
        if (!mble!!.isOpened) {
            val openIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(openIntent, REQUEST_ENABLE_BT)
        }
        super.onResume()
    }

    fun initData() {
        var list = java.util.ArrayList<android.support.v4.app.Fragment>()
        //list?.add(FirstFragment())
//        list?.add(SecondFragment())
//        list.add(ThirdFragment())
//        list.add(FourthFragment())
        var vId=ArrayList<Int>()
        for(i in 0 until equip_array.size){
            list?.add(FirstFragment())
            vId!!.add(equip_array[i])
        }
        adapter = FragmentAdapter(this,supportFragmentManager,list,vId)
        //初始化adapter
        //adapter = FragmentAdapter(supportFragmentManager, list)

        //将适配器和ViewPager结合
        //supportFragmentManager.findFragmentById(R.id.lin_treat).view!!.findViewById<ViewPager>(R.id.viewpager).adapter = adapter
        //viewpager.setAdapter(adapter)
    }
}