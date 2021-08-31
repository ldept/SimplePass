package com.ldept.simplepass.util

import android.content.Context
import android.net.ConnectivityManager

object Network {

    fun isConnectedToNetwork(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null
    }
}