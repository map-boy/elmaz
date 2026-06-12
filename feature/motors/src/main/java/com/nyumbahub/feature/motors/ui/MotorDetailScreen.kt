package com.nyumbahub.feature.motors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MotorDetailScreen(
    motorId: String,
    onBack: () -> Unit,
    onInquiry: (String) -> Unit = {}
) {
    var motor by remember { mutableStateOf<Motor?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var listerPhone by remember { mutableStateOf("") }
    var listerEmail by remember { mutableStateOf("") }
    var listerName by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(motorId) {
        FirebaseFirestore.getInstance().collection("motors").document(motorId).get()
            .addOnSuccessListener { doc ->
                motor = try {
                    Motor(
                        id           = doc.id,
                        make         = doc.getString("make") ?: "",
                        model        = doc.getString("model") ?: "",
                        year         = doc.getLong("year")?.toInt() ?: 0,
                        price        = doc.getDouble("price") ?: 0.0,
                        currency     = doc.getString("currency") ?: "USD",
                        mileage      = doc.getLong("mileage")?.toInt() ?: 0,
                        transmission = doc.getString("transmission") ?: "",
                        fuelType     = doc.getString("fuelType") ?: "",
                        condition    = doc.getString("condition") ?: "",
                        city         = doc.getString("city") ?: "",
                        neighbourhood= doc.getString("neighbourhood") ?: "",
                        description  = doc.getString("description") ?: "",
                        photos       = (doc.get("photos") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        listerId     = doc.getString("listerId") ?: "",
                        category     = doc.getString("category") ?: "Cars"
                    )
                } catch (e: Exception) { null }
                val lid = doc.getString("listerId") ?: ""
                if (lid.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("users").document(lid).get()
                        .addOnSuccessListener { u ->
                            listerName  = u.getString("displayName") ?: u.getString("name") ?: ""
                            listerPhone = u.getString("phone") ?: ""
                            listerEmail = u.getString("email") ?: ""
                        }
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(motor?.let { "${it.year} ${it.make} ${it.model}" } ?: "Motor Details",
                    maxLines = 1) },
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
        },
        bottomBar = {
            motor?.let {
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (listerPhone.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:$listerPhone"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call")
                            }
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://wa.me/${listerPhone.replace("+","").replace(" ","")}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("WhatsApp")
                            }
                        } else {
                            Button(
                                onClick = { onInquiry(motorId) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                            ) { Text("Send Inquiry") }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            motor == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Motor not found", color = Color.Gray)
            }
            else -> {
                val m = motor!!
                Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

                    // Photo gallery
                    if (m.photos.isNotEmpty()) {
                        val pagerState = rememberPagerState { m.photos.size }
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(m.photos[page]).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Page indicators
                            Row(
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(m.photos.size) { i ->
                                    Box(modifier = Modifier.size(if (pagerState.currentPage == i) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.5f)))
                                }
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = if (m.condition == "New") Color(0xFF2E7D32) else NavyPrimary
                            ) {
                                Text(m.condition.ifEmpty { "Used" }, color = Color.White, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = OrangeAccent
                            ) {
                                Text("${m.currency} ${"%,.0f".format(m.price)}", color = Color.White,
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp)
                            .background(NavyPrimary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null,
                                modifier = Modifier.size(72.dp), tint = Color.LightGray)
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        // Title + price
                        Text("${m.year} ${m.make} ${m.model}",
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${m.currency} ${"%,.0f".format(m.price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OrangeAccent, fontWeight = FontWeight.Bold)

                        // Quick specs row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Pair(Icons.Default.Speed, "${"%,d".format(m.mileage)} km"),
                                Pair(Icons.Default.Settings, m.transmission),
                                Pair(Icons.Default.LocalGasStation, m.fuelType)
                            ).filter { it.second.isNotBlank() }.forEach { (icon, label) ->
                                Surface(shape = RoundedCornerShape(20.dp), color = NavyPrimary.copy(alpha = 0.08f)) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                                        Text(label, fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        // Vehicle details
                        Text("Vehicle Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(10.dp)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MotorDetailRow("Make", m.make)
                                MotorDetailRow("Model", m.model)
                                MotorDetailRow("Year", m.year.toString())
                                MotorDetailRow("Mileage", "${"%,d".format(m.mileage)} km")
                                MotorDetailRow("Transmission", m.transmission)
                                MotorDetailRow("Fuel Type", m.fuelType)
                                MotorDetailRow("Condition", m.condition)
                                MotorDetailRow("Category", m.category)
                                MotorDetailRow("Location", "${m.neighbourhood.ifEmpty { m.city }}, ${m.city}")
                            }
                        }

                        HorizontalDivider()

                        // Description
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(m.description.ifEmpty { "No description provided." },
                            style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, lineHeight = 22.sp)

                        // Seller contact card
                        if (listerName.isNotEmpty() || listerPhone.isNotEmpty() || listerEmail.isNotEmpty()) {
                            HorizontalDivider()
                            Text("Seller Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(2.dp)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (listerName.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                                                .background(NavyPrimary), contentAlignment = Alignment.Center) {
                                                Text(listerName.first().uppercase(), color = Color.White,
                                                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(listerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                        HorizontalDivider()
                                    }
                                    if (listerPhone.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NavyPrimary.copy(alpha = 0.07f))
                                            .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(Icons.Default.Phone, contentDescription = null,
                                                tint = OrangeAccent, modifier = Modifier.size(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Phone", fontSize = 11.sp, color = Color.Gray)
                                                Text(listerPhone, fontSize = 14.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                IconButton(onClick = {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL,
                                                        android.net.Uri.parse("tel:$listerPhone"))
                                                    context.startActivity(intent)
                                                }, modifier = Modifier.size(36.dp)) {
                                                    Icon(Icons.Default.Call, contentDescription = "Call",
                                                        tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                                }
                                                IconButton(onClick = {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                                                        android.net.Uri.parse("https://wa.me/${listerPhone.replace("+","").replace(" ","")}"))
                                                    context.startActivity(intent)
                                                }, modifier = Modifier.size(36.dp)) {
                                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp",
                                                        tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                    if (listerEmail.isNotEmpty()) {
                                        Row(modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OrangeAccent.copy(alpha = 0.07f))
                                            .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(Icons.Default.Email, contentDescription = null,
                                                tint = OrangeAccent, modifier = Modifier.size(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Email", fontSize = 11.sp, color = Color.Gray)
                                                Text(listerEmail, fontSize = 13.sp, color = Color.DarkGray)
                                            }
                                            IconButton(onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO,
                                                    android.net.Uri.parse("mailto:$listerEmail"))
                                                context.startActivity(intent)
                                            }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Send, contentDescription = "Email",
                                                    tint = OrangeAccent, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MotorDetailRow(label: String, value: String) {
    if (value.isBlank() || value == "0") return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}