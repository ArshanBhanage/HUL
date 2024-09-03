package com.hul.salg.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hul.data.District
import com.hul.databinding.LocationViewDesignBinding

class SalgDistrictsAdapter(
    private val mList: ArrayList<District>,
    private val callback: SalgListDialogInterface,
    val mContext: Context
) : RecyclerView.Adapter<SalgDistrictsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LocationViewDesignBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LocationViewDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    // bind the items with each item
    // of the list languageList
    // which than will be
    // shown in recycler view
    // to keep it simple we are
    // not setting any image data to view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(mList[position]) {
                binding.locationPosition.text = mList[position].area_name
                binding.visit.text = ""
                binding.root.setOnClickListener {
                    callback.onDistrictSelect(this)
                }
                binding.location.visibility= View.GONE
                binding.chevron.visibility = View.GONE
            }
        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return mList.size
    }
}