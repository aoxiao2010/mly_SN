package com.example.administrator.neuroelectricstimulator

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import kotlin.concurrent.fixedRateTimer
import android.view.ViewGroup
import android.os.Bundle



/**
 * author:Chance_Zheng.
 * date:  On 2018-06-25
 */

class FragmentAdapter(private val mContext: Context, fm: FragmentManager,private  val fragments:ArrayList<Fragment>,private val vId:ArrayList<Int>) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int):Fragment {
        var id:Int=0
        //var pNum:Int=0    pNum=i
        for(i in 0 until vId.size){
            if(i===position){
                id=vId[i]
            }
        }
        val bundle = Bundle()
        bundle.putInt("vId",id)
        bundle.putInt("vPosition",position)
        return Fragment.instantiate(mContext, FirstFragment::class.java!!.name, bundle)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragments[position] = fragment
        return fragment
    }

}
