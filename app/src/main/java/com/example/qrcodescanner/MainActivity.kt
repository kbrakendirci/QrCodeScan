package com.example.qrcodescanner

import android.R
import android.R.attr.data
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.qrcodescanner.FileUtil.getRotatedImage
import com.example.qrcodescanner.FileUtil.toBitmap
import com.example.qrcodescanner.ui.theme.QrCodeScannerTheme
import com.google.zxing.Result
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class MainActivity : ComponentActivity() {
    companion object {
        private const val IMPORT_LAUNCH_INPUT = "*/*"
    }


    private lateinit var imagePath: String
    private var qrResult: Result? = null
    val qrArrayList: ArrayList<String> = arrayListOf("emre.com", "kübra.com", "btc.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrCodeScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //fileScannerQrCodeResult.launch(IMPORT_LAUNCH_INPUT)
                    //galleryChoose(arrayListOf("emre.com","kübra.com","btc.com"))
                    //takeAPhotoResultLauncher
                    dispatchTakePictureIntent()
                }
            }
        }
    }

    fun galleryChoose(qrList: ArrayList<String>): Boolean {
        //fileScannerQrCodeResult.launch(IMPORT_LAUNCH_INPUT)
        //Log.i("RESULT", "result3: ${qrResult?.text}")
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
                        galleryChoose(qrArrayList)
                        if (result != null) {
                            withContext(Dispatchers.Main) {
                                Log.i("RESULT", "result: ${result.text}")
                            }
                        } else {
                            val message = ""

                        }
                    } catch (e: Exception) {
                        val message = ""
                    }
                } else {

                }
            }
        }

    private val takeAPhotoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FileUtil.getImage(imagePath).toBitmap()?.getRotatedImage(imagePath)?.let { image ->
                    if (imagePath== null) return@registerForActivityResult
                    val contentResolver = contentResolver ?: return@registerForActivityResult
                    val takePhotoUri=imagePath.toUri()
                    lifecycleScope.launch {
                        if (FileScanner.validContentType(contentResolver,takePhotoUri)) {
                            try {
                                var takePhotoUri=imagePath.toUri()
                                val qrCodeFromFileScanner = FileScanner(contentResolver, QRCodeReader())
                                val result = qrCodeFromFileScanner.scan(takePhotoUri)
                                Log.i("RESULT", "result: $result, data: $ takePhotoUri")
                                qrResult = result
                                galleryChoose(qrArrayList)
                                if (result != null) {
                                    withContext(Dispatchers.Main) {
                                        Log.i("RESULT", "result: ${result.text}")
                                    }
                                } else {
                                    val message = ""

                                }
                            } catch (e: Exception) {
                                val message = ""
                            }
                        } else {

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


}




@Throws
fun File.createBitmap(): Bitmap {
    val options = BitmapFactory.Options()
    options.inSampleSize = 2
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    val bitmap = BitmapFactory.decodeFile(absolutePath, options)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val orientation = ExifInterface(this).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        bitmap.rotateBitmap(orientation)
    } else {
        bitmap
    }
}


@Throws
fun Bitmap.rotateBitmap(orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        else -> matrix.postRotate(0F)
    }
    return Bitmap.createBitmap(
        this, 0, 0, width, height,
        matrix, true
    )
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QrCodeScannerTheme {
        Greeting("Android")
    }
}