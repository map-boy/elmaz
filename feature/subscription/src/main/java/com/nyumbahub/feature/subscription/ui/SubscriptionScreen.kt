package com.nyumbahub.feature.subscription.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class PlanInfo(
    val name: String,
    val price: String,
    val priceRwf: String,
    val listings: String,
    val features: List<String>,
    val isPopular: Boolean = false,
    val planKey: String
)

val subscriptionPlans = listOf(
    PlanInfo(
        name = "Free", price = "0", priceRwf = "0 RWF", listings = "3 listings", planKey = "free",
        features = listOf("3 active listings","Basic search visibility","Standard support","Photo upload (3 per listing)")
    ),
    PlanInfo(
        name = "Pro", price = "10", priceRwf = "13,000 RWF/mo", listings = "20 listings",
        planKey = "pro", isPopular = true,
        features = listOf("20 active listings","Priority search placement","Featured badge on listings",
            "Photo upload (10 per listing)","Inquiry notifications","Analytics dashboard","Email & chat support")
    ),
    PlanInfo(
        name = "Business", price = "25", priceRwf = "32,500 RWF/mo", listings = "Unlimited",
        planKey = "business",
        features = listOf("Unlimited active listings","Top search placement","Featured + Verified badge",
            "Unlimited photos per listing","Priority inquiry notifications","Advanced analytics",
            "Dedicated account manager","Custom branding options","API access")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(onBack: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    var currentPlanKey by remember { mutableStateOf("free") }
    var isLoading by remember { mutableStateOf(true) }
    var showLaws by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("subscriptions").document(uid).get()
                .addOnSuccessListener { doc -> currentPlanKey = doc.getString("plan") ?: "free"; isLoading = false }
                .addOnFailureListener { isLoading = false }
        } ?: run { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Plans", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Choose your plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("All prices in RWF. Plans are activated manually by our team after payment confirmation.",
                style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            if (!isLoading) {
                Surface(shape = RoundedCornerShape(8.dp), color = NavyPrimary.copy(alpha = 0.1f)) {
                    Text("Current plan: ${currentPlanKey.replaceFirstChar { it.uppercase() }}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = NavyPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            subscriptionPlans.forEach { plan ->
                val isCurrent = currentPlanKey == plan.planKey
                Card(
                    modifier = Modifier.fillMaxWidth().border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) NavyPrimary else Color(0xFFDDDDDD),
                        shape = RoundedCornerShape(16.dp)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isCurrent) NavyPrimary.copy(alpha = 0.04f) else Color.White),
                    elevation = CardDefaults.cardElevation(if (isCurrent) 4.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    if (plan.isPopular) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = OrangeAccent) {
                                            Text("POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    if (isCurrent) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF2E7D32)) {
                                            Text("ACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                }
                                Text(plan.priceRwf, color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("${plan.listings} included", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Spacer(Modifier.height(8.dp))
                        plan.features.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Text(feature, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }

            // Contact to activate card
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(32.dp))
                    Text("How to upgrade", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE65100))
                    Text("1. Pay via MTN Mobile Money to: 0798028184", fontSize = 13.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    Text("2. Send payment screenshot to WhatsApp: 0798028184", fontSize = 13.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    Text("3. Our team will activate your plan within 24 hours", fontSize = 13.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = NavyPrimary.copy(alpha = 0.08f)) {
                        Text("Plans are activated manually. No automatic payment processing.",
                            fontSize = 11.sp, color = NavyPrimary, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            TextButton(onClick = { showLaws = !showLaws }) {
                Text(if (showLaws) "Hide Legal Information" else "View Legal Information & Terms", color = NavyPrimary, fontSize = 13.sp)
            }

            if (showLaws) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Legal Information & Rwanda Laws", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("1. Regulatory Compliance", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Elmaz operates in compliance with Rwanda Law No. 055/2021 on ICT and Law No. 058/2021 on Prevention of Cyber Crimes.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("2. Data Protection", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Your personal data is protected under Rwanda Law No. 058/2021. We never sell your data to third parties.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("3. Property Listing Rules", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("All listings must comply with Rwanda Housing Authority regulations. Fraudulent listings are prohibited under Rwanda Penal Code.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("4. Subscription & Payments", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Fees charged in RWF. Payments via MTN Mobile Money processed under National Bank of Rwanda (BNR) regulations. Refunds within 7 business days on written request.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("5. Consumer Protection", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Protected under Rwanda Law No. 13/2009. Disputes may be escalated to RURA or Rwanda Consumer Rights Protection.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("6. Listing Limits", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Free: 3 listings. Pro: 20 listings. Business: unlimited. Elmaz may remove listings violating terms or Rwanda law.", fontSize = 12.sp, color = Color.DarkGray)
                        Text("7. Dispute Resolution", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Disputes resolved under Rwandan courts jurisdiction. Alternative resolution via Rwanda Arbitration Centre (RAC).", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Contact: +250 798028184", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
