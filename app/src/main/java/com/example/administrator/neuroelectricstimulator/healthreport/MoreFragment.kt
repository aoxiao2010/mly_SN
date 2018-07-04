package com.example.administrator.neuroelectricstimulator

import android.app.DatePickerDialog
import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import kotlinx.android.synthetic.main.healthreport.*
import kotlinx.android.synthetic.main.more.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-02
 */
class MoreFragment:Fragment() {
    private var rootView: View? = null
    private var mActivity: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.more,container,false)
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mActivity = activity as MainActivity
        cancel.setOnClickListener {
            mActivity?.changeFragment(HealthReportFragment())
        }

        sure.setOnClickListener {
            mActivity?.changeFragment(HealthReportFragment())
        }
    }

//        //日历控件
//        DatePickerDialog dp = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker datePicker, int iyear, int monthOfYear, int dayOfMonth) {
//                long maxDate = datePicker.getMaxDate();//日历最大能设置的时间的毫秒值
//                int year = datePicker.getYear();//年
//                int month = datePicker.getMonth();//月-1
//                int dayOfMonth1 = datePicker.getDayOfMonth();//日*
//                //iyear:年，monthOfYear:月-1，dayOfMonth:日
//                Toast.makeText(getApplicationContext(), iyear +":"+ (monthOfYear+1)+":"+dayOfMonth , Toast.LENGTH_LONG).show();
//            }
//        }, 2013, 2, 1);//2013:初始年份，2：初始月份-1 ，1：初始日期
//        dp.show();
//        }
}