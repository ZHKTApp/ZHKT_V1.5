package com.exam

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import com.bright.course.BaseActivity
import com.bright.course.R
import com.views.PaletteView
import kotlinx.android.synthetic.main.test.*
import me.panavtec.drawableview.DrawableViewConfig


/**
 * Created by kim on 2018/9/11.
 *
 */
class TestPaintActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)


        val config = DrawableViewConfig()

        config.strokeColor = resources.getColor(android.R.color.black)
        config.isShowCanvasBounds = true
        config.strokeWidth = 20.0f
        config.minZoom = 1.0f
        config.maxZoom = 2.0f
        config.canvasHeight = resources.displayMetrics.heightPixels
        config.canvasWidth = resources.displayMetrics.widthPixels
        paintView.setConfig(config)
        paintView.setBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_browser))

        var isClearModel = false
        btnClear.setOnClickListener {
            isClearModel = !isClearModel
            config.isEarse = true
//            paintView.mode = PaletteView.Mode.ERASER
            config.strokeColor = Color.BLACK
            paintView.setConfig(config)
            paintView.setClearModel(isClearModel)

        }
    }
}