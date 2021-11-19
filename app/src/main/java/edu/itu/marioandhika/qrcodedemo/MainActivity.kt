package edu.itu.marioandhika.qrcodedemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import edu.itu.marioandhika.qrcodedemo.ui.theme.QRCodeDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRCodeDemoTheme {
                Scaffold(
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = { FloatingActionButton(onClick = { startActivity(Intent(this, CameraActivity::class.java)) }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    } }
                ) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        val text = remember{ mutableStateOf("") }
                        val bitmap = remember{ mutableStateOf(generateQRCode("")) }
                        TextField(value = text.value, onValueChange = {
                            text.value = it
                        })
                        Button(onClick = {
                            bitmap.value = generateQRCode(text.value)
                        }){
                            Text("Generate QR Code")
                        }
                        Image(bitmap = bitmap.value.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        if (text.isEmpty()) {
            return Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
        }

        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("My Tag", "generateQRCode: ${e.message}")
        }
        return bitmap
    }
}
