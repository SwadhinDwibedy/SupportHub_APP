package com.example.supporthub.core.firebase

import com.google.firebase.storage.FirebaseStorage
import kotlin.getValue

object StorageManager {

    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

}