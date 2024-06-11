package com.hul.dashboard.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hul.databinding.LocationViewDesignBinding
import com.hul.databinding.LocationVisitedDesignBinding

/**
 * Created by Nitin Chorge on 03-04-2024.
 */
class VisitedLocationAdapter(private val mList: ArrayList<String>, private val dashboardInterface: DashboardFragmentInterface, val mContext : Context) : RecyclerView.Adapter<VisitedLocationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LocationVisitedDesignBinding) : RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LocationVisitedDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    // bind the items with each item
    // of the list languageList
    // which than will be
    // shown in recycler view
    // to keep it simple we are
    // not setting any image data to view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mList[position]){
               binding.locationPosition.text = mList[position]
            }
        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return mList.size
    }
}