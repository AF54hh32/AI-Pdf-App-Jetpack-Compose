package com.example.mycomposeapplication.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mycomposeapplication.ui.ui.theme.MyComposeApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    Box(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        if (fileList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(fileList) { file ->
                    Text(text = file.name)
                }
            }
        }

        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                fileLaunch.launch(arrayOf("application/pdf"))
            }
        ) {
            Text(modifier = Modifier.padding(16.dp), text = "Add a PDF")
        }
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

