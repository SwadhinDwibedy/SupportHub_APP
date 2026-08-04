package com.example.supporthub.core.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

}