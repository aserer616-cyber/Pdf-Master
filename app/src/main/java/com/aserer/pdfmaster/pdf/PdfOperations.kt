package com.aserer.pdfmaster.pdf

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File

object PdfOperations {
 private var ready=false
 private fun init(c:Context){if(!ready){PDFBoxResourceLoader.init(c);ready=true}}
 fun output(c:Context,name:String):File{val d=File(c.getExternalFilesDir(null),"PDFMaster");d.mkdirs();return File(d,name)}
 fun merge(c:Context,uris:List<Uri>,out:File){init(c);val r=PDDocument();try{for(u in uris)c.contentResolver.openInputStream(u).use{PDDocument.load(requireNotNull(it)).use{s->for(p in s.pages)r.importPage(p)}};r.save(out)}finally{r.close()}}
 fun rotate(c:Context,u:Uri,degrees:Int,out:File){init(c);c.contentResolver.openInputStream(u).use{PDDocument.load(requireNotNull(it)).use{d->for(p in d.pages)p.rotation=(p.rotation+degrees)%360;d.save(out)}}}
 fun extract(c:Context,u:Uri,from:Int,to:Int,out:File){init(c);c.contentResolver.openInputStream(u).use{PDDocument.load(requireNotNull(it)).use{s->PDDocument().use{r->for(i in (from-1)..minOf(to-1,s.numberOfPages-1))r.importPage(s.getPage(i));r.save(out)}}}}
}
