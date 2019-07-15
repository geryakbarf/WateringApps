package com.id.wtf.wateringapps.utils

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class PageAdapter : PagerAdapter {
    lateinit var layouts: IntArray

    lateinit var inflater: LayoutInflater

    lateinit var con: Context

    constructor(layouts: IntArray, con: Context) : super() {
        this.layouts = layouts
        this.con = con
        inflater = con.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }


    override fun isViewFromObject(p0: View, `p1`: Any): Boolean {
        return p0 == `p1`
    }

    override fun getCount(): Int {
        return layouts.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view: View = inflater.inflate(layouts[position], container, false)
        container!!.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

        var view: View = `object` as View
        container!!.removeView(view)

    }
}