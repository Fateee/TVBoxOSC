package com.common.network.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetUtils {
    var app: Application? = null

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    @JvmStatic
    fun isNetworkConnected(): Boolean {
        val mConnectivityManager = app?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = mConnectivityManager.activeNetworkInfo
        return mNetworkInfo?.isAvailable ?: false
    }
}