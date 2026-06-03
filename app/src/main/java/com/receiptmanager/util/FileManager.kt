package com.receiptmanager.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

object FileManager {

    private val CHARS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890"

    // mirrors getRandomString()
    fun getRandomString(length: Int): String {
        return (1..length)
            .map { CHARS[Random.nextInt(CHARS.length)] }
            .joinToString("")
    }

    // mirrors FileNamer() — avoid collisions
    fun fileNamer(context: Context, originalName: String): String {
        val file = File(context.filesDir, originalName)
        return if (file.exists()) {
            "${getRandomString(10)}_$originalName"
        } else {
            originalName
        }
    }

    // mirrors SaveFilePermenately() — copies picked file into app's private files dir
    fun saveFilePermanently(context: Context, uri: Uri, originalName: String): String {
        val safeFileName = fileNamer(context, originalName)
        val destFile = File(context.filesDir, safeFileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return safeFileName  // return just the filename (not full path), same as Dart
    }

    // mirrors SaveImagePermenately() — copies camera image into app's private files dir
    fun saveImagePermanently(context: Context, sourceFile: File): String {
        val fileName = sourceFile.name
        val safeFileName = fileNamer(context, fileName)
        val destFile = File(context.filesDir, safeFileName)
        sourceFile.copyTo(destFile, overwrite = true)
        return safeFileName
    }

    // mirrors DeleteFile() — deletes a file stored by its short name
    fun deleteFile(context: Context, fileName: String): Int {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
            2
        } else {
            0
        }
    }

    // mirrors ReturnFilePath() — returns the full absolute path for a stored filename
    fun returnFilePath(context: Context, fileName: String): String {
        return File(context.filesDir, fileName).absolutePath
    }

    // mirrors DeleteCache() — clears cache and temp dirs
    fun deleteCache(context: Context) {
        context.cacheDir.deleteRecursively()
        context.getExternalFilesDir(null)?.deleteRecursively()
    }

    // mirrors MimeType()
    fun getMimeType(filePath: String): String? {
        val extension = filePath.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }

    // Returns a FileProvider URI for sharing
    fun getShareableUri(context: Context, fileName: String): Uri {
        val file = File(context.filesDir, fileName)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // mirrors IconsPredict() — returns icon resource
    fun getIconRes(mimeType: String?): Int {
        return when {
            mimeType == "application/pdf" -> android.R.drawable.ic_menu_agenda
            mimeType?.startsWith("image/") == true -> android.R.drawable.ic_menu_gallery
            mimeType == "application/msword" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            -> android.R.drawable.ic_menu_edit
            else -> android.R.drawable.ic_menu_gallery
        }
    }

    // mirrors IconsColor() — returns color int
    fun getIconColor(mimeType: String?): Int {
        return when {
            mimeType == "application/pdf" -> android.graphics.Color.RED
            mimeType?.startsWith("image/") == true -> android.graphics.Color.BLACK
            mimeType?.contains("word") == true -> android.graphics.Color.BLUE
            else -> android.graphics.Color.DKGRAY
        }
    }
}
