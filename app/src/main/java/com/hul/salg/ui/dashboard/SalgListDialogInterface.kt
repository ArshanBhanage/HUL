package com.hul.salg.ui.dashboard

import com.hul.data.District
import com.hul.data.State

interface SalgListDialogInterface {

    fun onDistrictSelect(district : District)
    fun onStateSelect(state : State)
}