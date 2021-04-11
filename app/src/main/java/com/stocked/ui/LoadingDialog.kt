package com.stocked.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import com.stocked.R

class LoadingDialog internal constructor(private val activity: Activity) {

    private var alertDialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    fun startLoadingDialog() {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.progressbar_dialog, null))
        builder.setCancelable(false)
        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun dismissDialog() {
        alertDialog!!.dismiss()
    }
}