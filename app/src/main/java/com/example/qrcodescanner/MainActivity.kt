package com.example.qrcodescanner

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.qrcodescanner.FileUtil.getRotatedImage
import com.example.qrcodescanner.FileUtil.toBitmap
import com.example.qrcodescanner.ui.theme.QrCodeScannerTheme
import com.google.zxing.Result
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {
    companion object {
        private const val IMPORT_LAUNCH_INPUT = "*/*"
    }


    private lateinit var imagePath: String
    private var qrResult: Result? = null
    private val qrArrayList: ArrayList<String> = arrayListOf("emre.com", "kübra.com", "btc.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrCodeScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var takeAPhoto by remember {
                        mutableStateOf(false)
                    }
                    var pickAPhoto by remember {
                        mutableStateOf(false)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Button(
                                onClick = { takeAPhoto = true },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "Fotoğraf Çek")
                            }
                            Button(
                                onClick = { pickAPhoto = true },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "Fotoğraf Seç")
                            }
                        }
                    }
                    if (takeAPhoto) dispatchTakePictureIntent()
                    if (pickAPhoto) fileScannerQrCodeResult.launch(IMPORT_LAUNCH_INPUT)
                }
            }
        }
    }

    //Servisten Gelen Qr code listesi fonksiyona parametre olarak alınıcak.
    // Qr code eğer servisten gelen liste içerisinde bulunuyorsa true bulunmuyorsa false dönücek.
    fun verificationQrCode(qrList: ArrayList<String>): Boolean {
        return qrList.contains(qrResult?.text)

    }

    private val fileScannerQrCodeResult = registerForActivityResult(ActivityResultContracts.GetContent()) { data ->
            if (data == null) return@registerForActivityResult
            val contentResolver = contentResolver ?: return@registerForActivityResult
            lifecycleScope.launch {
                if (FileScanner.validContentType(contentResolver, data)) {
                    try {
                        val qrCodeFromFileScanner = FileScanner(contentResolver, QRCodeReader())
                        val result = qrCodeFromFileScanner.scan(data)
                        Log.i("RESULT", "result: $result, data: $data")
                        qrResult = result
                        if (verificationQrCode(qrArrayList)) {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.qr_code_succes,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else failedMessage()

                    } catch (e: Exception) {
                        failedMessage()
                    }
                } else {
                    failedMessage()
                }
            }
        }


    private val takeAPhotoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FileUtil.getImage(imagePath).toBitmap()?.getRotatedImage(imagePath)?.let { image ->
                    val contentResolver = contentResolver ?: return@registerForActivityResult
                    val takePhotoUri = imagePath.toUri()
                    lifecycleScope.launch {
                        if (FileScanner.validContentType(contentResolver, takePhotoUri)) {
                            try {
                                val takePhotoUri = imagePath.toUri()
                                val qrCodeFromFileScanner =
                                    FileScanner(contentResolver, QRCodeReader())
                                val result = qrCodeFromFileScanner.scan(takePhotoUri)
                                Log.i("RESULT", "result: $result, data: $ takePhotoUri")
                                qrResult = result
                                verificationQrCode(qrArrayList)
                                failedMessage()
                            } catch (e: Exception) {
                                failedMessage()
                            }
                        } else {
                            failedMessage()
                        }
                    }
                }
            }
        }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.also {
            val photoFile: File? = try {
                FileUtil.createImageFile(this)
            } catch (ex: IOException) {
                null
            }
            photoFile?.let { file ->
                imagePath = file.absolutePath
                val authority = BuildConfig.APPLICATION_ID + ".provider"
                val uri: Uri = FileProvider.getUriForFile(this, authority, file)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                //takePhotoUri=uri
                takeAPhotoResultLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun failedMessage() {
        Toast.makeText(
            this@MainActivity,
            R.string.qr_code_failed,
            Toast.LENGTH_SHORT
        ).show()
    }

}



