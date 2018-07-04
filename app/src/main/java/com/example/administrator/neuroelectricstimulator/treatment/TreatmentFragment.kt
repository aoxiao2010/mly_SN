package com.example.administrator.neuroelectricstimulator

import android.app.AlertDialog
import android.os.Bundle
import android.app.Fragment
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.*
//import android.support.v4.app.Fragment
import android.widget.Toast
import kotlinx.android.synthetic.main.treatment_fragment.*
import android.util.DisplayMetrics
import android.util.Log
import android.graphics.Bitmap
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.ArrayList


/**
 * author:Chance_Zheng.
 * date:  On 2018-05-03
 */
class TreatmentFragment:Fragment(){
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var r: Float = 0f  //圆的半径

    private var cx: Float = 0f
    private var cy: Float = 0f

    //是否增加部位点
    private var zslPoint: Boolean = false
    private var ngPoint: Boolean = false
    private var zwxPoint: Boolean = false
    private var gyPoint: Boolean = false

    //点击部位点的次数
    private var zslCount: Int = 1
    private var ngCount: Int = 1
    private var zwxCount: Int = 1
    private var gyCount: Int = 1

    //加载部位图片位置
    private var left: Float = 0f
    private var top: Float = 0f

    //文字点
    private var x: Float = 0f
    private var y: Float = 0f

    //绘制椭圆
    private var rect = RectF()

    //点击矩形
    private var rect_zsl = RectF()
    private var rect_ng = RectF()
    private var rect_gy = RectF()
    private var rect_zwx = RectF()

    private var Tholder = ArrayList<TreatpositionHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.treatment_fragment,container,false)
        rootView?.isClickable = true //防止点击穿透，底层的fragment响应上层的触摸事件
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstImage()  //让ImageView控件呈现与画布同样大小的图片
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActivity = activity as MainActivity

        toolbar_treat.setNavigationOnClickListener {
            mActivity?.changeFragment(MainFragment())
        }

        add_position.setOnClickListener{
            mActivity?.setClicked(false)
            mActivity?.changeFragment(TreatpositionFragment())
        }

        xmlRead()

        start_treat.setOnClickListener{
            mActivity!!.equip_array.clear()
            for (item in Tholder!!.iterator()){
                if (item.siteName=="足三里(ST36)" && zslPoint==true){
                    mActivity!!.equip_array.add(item.equipmentID)
                    continue
                }
                if (item.siteName=="内关(PC6)" && ngPoint==true){
                    mActivity!!.equip_array.add(item.equipmentID)
                    continue
                }
                if (item.siteName=="关元(CV4)" && zwxPoint==true){
                    mActivity!!.equip_array.add(item.equipmentID)
                    continue
                }
                if (item.siteName=="胫后神经" && gyPoint==true){
                    mActivity!!.equip_array.add(item.equipmentID)
                    continue
                }
            }

            if (mActivity!!.equip_array.size>0){
                mActivity?.changeFragment(StarttreatFragment())
            }else{
                Toast.makeText(activity, R.string.selectBW,Toast.LENGTH_SHORT).show()
            }
        }

        imagePoint()

        img.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                //点击的时候获取点击的坐标及X、Y值
                val clickX = event.x
                val clickY = event.y

                var metrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(metrics);
                val width = metrics.widthPixels
                val height = metrics.heightPixels

                Log.i("屏幕宽度：",width.toString())
                Log.i("屏幕高度：",height.toString())

                Log.i("点击的X坐标：",clickX.toString())
                Log.i("点击的Y坐标：",clickY.toString())

                Log.i("zhi1：",rect_zsl.left.toString())
                Log.i("zhi1：",rect_ng.left.toString())
                Log.i("zhi1：",rect.top.toString())
                Log.i("zhi1：",rect.bottom.toString())

                if(clickX>rect_zsl.left && clickX<rect_zsl.right && clickY>rect_zsl.top && clickY<rect_zsl.bottom){
                    zslCount = zslCount.plus(1)
                    if(zslCount % 2 == 0){
                        zslPoint = true
                        imagePoint()
                    }else{
                        zslPoint = false
                        imagePoint()
                    }
                }
                if(clickX>rect_ng.left && clickX<rect_ng.right && clickY>rect_ng.top && clickY<rect_ng.bottom){
                    ngCount = ngCount.plus(1)
                    if(ngCount % 2 == 0){
                        ngPoint = true
                        imagePoint()
                    }else{
                        ngPoint = false
                        imagePoint()
                    }
                }
                if(clickX>rect_zwx.left && clickX<rect_zwx.right && clickY>rect_zwx.top && clickY<rect_zwx.bottom){
                    zwxCount = zwxCount.plus(1)
                    if(zwxCount % 2 == 0){
                        zwxPoint = true
                        imagePoint()
                    }else{
                        zwxPoint = false
                        imagePoint()
                    }
                }
                if(clickX>rect_gy.left && clickX<rect_gy.right && clickY>rect_gy.top && clickY<rect_gy.bottom){
                    gyCount = gyCount.plus(1)
                    if(gyCount % 2 == 0){
                        gyPoint = true
                        imagePoint()
                    }else{
                        gyPoint = false
                        imagePoint()
                    }
                }
            }
            true
        }



    }


    private fun RString(id: Int): String {
        return resources.getString(id)
    }

    //进入治疗界面时ImageView显示的图片
    fun firstImage(){
        var bitmap1 = BitmapFactory.decodeResource(resources,
                R.mipmap.st).copy(Bitmap.Config.ARGB_8888,true)

        var metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics);
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val height1 = 3*height/5


        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(width,height1,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        var paint = Paint()

        var rec1: Rect
        var rec2: Rect
        var changedBitmap: Bitmap? = null
        var scale: Float = 0f


        if(width < bitmap1.width && height1 >= bitmap1.height){
            scale = width / bitmap1.width.toFloat()
        }

        if(width >= bitmap1.width && height1 < bitmap1.height){
            scale = height1 / bitmap1.height.toFloat()
        }

        if(width < bitmap1.width && height1 < bitmap1.height){
            val scale1 = width / bitmap1.width.toFloat()
            val scale2 = height1 / bitmap1.height.toFloat()
            if(scale1 > scale2)
                scale = scale2
            else
                scale = scale1
        }

        if(width >= bitmap1.width && height1 >= bitmap1.height){
            scale = 1f
        }

        val matrix = Matrix()
        matrix.postScale(scale, scale)
        changedBitmap = Bitmap.createBitmap(bitmap1, 0, 0,
                bitmap1.width, bitmap1.height, matrix, true)

        rec1 = Rect(0,0,changedBitmap!!.width,changedBitmap.height)
        rec2 = Rect((width-changedBitmap.width)/2,(height1-changedBitmap.height)/2,
                changedBitmap.width+(width-changedBitmap.width)/2,changedBitmap.height+(height1-changedBitmap.height)/2)

        paint = Paint()
        paint.color = Color.WHITE
        canvas.drawColor(Color.WHITE)
        //bitmap1.eraseColor(Color.WHITE)
        canvas.drawBitmap(changedBitmap,rec1,rec2,paint)


        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        img.setImageBitmap(newBitmap)
    }


    //给图片添加治疗部位点
    fun imagePoint(){
        var bitmap1 = BitmapFactory.decodeResource(resources,
                R.mipmap.st).copy(Bitmap.Config.ARGB_8888,true)

        var metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics);
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val height1 = 3*height/5

        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(width,height1,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        var paint = Paint()

        var rec1: Rect
        var rec2: Rect
        var changedBitmap: Bitmap? = null
        var scale: Float = 0f


        if(width < bitmap1.width && height1 >= bitmap1.height){
            scale = width / bitmap1.width.toFloat()
        }

        if(width >= bitmap1.width && height1 < bitmap1.height){
            scale = height1 / bitmap1.height.toFloat()
        }

        if(width < bitmap1.width && height1 < bitmap1.height){
            val scale1 = width / bitmap1.width.toFloat()
            val scale2 = height1 / bitmap1.height.toFloat()
            if(scale1 > scale2)
                scale = scale2
            else
                scale = scale1
        }

        if(width >= bitmap1.width && height1 >= bitmap1.height){
            scale = 1f
        }

        val matrix = Matrix()
        matrix.postScale(scale, scale)
        changedBitmap = Bitmap.createBitmap(bitmap1, 0, 0,
                bitmap1.width, bitmap1.height, matrix, true)

        rec1 = Rect(0,0,changedBitmap!!.width,changedBitmap.height)
        rec2 = Rect((width-changedBitmap.width)/2,(height1-changedBitmap.height)/2,
                changedBitmap.width+(width-changedBitmap.width)/2,changedBitmap.height+(height1-changedBitmap.height)/2)

        paint = Paint()
        paint.color = Color.WHITE
        canvas.drawColor(Color.WHITE) //GREEN
        //bitmap1.eraseColor(Color.WHITE)
        canvas.drawBitmap(changedBitmap,rec1,rec2,paint)

        this.r = 5f



        //6.22 xcf
        for (item in Tholder!!.iterator()){
            if(item.siteName == "足三里(ST36)"){
                //足三里
                paint = Paint()
                paint.color = Color.RED
                var zsl_bitmap = BitmapFactory.decodeResource(resources,R.mipmap.zsl).copy(Bitmap.Config.ARGB_8888,true)
                pictureArea( 400f,850f,width,height1,changedBitmap,bitmap1)
                canvas.drawBitmap(zsl_bitmap,left,top,paint)

                paintPoint(365,895,width,height1,changedBitmap,bitmap1)
                canvas.drawCircle(cx,cy,3*r,paint)

                paint = Paint()
                paint.textSize = 18f
                paintText(320f,955f,width,height1,changedBitmap,bitmap1)
                canvas.drawText("足三里(ST36)",x,y,paint)

                val rectF = RectF(400f,850f,509f,943f)
                getRectangle(rectF,width,height1,changedBitmap,bitmap1)
                rect_zsl.left = rectF.left
                rect_zsl.right = rectF.left + zsl_bitmap.width
                rect_zsl.top = rectF.top
                rect_zsl.bottom = rect_zsl.top + zsl_bitmap.height

                if(zslPoint)
                    paintOval(rect_zsl,canvas)
            }
            if(item.siteName == "内关(PC6)"){
                //内关(PC6)
                paint = Paint()
                paint.color = Color.RED
                var ng_bitmap = BitmapFactory.decodeResource(resources,R.mipmap.ng).copy(Bitmap.Config.ARGB_8888,true)
                pictureArea(500f,510f,width,height1,changedBitmap,bitmap1)
                canvas.drawBitmap(ng_bitmap,left,top,paint)

                paintPoint(450,565,width,height1,changedBitmap,bitmap1)
                canvas.drawCircle(cx,cy,3*r,paint)

                paint = Paint()
                paint.textSize = 18f
                paintText(420f,625f,width,height1,changedBitmap,bitmap1)
                canvas.drawText("内关(PC6)",x,y,paint)

                val rectF = RectF(500f,510f,607f,601f)
                getRectangle(rectF,width,height1,changedBitmap,bitmap1)
                rect_ng.left = rectF.left
                rect_ng.right = rectF.left + ng_bitmap.width
                rect_ng.top = rectF.top
                rect_ng.bottom = rectF.top + ng_bitmap.height

                if(ngPoint)
                    paintOval(rect_ng,canvas)
            }
            if(item.siteName == "关元(CV4)"){
                //关元(CV4)
                paint = Paint()
                paint.color = Color.RED
                var zwx_bitmap = BitmapFactory.decodeResource(resources,R.mipmap.zwx).copy(Bitmap.Config.ARGB_8888,true)
                pictureArea(20f,355f,width,height1,changedBitmap,bitmap1)
                canvas.drawBitmap(zwx_bitmap,left,top,paint)

                paintPoint(285,405,width,height1,changedBitmap,bitmap1)
                canvas.drawCircle(cx,cy,3*r,paint)

                paint = Paint()
                paint.textSize = 18f
                paintText(255f,465f,width,height1,changedBitmap,bitmap1)
                canvas.drawText("关元(CV4)",x,y,paint)

                val rectF = RectF(20f,355f,126f,444f)
                getRectangle(rectF,width,height1,changedBitmap,bitmap1)
                rect_zwx.left = rectF.left
                rect_zwx.right =rectF.left + zwx_bitmap.width
                rect_zwx.top = rectF.top
                rect_zwx.bottom = rectF.top + zwx_bitmap.height

                if(zwxPoint)
                    paintOval(rect_zwx,canvas)
            }
            if(item.siteName == "胫后神经"){
                //胫后神经
                paint = Paint()
                paint.color = Color.RED

                var gy_bitmap = BitmapFactory.decodeResource(resources,R.mipmap.gy).copy(Bitmap.Config.ARGB_8888,true)
                pictureArea(0f,480f,width,height1,changedBitmap,bitmap1)
                canvas.drawBitmap(gy_bitmap,left,top,paint)

                paintPoint(285,535,width,height1,changedBitmap,bitmap1)
                canvas.drawCircle(cx,cy,3*r,paint)

                paint = Paint()
                paint.textSize = 18f
                paintText(255f,595f,width,height1,changedBitmap,bitmap1)
                canvas.drawText("胫后神经",x,y,paint)

                val rectF = RectF(0f,480f,110f,570f)
                getRectangle(rectF,width,height1,changedBitmap,bitmap1)
                rect_gy.left = rectF.left
                rect_gy.right = rectF.left + gy_bitmap.width
                rect_gy.top = rectF.top
                rect_gy.bottom = rectF.top + gy_bitmap.height

                if(gyPoint)
                    paintOval(rect_gy,canvas)
            }
        }


        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        img.setImageBitmap(newBitmap)
    }

    /**
     * 绘制部位点
     * @param x,y 图片中的标记点坐标
     * @param width,height 画布的宽高
     * @param changedBitmap 放进画布后的图片
     * @parameter 原始图片
     */
    fun paintPoint(x:Int,y:Int,width:Int,height: Int,changedBitmap: Bitmap,bitmap: Bitmap){
        var df = DecimalFormat("###0.0")
        cx = df.format(x*changedBitmap.width/bitmap.width.toFloat() + (width - changedBitmap.width)/2f).toFloat()
        cy =  df.format(y*changedBitmap.height/bitmap.height.toFloat() + (height - changedBitmap.height)/2f).toFloat()
    }


    /**
     * 部位示意图
     * @param left1,top1 图片放在画布上的位置
     * @param width,height 画布的宽高
     * @param changedBitmap 放进画布后的图片
     * @parameter 原始图片
     */
    fun pictureArea(left1:Float,top1:Float,width:Int,height: Int,changedBitmap: Bitmap,bitmap: Bitmap){
        var df = DecimalFormat("###0.0")
        left = df.format(left1*changedBitmap.width/bitmap.width.toFloat() + (width - changedBitmap.width)/2f).toFloat()
        top =  df.format(top1*changedBitmap.height/bitmap.height.toFloat() + (height - changedBitmap.height)/2f).toFloat()
    }


    /**
     * 部位名称
     * @param x1,y1 名称放在画布上的位置
     * @param width,height 画布的宽高
     * @param changedBitmap 放进画布后的图片
     * @parameter 原始图片
     */
    fun paintText(x1:Float,y1:Float,width:Int,height: Int,changedBitmap: Bitmap,bitmap: Bitmap){
        var df = DecimalFormat("###0.0")
        x = df.format(x1*changedBitmap.width/bitmap.width.toFloat() + (width - changedBitmap.width)/2f).toFloat()
        y =  df.format(y1*changedBitmap.height/bitmap.height.toFloat() + (height - changedBitmap.height)/2f).toFloat()
    }


    //得到画椭圆的矩形
    fun getRectangle(rectF: RectF,width: Int,height: Int,changedBitmap: Bitmap,bitmap: Bitmap):RectF{
        var df = DecimalFormat("###0.0")
        rectF.left = df.format(rectF.left*changedBitmap.width/bitmap.width + (width - changedBitmap.width)/2f).toFloat()
        rectF.top = df.format(rectF.top*changedBitmap.height/bitmap.height + (height - changedBitmap.height)/2f).toFloat()
        return rect
    }

    /**
     * 选中效果
     * @param rectF 需要绘制的椭圆
     */
    fun paintOval(rectF: RectF,canvas: Canvas){
        var paint = Paint()
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawOval(rectF,paint)
    }

    private fun xmlRead():Boolean{
        try{
            var file= File(activity.getFilesDir(),"treatposition.xml")
            var fis = FileInputStream(file);

            var parser= Xml.newPullParser()
            parser.setInput(fis, "utf-8")
            var eventType = parser.getEventType()
            var id:String?=null
            var siteName:String?=null
            var address:String?=null
            var presName:String?=null
            var equipmentID:String?=null
            while (eventType != XmlPullParser.END_DOCUMENT){
                var tagName = parser.getName()
                when (eventType){
                    XmlPullParser.START_TAG ->{
                        if ("users".equals(tagName)) {
                        } else if ("item".equals(tagName)) {
                            id = parser.getAttributeValue(null, "id")
                        } else if ("equipmentID".equals(tagName)) {
                            equipmentID = parser.nextText()
                        }else if ("siteName".equals(tagName)) {
                            siteName = parser.nextText()
                        } else if ("presName".equals(tagName)) {
                            presName = parser.nextText()
                        }else if ("address".equals(tagName)) {
                            address = parser.nextText()
                        }
                    }
                    XmlPullParser.END_TAG ->{
                        if (id!=null){
                            var tmp=TreatpositionHolder()
                            tmp.id=Integer.parseInt(id)
                            tmp.siteName=siteName
                            tmp.address=address
                            tmp.presName=presName
                            tmp.equipmentID=Integer.parseInt(equipmentID)
                            Tholder.add(tmp)
                        }
                        id=null
                        siteName=null
                        address=null
                        presName=null
                        equipmentID=null
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

}