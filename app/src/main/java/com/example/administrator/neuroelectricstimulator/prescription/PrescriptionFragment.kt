package com.example.administrator.neuroelectricstimulator

import android.app.AlertDialog
import android.app.Fragment
import android.app.ListFragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.equipmentinformation.*
import kotlinx.android.synthetic.main.prescriptioninformation.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class PrescriptionFragment: ListFragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null
    private var popupMenu: PopupMenu? = null
    private var mListAdapter: LeListAdapter? = null
    private var holder = ArrayList<PrescriptionHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.prescriptioninformation,container,false)
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popupMenu = PopupMenu(activity,rootView?.findViewById(R.id.add_prescription))
        popupMenu?.menuInflater?.inflate(R.menu.menu2,popupMenu?.menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        mListAdapter=LeListAdapter(activity)
        xmlRead(0)
        setListAdapter(mListAdapter)

        toolbar_prescription.setNavigationOnClickListener{
            if (mActivity!!.btnCFShow==true){
                mActivity!!.btnCFShow=false
                mActivity!!.changeFragment2(ManualequipmentFragment(),PrescriptionFragment())
            }else{
                mActivity?.changeFragment(MainFragment())
            }
        }

        add_prescription.setOnClickListener {
            if (mActivity!!.btnCFShow==true){
                Toast.makeText(activity, R.string.isAddprescription, Toast.LENGTH_LONG).show()
            }else{
                popupMenu?.show()  //显示弹出菜单
            }
        }

        //监听事件
        val integrator = IntentIntegrator(activity)
        popupMenu?.setOnMenuItemClickListener {item ->
            when(item.itemId){
                R.id.manual -> {
                    mActivity?.changeFragment(ManualprescriptionFragment())
                }
                R.id.flicking -> integrator.initiateScan()
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

    private fun RString(id: Int): String {
        return resources.getString(id)
    }


    private inner class LeListAdapter(context: Context) : BaseAdapter() {
        val mLeDevices: ArrayList<PrescriptionHolder>
        val mInflator: LayoutInflater

        init {
            mLeDevices = ArrayList()
            mInflator = LayoutInflater.from(context)//MainActivity.this.getLayoutInflater();
        }

        fun getDevice(position: Int): PrescriptionHolder? {
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
                view = mInflator.inflate(R.layout.childprescription, null)
                viewHolder = ViewHolder()
                viewHolder.acpointTextView = view!!.findViewById<View>(R.id.txtAcpoint) as TextView
                viewHolder.pulseWidthTextView = view.findViewById<View>(R.id.txtPulseWidth) as TextView
                viewHolder.freqTextView = view.findViewById<View>(R.id.txtFreq) as TextView
                viewHolder.onTimeTextView = view.findViewById<View>(R.id.txtOnTime) as TextView
                viewHolder.offTimeTextView=view.findViewById<View>(R.id.txtOffTime) as TextView
                viewHolder.pulseDirectionTextView=view.findViewById<View>(R.id.txtPulseDirection) as TextView
                viewHolder.nameTextView=view.findViewById<View>(R.id.txtName) as TextView
                viewHolder.ImgCFImageView=view.findViewById<View>(R.id.ImgCF) as ImageView
                viewHolder.cfDeleteTextView=view.findViewById<View>(R.id.cfDelete) as TextView
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val equip = mLeDevices[i]
            viewHolder.acpointTextView!!.text = equip.acpoint
            viewHolder.pulseWidthTextView!!.text = equip.pulseWidth
            viewHolder.freqTextView!!.text = equip.freq
            viewHolder.onTimeTextView!!.text = equip.onTime
            viewHolder.offTimeTextView!!.text = equip.offTime
            viewHolder.pulseDirectionTextView!!.text = equip.pulseDirection
            viewHolder.nameTextView!!.text = equip.name
            viewHolder.ImgCFImageView!!.setOnClickListener({
                if (mActivity!!.btnCFShow==true){
                    mActivity!!.btnCFShow=false
                    mActivity!!.cfID=equip.id
                    mActivity!!.cfName=equip.name
                    mActivity!!.cfAcpoint=equip.acpoint
                    mActivity!!.changeFragment2(ManualequipmentFragment(),PrescriptionFragment())
                }
            })
            viewHolder.cfDeleteTextView!!.setOnClickListener({
                val panel_info3 ="是否删除适用范围： "+equip.name+"  治疗部位："+equip.acpoint+" 的处方"
                AlertDialog.Builder(activity)
                        .setTitle(RString(R.string.delEquip))
                        .setMessage(panel_info3)
                        .setPositiveButton(RString(R.string.OK),  DialogInterface.OnClickListener { dialog, which ->
                            holder.clear()
                            if (xmlRead(1)){
                                if (xmlWrite(i)){
                                    mListAdapter!!.delValue(i)
                                    notifyDataSetChanged()
                                    Toast.makeText(activity,"处方删除成功！",Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(activity,"处方删除失败！",Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(activity,"处方删除失败！",Toast.LENGTH_SHORT).show()
                            }

                        })
                        .show()
            })

            return view
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        fun addValue(equip: PrescriptionHolder) {
            mLeDevices.add(equip)
        }
        fun delValue(position: Int) {
            mLeDevices.removeAt(position)
        }
    }

    internal class ViewHolder {
        var acpointTextView: TextView? = null
        var pulseWidthTextView: TextView? = null
        var freqTextView: TextView? = null
        var onTimeTextView: TextView? = null
        var offTimeTextView: TextView? = null
        var pulseDirectionTextView: TextView?=null
        var nameTextView: TextView? = null
        var ImgCFImageView:ImageView?=null
        var cfDeleteTextView:TextView?=null
    }
    private fun xmlWrite(position: Int):Boolean{
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
            holder.removeAt(position)
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
    private fun xmlRead(types:Int):Boolean{
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
                            if (types==0){
                                mListAdapter!!.addValue(PrescriptionHolder(Integer.parseInt(id),name,acpoint,freq,pulseWidth,onTime,offTime,strength,pulseDirection,pointCode))
                            }else{
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