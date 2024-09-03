package com.hul.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SocietyVisitDataViewModel @Inject constructor(private val repository: SocietyVisitDataRepository) : ViewModel() {

    val allSyncData = repository.allVisitDataTable.asLiveData()

    fun insert(societyvisitDataTable: SocietyVisitDataTable) = viewModelScope.launch {
        repository.insert(societyvisitDataTable)
    }

    fun getSyncDataByVisitNumber(wingNumber: String, floor : String, flatNumber : String) = viewModelScope.launch {
        repository.getSyncDataByVisitNumber(wingNumber, floor, flatNumber)
    }

    fun deleteById(id: Int) = viewModelScope.launch {
        repository.deleteById(id)
    }
}

class SocietyVisitDataViewModelFactory(private val repository: SocietyVisitDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocietyVisitDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocietyVisitDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}