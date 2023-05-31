package com.example.myinfo.data

import android.util.Log
import com.example.myinfo.model.Status
import com.example.myinfo.util.Resource
import javax.inject.Inject

class FormDataRepository @Inject constructor(private val formApi:FormApi)
{
    suspend fun submitDataToApi(formData:Map<String,String>):Resource<Status?>
    {
        return try
        {
            val result=formApi.submitDataToApi(formData)
            Resource.Success(result.body())
        }
        catch(e:Exception)
        {
            Log.d("MyTag", "submitDataToApi: ${e.message}")
            Resource.Error(e.message.toString())
        }
    }
}