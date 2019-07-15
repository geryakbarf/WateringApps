package com.id.wtf.wateringapps.models

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.id.wtf.wateringapps.utils.PageAdapter
import com.id.wtf.wateringapps.R
import com.id.wtf.wateringapps.utils.Session
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var session: Session
    private lateinit var mPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView>
    private lateinit var mAdapter: PageAdapter
    private var layouts: IntArray = intArrayOf(
        R.layout.slide1,
        R.layout.slide2,
        R.layout.slide3
    )

    //button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 19) {

            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        } else {

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        }
        session = Session(applicationContext)
        if(session.getValueString("intro") == null){
            mPager = findViewById<ViewPager>(R.id.pager)
            mAdapter = PageAdapter(layouts, this)
            mPager.adapter = mAdapter
            // button skip and next
            dotsLayout = findViewById<LinearLayout>(R.id.dots)
            btn_next.setOnClickListener(this)
            btn_skip.setOnClickListener(this)
            createDots(0)
            mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {

                }

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

                }

                override fun onPageSelected(p0: Int) {
                    createDots(p0)

                    if (p0 == layouts.size - 1) {
                        btn_next.text = "Mulai"
                        btn_skip.visibility = View.INVISIBLE
                    } else {
                        btn_next.text = "Lanjut"
                        btn_skip.visibility = View.VISIBLE
                    }

                }

            })

        }else {
            val intent = Intent(applicationContext,MenuActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


    private fun loadNextSlide() {
        var nextSlide: Int = mPager.currentItem + 1

        if (nextSlide < layouts.size) {
            mPager.currentItem = nextSlide
        } else {
            val intent = Intent(applicationContext, MenuActivity::class.java)
            startActivity(intent)
            finish()
            session.save("intro", "ada")
        }
    }

    fun createDots(position: Int) {
        if (dotsLayout != null) {
            dotsLayout.removeAllViews()
        }

        dots = Array(layouts.size, { i -> ImageView(this) })

        for (i in 0..layouts.size - 1) {

            dots[i] = ImageView(this)

            if (i == position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dots))
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_dots))
            }

            var params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(4, 0, 4, 0)
            dotsLayout.addView(dots[i], params)


        }

    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_skip -> {


            }

            R.id.btn_next -> {
                loadNextSlide()
            }

        }

    }
}
