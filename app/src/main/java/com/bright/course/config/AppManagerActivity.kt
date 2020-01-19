package com.bright.course.config

import android.content.Context
import android.os.Bundle
import com.bright.course.BaseActivity
import com.bright.course.R
import kotlinx.android.synthetic.main.activity_app_manager.*
import org.jetbrains.anko.intentFor

/**
 * Created by kim on 2018/7/17.
 *
 */
class AppManagerActivity : BaseActivity() {
    companion object {
        fun launch(context: Context) {
            context.startActivity(context.intentFor<AppManagerActivity>())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_manager)
        btnBack.setOnClickListener {
            onBackPressed()
        }

        val adapter = AppManagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        pagerTabStrip.setupWithViewPager(viewPager)

    }
}