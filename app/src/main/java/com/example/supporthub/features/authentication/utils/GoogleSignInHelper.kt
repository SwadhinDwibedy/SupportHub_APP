package com.example.supporthub.features.authentication.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.supporthub.core.firebase.GoogleAuth
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

class GoogleSignInHelper(
    private val context: Context
) {

    private val credentialManager =
        CredentialManager.create(context)

    suspend fun signIn(): String? {

        return try {

            val googleIdOption =
                GetGoogleIdOption.Builder()
                    .setServerClientId(
                        GoogleAuth.WEB_CLIENT_ID
                    )
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

            val request =
                GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            val result =
                credentialManager.getCredential(
                    context = context,
                    request = request
                )

            val credential = result.credential

            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            googleCredential.idToken

        } catch (_: GoogleIdTokenParsingException) {

            null

        } catch (_: Exception) {

            null

        }

    }

}