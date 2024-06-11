package com.hul.storage

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
interface Storage {
    fun setString(key: String, value: String)
    fun getString(key: String): String
    fun setBoolean(key: String, value: Boolean)
    fun getBoolean(key: String): Boolean
    fun setInt(key: String, value: Int)
    fun getInt(key: String): Int
    fun setLong(key: String, value: Long)
    fun getLong(key: String): Long
}
