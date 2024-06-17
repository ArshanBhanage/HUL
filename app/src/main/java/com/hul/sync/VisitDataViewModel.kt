package com.hul.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class VisitDataViewModel @Inject constructor(private val repository: VisitDataRepository) : ViewModel() {

    val allSyncData = repository.allVisitDataTable.asLiveData()

    fun insert(visitDataTable: VisitDataTable) = viewModelScope.launch {
        repository.insert(visitDataTable)
    }

    fun getSyncDataByVisitNumber(visitNumber: Int, uDiceCode : String) = viewModelScope.launch {
        repository.getSyncDataByVisitNumber(visitNumber, uDiceCode)
    }

    fun deleteById(id: Int) = viewModelScope.launch {
        repository.deleteById(id)
    }
}

class VisitDataViewModelFactory(private val repository: VisitDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}