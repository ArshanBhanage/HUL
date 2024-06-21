package com.hul.screens.field_auditor_dashboard.ui.mobiliser_visits

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hul.R
import com.hul.data.ProjectInfo
import com.hul.databinding.ItemMobiliserVisitBinding
import com.hul.utils.SUBMITTED
import com.hul.utils.SUB_AGENCY_APPROVED

/**
 * Created by Nitin Chorge on 03-04-2024.
 */
class MobiliserVisitsAdapter(
    private var mList: List<ProjectInfo>,
    private val callback: MobiliserVisitsFragmentInterface,
    val mContext: Context
) : RecyclerView.Adapter<MobiliserVisitsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMobiliserVisitBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMobiliserVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    fun updateVisits(newVisits: List<ProjectInfo>) {
        this.mList = newVisits as ArrayList<ProjectInfo>
        notifyDataSetChanged()
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
                binding.txtSchoolName.text =
                    mContext.getString(R.string.visit) + " " + mList[position].visit_number + " | " + mList[position].location_name + if (mList[position].is_revisit == 1) "(Revisit)" else ""
                binding.txtVisitStatus.text =
                    if (mList[position].visit_status.equals(SUBMITTED, ignoreCase = true)
                        || mList[position].visit_status.equals(
                            SUB_AGENCY_APPROVED,
                            ignoreCase = true
                        )
                    ) "Pending" else "Completed"
                binding.txtVisitStatus.setTextColor(
                    Color.parseColor(
                        if (mList[position].visit_status.equals(
                                SUBMITTED, ignoreCase = true
                            ) || mList[position].visit_status.equals(
                                SUB_AGENCY_APPROVED, ignoreCase = true
                            )
                        ) "#FF9F43" else "#53D28C"
                    )
                )
                binding.chevron.setOnClickListener {
                    callback.redirectToSchoolActivity(this, ArrayList())
                }
                binding.llDirection.setOnClickListener {
                    callback.goToMap(this)
                }
            }
        }
    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return mList.size
    }
}