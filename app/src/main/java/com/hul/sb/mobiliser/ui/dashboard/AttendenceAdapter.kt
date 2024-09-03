package com.hul.sb.mobiliser.ui.dashboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.hul.R
import com.hul.data.Attendencemodel
import com.hul.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendenceAdapter(private val context: Context, private val dataSource: List<Attendencemodel>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.attendence_view, parent, false)
            viewHolder = ViewHolder()
            viewHolder.imageView = view.findViewById(R.id.present) as ImageView
            viewHolder.date = view.findViewById(R.id.date) as TextView
            viewHolder.month = view.findViewById(R.id.month) as TextView
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val item = getItem(position) as Attendencemodel

        if(item.date != null) {
            val timeStamp = TimeUtils.getTimeStampFromDateString(item.date!!)
            val month = capitalizeWords(TimeUtils.getMonthFromTimestamp(timeStamp))

            viewHolder.date.text = formatDateToDayWithSuffix(stringToDate(item.date!!)!!)
            viewHolder.month.text = month

        }

        if(item.present != null)
        {
            viewHolder.imageView.setImageDrawable( context.resources.getDrawable(R.mipmap.present))
        }
        else{
            viewHolder.imageView.setImageDrawable( context.resources.getDrawable(R.mipmap.absent))
        }

        return view
    }

    private class ViewHolder {
        lateinit var imageView: ImageView
        lateinit var date: TextView
        lateinit var month: TextView
    }

    fun stringToDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun formatDateToDayWithSuffix(date: Date): String {
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val day = dayFormat.format(date).toInt()
        val suffix = getDayOfMonthSuffix(day)
        return day.toString() + suffix
    }

    fun getDayOfMonthSuffix(day: Int): String {
        return if (day in 11..13) {
            "th"
        } else when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

}


