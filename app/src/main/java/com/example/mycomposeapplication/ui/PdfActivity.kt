package com.example.mycomposeapplication.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.RoundedCorner
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.mycomposeapplication.ui.ui.theme.MyComposeApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.jvm.java

class PdfActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyComposeApplicationTheme {
                ComposeFunction()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComposeFunction() {
    val context = LocalContext.current
    var fileList by remember { mutableStateOf(getAllFiles(context)) }
    val scope = rememberCoroutineScope()

    val fileLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    uris.forEach { uri ->
                        saveFile(context, uri)
                    }
                }
                fileList = getAllFiles(context)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
    ) {
       // if (fileList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().weight(4f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(fileList) { file ->

                    Card(modifier = Modifier.padding(16.dp), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 10.dp), onClick = {
                        val intent = Intent(context, PDFRenderActivity::class.java)
                        intent.putExtra("fileUri",file.absolutePath)
                        context.startActivity(intent)
                    }, colors = CardDefaults.cardColors(containerColor = Color.White)){

                        Box(modifier = Modifier.fillMaxSize(),){

                            Text(text = file.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp).align(Alignment.Center))

                        }


                    }

                }
            }
       // }


      // Box(modifier = Modifier.fillMaxWidth().weight(.5f)){
           Button(
               modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(150.dp),
               onClick = {
                   fileLaunch.launch(arrayOf("application/pdf"))
               }, colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)

           ) {
               Text(text = "Add a PDF", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White)
           }
     //  }

    }
}

fun saveFile(context: Context, uri: Uri) {
    val name = getFileName(context, uri) ?: "pdf_${System.currentTimeMillis()}.pdf"
    val pdfDir = File(context.filesDir, "pdf")
    if (!pdfDir.exists()) {
        pdfDir.mkdirs()
    }
    val dest = File(pdfDir, name)

    context.contentResolver.openInputStream(uri)?.use { input ->
        dest.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun getAllFiles(context: Context): List<File> {
    val pdfDir = File(context.filesDir, "pdf")
    if (!pdfDir.exists()) {
        pdfDir.mkdirs()
    }
    return pdfDir.listFiles()?.toList() ?: emptyList()
}

fun getFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            cursor.getString(index)
        } else {
            null
        }
    }
}

