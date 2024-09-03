package com.hul.salg.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hul.databinding.PerfItemBinding

/**
 * Created by Nitin Chorge on 03-04-2024.
 */
class SalgMyPerfAdapter(
    private val mList: ArrayList<String>,
    private val perfSelectedposition : Int,
    private val perfInterface: SalgPerfInterface,
    val mContext: Context
) : RecyclerView.Adapter<SalgMyPerfAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: PerfItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PerfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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
                binding.locationPosition.text = mList[position]
                if(position == perfSelectedposition)
                {
                    binding.rad.isChecked = true
                }
                binding.locationPosition.setOnClickListener{
                    perfInterface.onSelected(position = position)
                }

                binding.rad.setOnClickListener{
                    perfInterface.onSelected(position = position)
                }
            }
        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return mList.size
    }
}