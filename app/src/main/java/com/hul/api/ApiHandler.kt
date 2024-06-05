package com.hul.api

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
interface ApiHandler {

    fun onApiSuccess(o: String?, objectType: Int)
    fun onApiError(message: String?)
}