package com.example.qrcodescanner

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.example.qrcodescanner.ui.theme.QrCodeScannerTheme
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val IMPORT_LAUNCH_INPUT = "*/*"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrCodeScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    fileScannerQrCodeResult.launch(IMPORT_LAUNCH_INPUT)
                }
            }
        }
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