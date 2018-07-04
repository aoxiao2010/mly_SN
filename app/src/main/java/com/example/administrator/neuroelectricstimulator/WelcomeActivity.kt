package com.example.administrator.neuroelectricstimulator

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_welcome.*
import android.content.Intent
import android.view.View
import android.widget.LinearLayout


/**
 * author:Chance_Zheng.
 * date:  On 2018-04-28
 */
class WelcomeActivity:AppCompatActivity() {
    private var v: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var y = 100f
        welcome.setOnClickListener { v ->
//            var colorAnimator = ObjectAnimator.ofInt(v,"backgroundColor",0xF000000,0xF00ffff)
//            colorAnimator.setDuration(3000)  //设置动画时间
//            colorAnimator.setEvaluator(ArgbEvaluator())  //设置插值器
//            colorAnimator.repeatCount = ValueAnimator.INFINITE   //设置播放次数无限
//            colorAnimator.repeatMode = ValueAnimator.REVERSE      //设置播放完成之后反转
//            colorAnimator.start()

            var animators = listOf<ObjectAnimator>(
                    ObjectAnimator.ofFloat(v,"rotationX",0f,360f),
                    ObjectAnimator.ofFloat(v,"rotationY",0f,180f),
                    ObjectAnimator.ofFloat(v,"rotation",0f,-90f),
                    ObjectAnimator.ofFloat(v,"translationX",0f,90f),
                    ObjectAnimator.ofFloat(v,"translationY",0f,90f),
                    ObjectAnimator.ofFloat(v,"scaleY",1f,1.5f),
                    ObjectAnimator.ofFloat(v,"scaleX",1f,0.5f),
                    ObjectAnimator.ofFloat(v,"alpha",0f,1f,0.25f,1f)
            )
            val set = AnimatorSet()
            set.playTogether(animators)
            set.setDuration(5*1000).start()
            set.end()
            initAnim()
        }
    }

    private fun initAnimation(){
        var anim = AnimationUtils.loadAnimation(this, R.anim.activity_welcome);
        anim.start()
    }

    private fun initAnim() {
        val intent = Intent(this@WelcomeActivity, StartActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_btn,R.anim.out_btn)
        finish()
    }
}