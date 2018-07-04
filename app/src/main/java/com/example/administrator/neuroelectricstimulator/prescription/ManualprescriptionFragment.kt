package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.os.Bundle
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.manualprescription.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class ManualprescriptionFragment: Fragment()  {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var holder = ArrayList<PrescriptionHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.manualprescription,container,false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //xmlRead()

        mActivity = activity as MainActivity
        return_back5.setOnClickListener{
            mActivity?.changeFragment(PrescriptionFragment())
        }

        submit1.setOnClickListener {
            var tmp=PrescriptionHolder()
            tmp.id=holder.size
            tmp.name="FD"//"FD"
            tmp.acpoint="足三里(ST36)"//足三里(ST36)
            tmp.freq="25"//"25"
            tmp.pulseWidth="500"
            tmp.onTime="20"//"20"
            tmp.offTime="30"//"30"
            tmp.strength="1.0"
            tmp.pulseDirection="0"
            tmp.pointCode="21"//"21"
            holder.add(tmp)

            tmp=PrescriptionHolder()
            tmp.id=holder.size
            tmp.name="FD"//"FD"
            tmp.acpoint="内关(PC6)"//足三里(ST36)
            tmp.freq="100"//"25"
            tmp.pulseWidth="500"
            tmp.onTime="10"//"20"
            tmp.offTime="40"//"30"
            tmp.strength="1.0"
            tmp.pulseDirection="0"
            tmp.pointCode="22"//"21"
            holder.add(tmp)

            tmp=PrescriptionHolder()
            tmp.id=holder.size
            tmp.name="糖尿病"//"FD"
            tmp.acpoint="关元(CV4)"
            tmp.freq="15"//"25"
            tmp.pulseWidth="500"
            tmp.onTime="600"//"20"
            tmp.offTime="0"//"30"
            tmp.strength="1.0"
            tmp.pulseDirection="0"
            tmp.pointCode="21"//"21"
            holder.add(tmp)

            tmp=PrescriptionHolder()
            tmp.id=holder.size
            tmp.name="便秘"//"FD"
            tmp.acpoint="胫后神经"
            tmp.freq="25"//"25"
            tmp.pulseWidth="500"
            tmp.onTime="20"//"20"
            tmp.offTime="30"//"30"
            tmp.strength="1.0"
            tmp.pulseDirection="0"
            tmp.pointCode="22"//"21"
            holder.add(tmp)

            if (xmlWrite()){
                mActivity?.changeFragment(PrescriptionFragment())
            }else{
                Toast.makeText(activity,"处方添加失败！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun xmlWrite():Boolean{
        try{
            var file= File(activity.getFilesDir(),"prescripion.xml")
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
            for (item in holder!!.iterator()){
                num=num+1
                serializer.startTag(null, "item")
                serializer.attribute(null, "id",num.toString())

                serializer.startTag(null, "name")
                serializer.text(item.name)
                serializer.endTag(null, "name")

                serializer.startTag(null, "acpoint")
                serializer.text(item.acpoint)
                serializer.endTag(null, "acpoint")

                serializer.startTag(null, "freq")
                serializer.text(item.freq)
                serializer.endTag(null, "freq")

                serializer.startTag(null, "pulseWidth")
                serializer.text(item.pulseWidth)
                serializer.endTag(null, "pulseWidth")

                serializer.startTag(null, "onTime")
                serializer.text(item.onTime)
                serializer.endTag(null, "onTime")

                serializer.startTag(null, "offTime")
                serializer.text(item.offTime)
                serializer.endTag(null, "offTime")

                serializer.startTag(null, "strength")
                serializer.text(item.strength)
                serializer.endTag(null, "strength")

                serializer.startTag(null, "pulseDirection")
                serializer.text(item.pulseDirection)
                serializer.endTag(null, "pulseDirection")

                serializer.startTag(null, "pointCode")
                serializer.text(item.pointCode)
                serializer.endTag(null, "pointCode")

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
                            holder.add(tmp)
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

}