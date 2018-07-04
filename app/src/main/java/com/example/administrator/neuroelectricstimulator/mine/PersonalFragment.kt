package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.help.*
import kotlinx.android.synthetic.main.personal.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class PersonalFragment:Fragment(){
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.personal,container,false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        toolbar_personal.setNavigationOnClickListener{
            mActivity?.changeFragment(Mine_mainFragment())
        }

        my_picure.setOnClickListener {
            Toast.makeText(activity,"上传照片",Toast.LENGTH_SHORT).show()
        }
    }
}