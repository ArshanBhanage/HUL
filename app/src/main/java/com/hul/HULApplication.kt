package com.hul

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import android.app.Application
import android.util.Log
import com.hul.di.AppComponent
import com.hul.di.DaggerAppComponent
import com.hul.sync.HulDatabase
import com.hul.sync.VisitDataRepository

open class HULApplication : Application() {

    val appComponent: AppComponent by lazy {
        // Creates an instance of AppComponent using its Factory constructor
        // We pass the applicationContext that will be used as Context in the graph
        DaggerAppComponent.factory().create(applicationContext)
    }



    companion object {
        const val IS_DEBUG = true
        const val TAG = "HULApplication"
        fun printLog(tag: String?, message: String?) {
            if (IS_DEBUG && tag != null && message != null) Log.d(tag, message)
        }

        // to prevent crash caused by null messages from api response(bad response or due to limited connectivity)
        fun printError(tag: String?, message: String?) {
            if (IS_DEBUG && tag != null && message != null) Log.e(tag, message)
        }

        fun printInfo(tag: String?, message: String?) {
            if (IS_DEBUG && tag != null && message != null) Log.i(tag, message)
        }
    }


}


