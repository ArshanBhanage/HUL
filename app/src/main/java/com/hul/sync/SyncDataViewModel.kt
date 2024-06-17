package com.hul.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SyncDataViewModel @Inject constructor(private val repository: SyncDataRepository) : ViewModel() {
    fun insert(syncData: SyncData) = viewModelScope.launch {
        repository.insert(syncData)
    }

    fun getSyncDataByVisitNumber(visitNumber: Int) = viewModelScope.launch {
        repository.getSyncDataByVisitNumber(visitNumber)
    }

    fun deleteById(id: Int) = viewModelScope.launch {
        repository.deleteById(id)
    }
}