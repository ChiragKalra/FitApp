package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class FoodDetailsViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedActivityViewModel::class.java))
            return SharedActivityViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}