package com.stocked.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Inventory Fragment"
    }
    val text: LiveData<String> = _text
}