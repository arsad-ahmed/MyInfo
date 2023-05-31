package com.example.myinfo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinfo.data.FormDataRepository
import com.example.myinfo.model.Status
import com.example.myinfo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormDataViewModel @Inject constructor(private val formDataRepository:FormDataRepository):ViewModel()
{
    private val _formLiveData=MutableLiveData<Resource<Status?>>()
    val formLiveData:LiveData<Resource<Status?>>
        get()=_formLiveData

    fun submitDataToApi(formData:Map<String,String>)
    {
        _formLiveData.postValue(Resource.Loading())
        viewModelScope.launch (Dispatchers.IO){
            _formLiveData.postValue(formDataRepository.submitDataToApi(formData))
        }
    }
}