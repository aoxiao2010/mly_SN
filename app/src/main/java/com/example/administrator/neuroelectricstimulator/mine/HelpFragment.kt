package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.help.*
import kotlinx.android.synthetic.main.mine_main.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-07
 */
class HelpFragment:Fragment(){
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.help,container,false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        toolbar_help.setNavigationOnClickListener{
            mActivity?.changeFragment(Mine_mainFragment())
        }
    }
}