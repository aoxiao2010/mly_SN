package com.example.administrator.neuroelectricstimulator

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Xml
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * author:Chance_Zheng.
 * date:  On 2018-04-27
 */
class RegisterActivity: AppCompatActivity(){
    private var holder = ArrayList<RegisterHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        xmlRead()
        setContentView(R.layout.activity_register)
    }

    //注册
    fun register_Click(v:View?){
        when(v?.id){
            R.id.register ->{
                if (edtName.text.toString()==""){
                    Toast.makeText(this,"名字不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                if (edtXName.text.toString()==""){
                    Toast.makeText(this,"姓不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                if (edtHospId.text.toString()==""){
                    Toast.makeText(this,"医生ID不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                if (edtPhone.text.toString()==""){
                    Toast.makeText(this,"邮箱或者电话不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                if (edtPassword.text.toString()==""){
                    Toast.makeText(this,"密码不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                var tmp=RegisterHolder()
                tmp.id=holder.size+1
                tmp.name=edtName.text.toString()
                tmp.xName=edtXName.text.toString()
                tmp.hospId=edtHospId.text.toString()
                tmp.phone=edtPhone.text.toString()
                tmp.password=edtPassword.text.toString()
                tmp.forget=false
                holder.add(tmp)
                if (xmlWrite()){
                    Toast.makeText(this,"注册成功",Toast.LENGTH_SHORT).show()
                    intent.setClass(this,MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"注册失败",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    //点击此登录
    fun here_Click(v:View?){
        startActivity(intent.setClass(this,StartActivity::class.java))
        //startActivity(Intent(this,StartActivity::class.java))
    }

    private fun xmlWrite():Boolean{
        try{
            var file= File(this.getFilesDir(),"register.xml")
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

                serializer.startTag(null, "xName")
                serializer.text(item.xName)
                serializer.endTag(null, "xName")

                serializer.startTag(null, "hospId")
                serializer.text(item.hospId)
                serializer.endTag(null, "hospId")

                serializer.startTag(null, "phone")
                serializer.text(item.phone)
                serializer.endTag(null, "phone")

                serializer.startTag(null, "password")
                serializer.text(item.password)
                serializer.endTag(null, "password")

                serializer.startTag(null, "forget")
                serializer.text(item.forget.toString())
                serializer.endTag(null, "forget")

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
            var file= File(this.getFilesDir(),"register.xml")
            var fis = FileInputStream(file);

            var parser= Xml.newPullParser()
            parser.setInput(fis, "utf-8")
            var eventType = parser.getEventType()
            var id:String?=null
            var name:String?=null
            var xName:String?=null
            var hospId:String?=null
            var phone:String?=null
            var password:String?=null
            var forget:Boolean?=false
            while (eventType != XmlPullParser.END_DOCUMENT){
                var tagName = parser.getName()
                when (eventType){
                    XmlPullParser.START_TAG ->{
                        if ("users".equals(tagName)) {
                        } else if ("item".equals(tagName)) {
                            id = parser.getAttributeValue(null, "id")
                        } else if ("name".equals(tagName)) {
                            name = parser.nextText()
                        }else if ("xName".equals(tagName)) {
                            xName = parser.nextText()
                        } else if ("hospId".equals(tagName)) {
                            hospId = parser.nextText()
                        }else if ("phone".equals(tagName)) {
                            phone = parser.nextText()
                        }else if ("password".equals(tagName)) {
                            password = parser.nextText()
                        }else if ("forget".equals(forget)) {
                            var vl:String=parser.nextText()
                            if (vl=="true"){
                                forget = true
                            }else{
                                forget = false
                            }
                        }
                    }
                    XmlPullParser.END_TAG ->{
                        if (id!=null){
                            var tmp=RegisterHolder()
                            tmp.id=Integer.parseInt(id)
                            tmp.name=name
                            tmp.xName=xName
                            tmp.hospId=hospId
                            tmp.phone=phone
                            tmp.password=password
                            tmp.forget=forget
                            holder.add(tmp)
                        }
                        id=null
                        name=null
                        xName=null
                        hospId=null
                        phone=null
                        password=null
                        forget=false
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