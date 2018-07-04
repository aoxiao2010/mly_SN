package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.mine_main.*
import kotlinx.android.synthetic.main.treatment_fragment.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-02
 */
class Mine_mainFragment: Fragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.mine_main,container,false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        toolbar_mine.setNavigationOnClickListener{
            mActivity?.changeFragment(MainFragment())
        }

        personalID.setOnClickListener{
            mActivity?.changeFragment(PersonalFragment())
        }

        helpID.setOnClickListener{
            mActivity?.changeFragment(HelpFragment())
        }

        exit.setOnClickListener {
            var intent= Intent()
            startActivity(intent.setClass(activity,StartActivity::class.java))
        }
    }
}