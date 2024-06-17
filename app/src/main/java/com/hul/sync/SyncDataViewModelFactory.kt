package com.hul.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class SyncDataViewModelFactory @Inject constructor(private val repository: SyncDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}