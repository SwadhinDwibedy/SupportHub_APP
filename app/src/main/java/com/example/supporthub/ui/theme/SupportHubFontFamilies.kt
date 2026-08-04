package com.example.supporthub.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.supporthub.R

object SupportHubFontFamilies {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    val poppins: FontFamily = FontFamily(
        Font(
            googleFont = GoogleFont("Poppins"),
            fontProvider = provider
        )
    )
}
