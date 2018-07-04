package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Xml
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.equipmentinformation.*
import kotlinx.android.synthetic.main.treatposition.*
import android.widget.AdapterView.OnItemSelectedListener
import com.example.administrator.neuroelectricstimulator.R.id.toolbar_addposition
import com.example.administrator.neuroelectricstimulator.R.id.spinner
import kotlinx.android.synthetic.main.treatposition.view.*
import java.util.ArrayList
import android.widget.TextView
import kotlinx.android.synthetic.main.treatment_fragment.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class TreatpositionFragment:Fragment(){
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var str: String? = null
    private var j:Int = 0
    private var received: Boolean = false
    private var holder = ArrayList<EquipmentHolder>()
    private var Tholder = ArrayList<TreatpositionHolder>()
    private var isSubmit: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.treatposition,container,false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActivity = activity as MainActivity
        BW_xmlRead()
        if (mActivity!!.equipId!=null){
            xmlRead()
            select_equipment.visibility=View.VISIBLE
            submit3.visibility=View.VISIBLE
            for (item in holder!!.iterator()){
                if (item.id.toString()==mActivity!!.equipId.toString()){
                    txtMAC_BW.text=item.address
                    txtName_CF.text=item.name
                    txt_BW.text=item.pres
                    mActivity?.spinnerSelected =item.pres
                    break
                }
            }
        }

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            //当用户选择其中一项时
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                str = spinner.selectedItem.toString()
//                txt_BW.text = str
//                 str=txt_BW.text.toString()
//                mActivity?.spinnerSelected = str
//                val tv = view as TextView
//                tv.textSize = 20f
//                tv.gravity = Gravity.LEFT

                (parent.getChildAt(0) as TextView).textSize = 20f
                (parent.getChildAt(0) as TextView).gravity = Gravity.LEFT
            }

            //当没有任何选择时
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        submit3.setOnClickListener {
            var bool:Boolean=false
            for (item in Tholder!!.iterator()){
                if (item.siteName==txt_BW.text.toString()){
                    bool=true
                }
            }
            if (bool==false){
                var tmp=TreatpositionHolder()
                tmp.id=holder.size+1
                tmp.equipmentID=mActivity!!.equipId!!.toInt()
                tmp.presName=txtName_CF.text.toString()
                tmp.siteName=txt_BW.text.toString()
                tmp.address=txtMAC_BW.text.toString()
                Tholder.add(tmp)
            }
            if (xmlWrite()){
                mActivity!!.equipId=null

                mActivity?.spinnerSelected =txt_BW.text.toString()
                mActivity?.setClicked(true)
                mActivity!!.increasePoint = true

                mActivity?.changeFragment(TreatmentFragment())
            }
        }

        toolbar_addposition.setNavigationOnClickListener {
            mActivity?.changeFragment(TreatmentFragment())
        }

        image_btn3.setOnClickListener {
            mActivity!!.btnShow=true
            isSubmit = true
            mActivity?.setClicked(isSubmit)
            mActivity?.changeFragment(EquipmentFragment())
        }

//        //显示提交按钮
//        received = mActivity!!.getClicked()
//        j = mActivity!!.getNumebr()
//        if(received){
//            submit3.visibility = View.VISIBLE
//            select_equipment.visibility = View.VISIBLE
//        }
        //显示提交按钮
        received = mActivity!!.getClicked()
        j = mActivity!!.getNumebr()
        if(received){
            submit3.visibility = View.VISIBLE
            select_equipment.visibility = View.VISIBLE
        }

        //改变图片颜色
        if(mActivity!!.getClicked())
            image_btn3.setImageResource(R.drawable.sa)
        else
            image_btn3.setImageResource(R.drawable.sb)
    }

    override fun onDestroy() {
        mActivity!!.equipId=null
        super.onDestroy()
    }

    private fun xmlWrite():Boolean{
        try{
            var file= File(activity.getFilesDir(),"treatposition.xml")
            if (file.exists()){
                file.delete()
            }
            var fos = FileOutputStream(file);
            // 获得一个序列化工具
            val serializer = Xml.newSerializer()
            serializer.setOutput(fos, "utf-8")
            // 设置文件头
            serializer.startDocument("utf-8", true)
            serializer.startTag(null, "users")
            var num:Int=0
            for (item in Tholder!!.iterator()){
                num=num+1
                serializer.startTag(null, "item")
                serializer.attribute(null, "id",num.toString())

                serializer.startTag(null, "equipmentID")
                serializer.text(item.equipmentID.toString())
                serializer.endTag(null, "equipmentID")

                serializer.startTag(null, "siteName")
                serializer.text(item.siteName)
                serializer.endTag(null, "siteName")

                serializer.startTag(null, "presName")
                serializer.text(item.presName)
                serializer.endTag(null, "presName")

                serializer.startTag(null, "address")
                serializer.text(item.address)
                serializer.endTag(null, "address")

                serializer.endTag(null, "item")
            }
            serializer.endTag(null, "users")
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

    private fun BW_xmlRead():Boolean{
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