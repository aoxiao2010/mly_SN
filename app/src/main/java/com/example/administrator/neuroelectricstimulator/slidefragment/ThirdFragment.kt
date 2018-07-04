package com.example.administrator.neuroelectricstimulator

import android.graphics.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.firstfragment.*
import kotlinx.android.synthetic.main.thirdfragment.*
import java.text.DecimalFormat

/**
 * author:Chance_Zheng.
 * date:  On 2018-06-25
 */

class ThirdFragment: Fragment() {
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
    private var count: Int = 0  //控制开关状态
    private var isBluetooth: Boolean = false  //蓝牙连接


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.thirdfragment,container,false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        image_equipment3.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                val clickX = event.x
                val clickY = event.y

                var df = DecimalFormat("###0.0")
                if(clickX>click_left && clickX<click_right && clickY>click_top && clickY<click_bottom)
                {
                    electric = df.format(electric.toFloat() - 0.1).toString()
                    pointPosition = "left"
                    change_electric(view!!)
                }

                if(clickX>380*x/548 && clickX<470*x/548 && clickY>280*y/705 && clickY<350*y/705)
                {
                    electric = df.format(electric.toFloat() + 0.1).toString()
                    pointPosition = "right"
                    change_electric(view!!)
                }

                val instance = Math.sqrt((clickX-click_x)*(clickX-click_x)+(clickY-click_y)*(clickY-click_y).toDouble())
                if(instance>=0 && instance <= standard_r){
                    count = count.plus(1)
                    pointPosition = "down"
                    change_electric(view!!)
                }

                if(clickX>400 && clickX<460 && clickY>200 && clickY<260){
                    Toast.makeText(activity,"蓝牙正在连接",Toast.LENGTH_SHORT).show()
                    isBluetooth = true
                    change_electric(view!!)
                }
            }

            if(event.action == MotionEvent.ACTION_UP){
                pointPosition = "usual"
                change_electric(view!!)
            }
            true
        }
    }

    fun firstView(){
        bitmap1 = BitmapFactory.decodeResource(resources,
                R.mipmap.es_link).copy(Bitmap.Config.ARGB_8888, true)

//        val bitmap2 = (resources.getDrawable(
//                R.drawable.dot) as BitmapDrawable).bitmap

        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(bitmap1)
        val canvas = Canvas(newBitmap)
        var paint = Paint()

        val img_width= image_equipment3.width
        val img_height = image_equipment3.height

        x= img_width
        y = img_height

        paint = Paint()
        paint.color = Color.RED


        val bitmap_clock = BitmapFactory.decodeResource(resources,
                R.mipmap.clock).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_clock,100f,120f,paint)

        var bitmap_switch: Bitmap? = null
        if(count % 2 == 0){
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
        image_equipment3.setImageBitmap(newBitmap)
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
        else if(pointPosition == "usual")
            bitmap1 = BitmapFactory.decodeResource(resources,
                    R.mipmap.es_link).copy(Bitmap.Config.ARGB_8888, true)
        var newBitmap: Bitmap? = null
        newBitmap = Bitmap.createBitmap(bitmap1)
        val canvas = Canvas(newBitmap)
        var paint = Paint()
        val w = bitmap1!!.width
        val h = bitmap1!!.height

        val img_width= image_equipment3.width
        val img_height = image_equipment3.height

        x= img_width
        y = img_height

        paint = Paint()
        paint.color = Color.RED


        val bitmap_clock = BitmapFactory.decodeResource(resources,
                R.mipmap.clock).copy(Bitmap.Config.ARGB_8888, true)
        canvas.drawBitmap(bitmap_clock,100f,120f,paint)

        var bitmap_switch: Bitmap? = null
        if(count % 2 == 0){
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


        paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 50f
        paint.color = Color.WHITE
        canvas.drawText(electric,250f,200f,paint)

        rectangleProportions(w,h,80,280,170,350)
        clickRectangle(img_width,img_height)
        rectangleProportions(w,h,380,280,470,350)

        circleProportions(w,h,280f,430f,30f)
        clickCircle(img_width,img_height,280f,430f,30f)


        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        image_equipment3.setImageBitmap(newBitmap)
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
}