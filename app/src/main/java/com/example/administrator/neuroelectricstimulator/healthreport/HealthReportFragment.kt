package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.healthreport.*
import kotlinx.android.synthetic.main.manualequipment.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-02
 */
class HealthReportFragment:Fragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.healthreport,container,false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        toolbar_report.setNavigationOnClickListener {
            mActivity?.changeFragment(MainFragment())
        }

        more.setOnClickListener {
            mActivity?.changeFragment(MoreFragment())
        }
    }
}