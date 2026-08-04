package com.example.supporthub.core.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

}
