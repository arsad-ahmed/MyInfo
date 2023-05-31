package com.example.myinfo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.myinfo.databinding.FragmentRegistrationBinding
import com.example.myinfo.model.FormDataModel
import com.example.myinfo.util.CACHE_DIR
import com.example.myinfo.util.LOADING_ANNOTATION
import com.example.myinfo.util.Resource
import com.example.myinfo.viewmodel.FormDataViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class RegistrationFragment:Fragment(), PermissionListener
{

    private val formDataViewModel by viewModels<FormDataViewModel>()
    private lateinit var binding:FragmentRegistrationBinding

    private var mImageUri: Uri? = null

    @Inject
    @Named(LOADING_ANNOTATION)
    lateinit var loadingDialog:Dialog

    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View?
    {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_registration,container,false)
        binding.fragment=this
        observeLiveData()
        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        showUserImage()
    }

    private fun observeLiveData()
    {
        formDataViewModel.formLiveData.observe(viewLifecycleOwner){
            when(it)
            {
                is Resource.Loading ->
                {
                    loadingDialog.show()
                }
                is Resource.Success ->
                {
                    loadingDialog.hide()
                    if(it.data?.status == true)
                    {
                        Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                        binding.apply {
                            userProfileImage.setImageResource(R.drawable.ic_profile)
                            nameEt.setText("")
                            genderEt.setText("")
                            ageEt.setText("")
                            phoneEt.setText("")
                            emailEt.setText("")
                            addressEt.setText("")
                            countryEt.setText("")
                            checkBox.isChecked=false
                        }
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "Please try again later", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error ->
                {
                    loadingDialog.hide()
                    Toast.makeText(requireContext(), "Something went wrong,please again later", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun submitUserInfo()
    {
        val name=binding.nameEt.text.toString().trim()
        val gender=binding.genderEt.text.toString().trim()
        val age=binding.ageEt.text.toString().trim()
        val number=binding.phoneEt.text.toString().trim()
        val email=binding.emailEt.text.toString().trim()
        val address=binding.addressEt.text.toString().trim()
        val country=binding.countryEt.text.toString().trim()



        if (name.isEmpty())
        {
            Toast.makeText(requireContext(), "Name can't empty!", Toast.LENGTH_SHORT).show()
            return
        }
        if (gender.isEmpty())
        {
            Toast.makeText(requireContext(), "Gender can't empty!", Toast.LENGTH_SHORT).show()
            return
        }
        if (age.length != 2)
        {
            Toast.makeText(requireContext(), "Please enter valid age", Toast.LENGTH_SHORT).show()
            return
        }
        if (number.length != 10)
        {
            Toast.makeText(requireContext(), "Please enter 10 digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty() and !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(requireContext(), "please enter a valid email address!", Toast.LENGTH_SHORT).show()
            return
        }
        if (address.isEmpty())
        {
            Toast.makeText(requireContext(), "Address can't empty!", Toast.LENGTH_SHORT).show()
            return
        }

        if(country.isEmpty())
        {
            Toast.makeText(requireContext(), "Country can't empty!", Toast.LENGTH_SHORT).show()
            return
        }

        if(mImageUri == null)
        {
            Toast.makeText(requireContext(), "Please add an image to your account!", Toast.LENGTH_SHORT).show()
            return
        }

        if(!binding.checkBox.isChecked)
        {
            Toast.makeText(requireContext(), "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        val formData = FormDataModel(name, gender, age,number,email,address,country,mImageUri.toString())
        val formDataMap = mapOf(
                "Name" to formData.Name,
                "Gender" to formData.Gender,
                "Age" to formData.Age,
                "Phone" to formData.Phone,
                "Email" to formData.Email,
                "Address" to formData.Address,
                "Country" to formData.Country,
                "Photo" to formData.Photo
                               )
        formDataViewModel.submitDataToApi(formDataMap)
    }


    fun setProfilePhoto()
    {
        val popupMenu= PopupMenu(context,binding.userProfileImage)

        popupMenu.menuInflater.inflate(R.menu.profile_photo_menu,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {item ->
            when(item.itemId)
            {
                R.id.galleryMenu -> selectImageFromGallery()
                R.id.cameraMenu -> checkPermissions()

            }
            true
        }
        popupMenu.show()
    }

    private fun selectImageFromGallery()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private val resultLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        mImageUri = result.data?.data
    }


    private fun setImageUsingCamera()
    {
        val intent=Intent()
        intent.action=MediaStore.ACTION_IMAGE_CAPTURE
        cameraResultLauncher.launch(intent)

    }

    private val cameraResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        if(result.resultCode== Activity.RESULT_OK)
        {
            val bitmap=result.data?.extras?.get("data") as Bitmap
            bitmapToImageUri(bitmap)
        }

    }

    private fun bitmapToImageUri(bitmap:Bitmap)
    {

        binding.userProfileImage.setImageBitmap(bitmap)
        val file = File(requireContext().cacheDir, CACHE_DIR)
        file.delete()
        file.createNewFile()
        val fileOutputStream = file.outputStream()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)
        val bytearray = byteArrayOutputStream.toByteArray()
        fileOutputStream.write(bytearray)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.close()

        mImageUri = file.toUri()
    }

    private fun showUserImage()
    {
        if (mImageUri != null)
        {
            binding.userProfileImage.setImageURI(mImageUri)
        }
    }

    private fun checkPermissions()
    {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(p0:PermissionGrantedResponse?)
    {
        setImageUsingCamera()
    }

    override fun onPermissionDenied(p0:PermissionDeniedResponse?)
    {
        Toast.makeText(requireContext(), "Camera permission is required to take picture", Toast.LENGTH_SHORT).show()

    }

    override fun onPermissionRationaleShouldBeShown(p0:PermissionRequest?, p1:PermissionToken?)
    {
        p1?.continuePermissionRequest()

    }

}