package com.example.instademo.utils

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

fun uploadImage(uri: Uri, folderName: String,callback:(String?)->Unit){
    var ImageUrl: String? =null
    FirebaseStorage.getInstance().getReference(folderName).child(UUID.randomUUID().toString())
        .putFile(uri)
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                ImageUrl = it.toString()

                callback(ImageUrl!!)

            }


        }
}

fun uploadVideo(
    uri: Uri,
    folderName: String,
    context: Context,
    pd: ProgressDialog,
    callback: (String?) -> Unit
) {
    pd.setTitle("Uploading...")
    pd.show() // Ensure the ProgressDialog is shown
    val storageReference = FirebaseStorage.getInstance().getReference(folderName)
        .child(UUID.randomUUID().toString())

    storageReference.putFile(uri)
        .addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val videoUrl = uri.toString()
                callback(videoUrl)
                pd.dismiss()
            }.addOnFailureListener {
                pd.dismiss()
                Toast.makeText(context, "Failed to get video URL", Toast.LENGTH_LONG).show()
                callback(null)
            }
        }
        .addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            pd.setMessage("Uploaded ${progress.toInt()}%")
        }
        .addOnFailureListener {
            pd.dismiss()
            Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
            callback(null)
        }
}

