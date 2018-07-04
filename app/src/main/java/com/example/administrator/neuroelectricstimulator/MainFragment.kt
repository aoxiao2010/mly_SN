package com.example.administrator.neuroelectricstimulator


import android.os.Bundle
import android.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-17
 */
class MainFragment:Fragment() {
    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.toolbar,container,false)
        return rootView
    }
}