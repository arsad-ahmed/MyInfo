package com.example.myinfo.data


import com.example.myinfo.model.FormDataModel
import com.example.myinfo.model.Status
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FormApi
{
    @POST("index.php")
    @FormUrlEncoded
    suspend fun submitDataToApi(@FieldMap formData:Map<String,String>):Response<Status?>
}