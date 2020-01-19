package com.classroom.dialog

import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bright.course.R
import kotlinx.android.synthetic.main.dialog_classroom_netconfig.*

/**
 * Created by kim on 2018/7/15.
 *
 */
class ClassRoomNetConfigDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_classroom_netconfig, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSure.setOnClickListener {
            dismiss()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
//        tvAppManager.setOnClickListener {
//            AppManagerActivity.launch(activity)
//        }
    }
}