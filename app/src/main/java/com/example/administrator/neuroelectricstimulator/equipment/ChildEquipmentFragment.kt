package com.example.administrator.neuroelectricstimulator

import android.app.Fragment
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.administrator.neuroelectricstimulator.R.id.*
import kotlinx.android.synthetic.main.childequipment.*
import kotlinx.android.synthetic.main.equipmentinformation.*

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-22
 */
class ChildEquipmentFragment:Fragment() {
    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater!!.inflate(R.layout.childequipment,container,false)
        return rootView
    }
}