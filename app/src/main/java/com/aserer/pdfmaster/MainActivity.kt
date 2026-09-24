package com.aserer.pdfmaster

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aserer.pdfmaster.pdf.PdfOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App(this) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(context: Context) {
 var files by remember { mutableStateOf<List<Uri>>(emptyList()) }
 var status by remember { mutableStateOf("Ready") }
 val scope=rememberCoroutineScope()
 val pick=androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){ u->files=u;status="${u.size} PDF(s) selected"}
 Scaffold(topBar={TopAppBar(title={Text("PDF Master")})}){ pad->
  Column(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Text("PDF tools",style=MaterialTheme.typography.headlineSmall)
   Text(status)
   Button({pick.launch(arrayOf("application/pdf"))},Modifier.fillMaxWidth()){Text("Select PDFs")}
   Button({
    if(files.size<2){status="Select at least 2 PDFs"} else scope.launch(Dispatchers.IO){try{val o=PdfOperations.output(context,"merged.pdf");PdfOperations.merge(context,files,o);status="Merged: ${o.name}"}catch(e:Exception){status="Error: ${e.message}"}}
   },Modifier.fillMaxWidth()){Text("Merge PDFs")}
   Button({
    if(files.size!=1){status="Select exactly 1 PDF"} else scope.launch(Dispatchers.IO){try{val o=PdfOperations.output(context,"rotated.pdf");PdfOperations.rotate(context,files[0],90,o);status="Rotated: ${o.name}"}catch(e:Exception){status="Error: ${e.message}"}}
   },Modifier.fillMaxWidth()){Text("Rotate 90°")}
   Button({
    if(files.size!=1){status="Select exactly 1 PDF"} else scope.launch(Dispatchers.IO){try{val o=PdfOperations.output(context,"first-page.pdf");PdfOperations.extract(context,files[0],1,1,o);status="Extracted first page: ${o.name}"}catch(e:Exception){status="Error: ${e.message}"}}
   },Modifier.fillMaxWidth()){Text("Extract first page")}
   Text("Cloud build is configured in GitHub Actions. More Online2PDF-style modules can be added to this project.")
  }
 }
}
