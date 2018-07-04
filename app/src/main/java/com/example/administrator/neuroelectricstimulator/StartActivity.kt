package com.example.administrator.neuroelectricstimulator

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Xml
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_start.*
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class StartActivity : AppCompatActivity() {
    private var holder = ArrayList<RegisterHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE)   //去掉标题栏
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN)  //设置全屏

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        xmlRead()
        var ePhone =findViewById<EditText>(R.id.edtStartPhone)as EditText
        var ePwd =findViewById<EditText>(R.id.edtStartPwd)as EditText
        var eFgt =findViewById<CheckBox>(R.id.forgetCheck)as CheckBox
        for (item in holder!!.iterator()){
            if(item.forget==false){
                ePhone.setText(item.phone)
                ePwd.setText(item.password)
                eFgt.isChecked=true
                break
            }
        }
    }


    //登陆
    fun login_Click(v: View?){
        when(v?.id){
            R.id.login_btn ->{
//                var intent = Intent()
//                intent.setClass(this,MainActivity::class.java)
//                startActivity(intent)


                if ( edtStartPhone.text.toString()==""){
                    Toast.makeText(this,"名字不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                if (edtStartPwd.text.toString()==""){
                    Toast.makeText(this,"姓不能为空！",Toast.LENGTH_SHORT).show()
                    return
                }
                var bool:Boolean=false
                for (item in holder!!.iterator()){
                    item.forget=false
                }
                for (item in holder!!.iterator()){
                    if (item.phone==edtStartPhone.text.toString() && item.password==edtStartPwd.text.toString()){
                        bool=true
                        item.forget=true
                        break
                    }
                }
                if (bool==true){
                    if (forgetCheck.isChecked==true){
                        xmlWrite()
                    }
                    var intent = Intent()
                    intent.setClass(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"登陆成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"登陆邮箱、手机或者密码错误，请重新输入！",Toast.LENGTH_SHORT).show();
                }

            }


        }
    }

    //注册账号
    fun image1_Click(v: View?){
        var intent = Intent()
        intent.setClass(this,RegisterActivity::class.java)
        startActivity(intent)
    }

    //忘记密码
    fun forgot_Click(v:View?){
        var intent = Intent()
        intent.setClass(this,ForgotPasswordActivity::class.java)
        startActivity(intent)
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
