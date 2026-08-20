// File: app/src/main/java/com/example/brainbox/ui/screens/PdfViewerScreen.kt
package com.example.brainbox.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val TAG = "PdfViewerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(navController: NavController, pdfUrl: String) {
    val context = LocalContext.current
    val uri = remember(pdfUrl) { Uri.parse(pdfUrl) }
    val title = remember(pdfUrl) {
        uri.lastPathSegment?.split("/")?.lastOrNull() ?: "PDF Viewer"
    }

    // State to hold the downloaded file
    var pdfFile by remember { mutableStateOf<File?>(null) }
    // State to show loading progress
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pdfUrl) {
        isLoading = true
        coroutineScope.launch {
            try {
                // Download the file in a background coroutine
                val file = downloadPdfFile(context, pdfUrl)
                withContext(Dispatchers.Main) {
                    pdfFile = file
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading PDF", e)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    // Handle download error, e.g., show a toast or error message
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Show a loading indicator while the PDF is downloading
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (pdfFile != null) {
                // Once downloaded, use the PDFView to display the local file
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PDFView(context, null).apply {
                            fromFile(pdfFile) // Use the downloaded file
                                .defaultPage(0)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .scrollHandle(DefaultScrollHandle(context))
                                .spacing(10)
                                .load()
                        }
                    },
                    update = { pdfView ->
                        // This block can be used for updates if the file changes
                    }
                )
            } else {
                // Show an error message if the download failed
                Text("Failed to load PDF", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }
    }
}

/**
 * Downloads a PDF file from a URL to a temporary local file.
 * This function should be called from a background thread (e.g., a coroutine).
 */
suspend fun downloadPdfFile(context: android.content.Context, urlString: String): File =
    withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        val file = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    }

@Preview(showBackground = true)
@Composable
fun PdfViewerScreenPreview() {
    PdfViewerScreen(navController = rememberNavController(), pdfUrl = "https://www.example.com/sample.pdf")
}