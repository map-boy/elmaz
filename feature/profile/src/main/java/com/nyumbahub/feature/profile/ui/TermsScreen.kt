package com.nyumbahub.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nyumbahub.core.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    isPrivacyPolicy: Boolean = false,
    onBack: () -> Unit
) {
    val title = if (isPrivacyPolicy) "Privacy Policy" else "Terms & Conditions"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            if (isPrivacyPolicy) {
                PrivacyPolicyContent()
            } else {
                TermsContent()
            }
        }
    }
}

@Composable
private fun TermsContent() {
    TermsSection("1. Acceptance of Terms",
        "By using Elmaz you agree to these terms. If you do not agree, please do not use the app.")
    TermsSection("2. Listings",
        "All listings must be accurate and lawful. Elmaz reserves the right to remove any listing that violates these terms or applicable Rwandan law.")
    TermsSection("3. User Accounts",
        "You are responsible for maintaining the confidentiality of your account credentials. You must notify us immediately of any unauthorised use.")
    TermsSection("4. Subscriptions & Payments",
        "Subscription fees are charged in advance. Refunds are not provided for partial billing periods unless required by law.")
    TermsSection("5. Prohibited Conduct",
        "You may not post fraudulent listings, harass other users, scrape data, or use the platform for any unlawful purpose.")
    TermsSection("6. Limitation of Liability",
        "Elmaz is a marketplace platform only. We are not responsible for the accuracy of listings or the conduct of users.")
    TermsSection("7. Changes to Terms",
        "We may update these terms at any time. Continued use of the app after changes constitutes acceptance.")
    TermsSection("8. Contact",
        "For questions about these terms, contact us at legal@Elmaz.rw")
}

@Composable
private fun PrivacyPolicyContent() {
    TermsSection("1. Data We Collect",
        "We collect information you provide (name, email, phone), listing data, and usage analytics to improve the service.")
    TermsSection("2. How We Use Your Data",
        "Your data is used to operate the platform, send relevant notifications, and improve our services. We do not sell your personal data.")
    TermsSection("3. Data Sharing",
        "We share data only with Firebase (Google) for hosting and analytics, and with other users only as necessary to facilitate transactions.")
    TermsSection("4. Data Retention",
        "We retain your data for as long as your account is active. You may request deletion by contacting support.")
    TermsSection("5. Security",
        "We use industry-standard encryption and Firebase security rules to protect your data.")
    TermsSection("6. Your Rights",
        "You have the right to access, correct, or delete your personal data at any time by contacting us at privacy@Elmaz.rw")
    TermsSection("7. Cookies & Analytics",
        "The app uses Firebase Analytics to understand usage patterns. No third-party advertising cookies are used.")
    TermsSection("8. Contact",
        "For privacy concerns, contact us at privacy@Elmaz.rw")
}

@Composable
private fun TermsSection(heading: String, body: String) {
    Text(
        text = heading,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    )
    Spacer(Modifier.height(20.dp))
}


