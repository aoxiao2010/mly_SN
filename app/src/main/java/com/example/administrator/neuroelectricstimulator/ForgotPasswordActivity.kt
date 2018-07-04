package com.example.administrator.neuroelectricstimulator

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

/**
 * author:Chance_Zheng.
 * date:  On 2018-04-27
 */
class ForgotPasswordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)
    }

    fun submit_Click(v: View?){
        when(v?.id){
            R.id.submit ->
                Toast.makeText(this,"提交成功",Toast.LENGTH_LONG).show()
        }
        startActivity(intent.setClass(this,StartActivity::class.java))
    }
}