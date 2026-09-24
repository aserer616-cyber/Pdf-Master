package com.aserer.pdfmaster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.graphics.pdf.PdfRenderer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aserer.pdfmaster.pdf.PdfOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Context) {

    var files by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var status by remember { mutableStateOf("Ready") }
    var selectedPdf by remember { mutableStateOf<Uri?>(null) }

    val scope = rememberCoroutineScope()

    val pick =
        androidx.activity.compose.rememberLauncherForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            files = uris

            if (uris.isNotEmpty()) {
                selectedPdf = uris[0]
                status = "${uris.size} PDF(s) selected"
            } else {
                status = "No PDF selected"
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("PDF Master")
                }
            )
        }
    ) { pad ->

        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "PDF tools",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(status)

            Button(
                onClick = {
                    pick.launch(arrayOf("application/pdf"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select PDFs")
            }

            selectedPdf?.let { uri ->

                Text(
                    "PDF Preview",
                    style = MaterialTheme.typography.titleMedium
                )

                AndroidView(
                    factory = { PdfPreviewView(context) },
                    update = { view ->
                        view.setPdf(uri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            Button(
                onClick = {
                    if (files.size < 2) {
                        status = "Select at least 2 PDFs"
                    } else {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val output =
                                    PdfOperations.output(context, "merged.pdf")

                                PdfOperations.merge(
                                    context,
                                    files,
                                    output
                                )

                                status = "Merged: ${output.name}"
                            } catch (e: Exception) {
                                status = "Error: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Merge PDFs")
            }

            Button(
                onClick = {
                    if (files.size != 1) {
                        status = "Select exactly 1 PDF"
                    } else {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val output =
                                    PdfOperations.output(context, "rotated.pdf")

                                PdfOperations.rotate(
                                    context,
                                    files[0],
                                    90,
                                    output
                                )

                                status = "Rotated: ${output.name}"
                            } catch (e: Exception) {
                                status = "Error: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rotate 90°")
            }

            Button(
                onClick = {
                    if (files.size != 1) {
                        status = "Select exactly 1 PDF"
                    } else {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val output =
                                    PdfOperations.output(
                                        context,
                                        "first-page.pdf"
                                    )

                                PdfOperations.extract(
                                    context,
                                    files[0],
                                    1,
                                    1,
                                    output
                                )

                                status = "Extracted first page: ${output.name}"
                            } catch (e: Exception) {
                                status = "Error: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Extract first page")
            }
        }
    }
}

class PdfPreviewView(context: Context) : android.view.View(context) {

    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var bitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setPdf(uri: Uri) {

        try {
            renderer?.close()
            fileDescriptor?.close()
            bitmap?.recycle()

            fileDescriptor =
                context.contentResolver.openFileDescriptor(uri, "r")

            if (fileDescriptor == null) {
                return
            }

            renderer = PdfRenderer(fileDescriptor!!)

            if (renderer!!.pageCount > 0) {

                val page = renderer!!.openPage(0)

                val width = page.width
                val height = page.height

                bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                )

                bitmap!!.eraseColor(Color.WHITE)

                page.render(
                    bitmap!!,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                page.close()

                invalidate()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmap?.let {

            val destination = Rect(
                0,
                0,
                width,
                height
            )

            canvas.drawBitmap(
                it,
                null,
                destination,
                paint
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        try {
            renderer?.close()
            fileDescriptor?.close()
            bitmap?.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```
