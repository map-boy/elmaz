package com.nyumbahub.feature.listings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.nyumbahub.core.domain.model.*
import com.nyumbahub.core.ui.FavoritesStore
import com.nyumbahub.core.ui.components.StatChip
import com.nyumbahub.feature.listings.viewmodel.ListingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    onBack: () -> Unit,
    onInquiry: (String) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listing = uiState.listing
    val listerInfo = uiState.listerInfo
    val isLoading = uiState.isLoading
    val isCreatingInquiry = uiState.isCreatingInquiry
    val isSaved by remember { derivedStateOf { FavoritesStore.isSaved(listingId) } }
    val context = LocalContext.current

    LaunchedEffect(listingId) {
        viewModel.load(listingId)
    }

    fun createInquiryAndChat() {
        viewModel.sendInquiry(onSuccess = { inquiryId -> onInquiry(inquiryId) })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Property Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { FavoritesStore.toggle(listingId) }) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color(0xFFE87722) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A3C5E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            listing?.let {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (!isCreatingInquiry) createInquiryAndChat() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            enabled = !isCreatingInquiry
                        ) { Text("Send Inquiry") }
                        Button(
                            onClick = { if (!isCreatingInquiry) createInquiryAndChat() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE87722)),
                            enabled = !isCreatingInquiry
                        ) {
                            if (isCreatingInquiry) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text("Contact Now")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            listing == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Listing not found", color = Color.Gray)
                }
            }
            else -> {
                val l = listing!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Photo section
                    Box(
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (l.photos.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(l.photos.first()).build(),
                                contentDescription = l.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (l.photos.size > 1) {
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Text("1/${l.photos.size} photos", color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                        } else {
                            Icon(Icons.Default.Home, contentDescription = null,
                                modifier = Modifier.size(72.dp), tint = Color.Gray)
                        }
                        Box(
                            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (l.type == ListingType.RENT) Color(0xFFE87722) else Color(0xFF1A3C5E))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (l.type == ListingType.RENT) "For Rent" else "For Sale",
                                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(l.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("${l.location.neighbourhood}, ${l.location.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(12.dp))
                        Text("${l.currency} ${"%,.0f".format(l.price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFE87722), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Text("Property Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatChip("${l.bedrooms} Bedrooms")
                            StatChip("${l.bathrooms} Bathrooms")
                            if (l.sizeSqm > 0) StatChip("${l.sizeSqm.toInt()} m²")
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(l.description.ifEmpty { "No description provided." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Spacer(Modifier.height(24.dp))

                        // Broker / Owner contact card
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))
                        Text("Contact Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp).clip(CircleShape)
                                            .background(Color(0xFF1A3C5E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            (listerInfo?.displayName ?: "O").first().uppercase(),
                                            color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                listerInfo?.displayName ?: "Owner",
                                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                                            )
                                            if (listerInfo?.isVerified == true) {
                                                Icon(Icons.Default.Verified, contentDescription = "Verified",
                                                    tint = Color(0xFF1A3C5E), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        if (listerInfo?.agency?.isNotEmpty() == true) {
                                            Text(listerInfo!!.agency, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                                HorizontalDivider()
                                if (listerInfo?.phone?.isNotEmpty() == true) {
                                    val ctx = LocalContext.current
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1A3C5E).copy(alpha = 0.08f))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null,
                                            tint = Color(0xFFE87722), modifier = Modifier.size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Phone", fontSize = 11.sp, color = Color.Gray)
                                            Text(listerInfo!!.phone, fontSize = 14.sp,
                                                color = Color(0xFF1A3C5E), fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL,
                                                    android.net.Uri.parse("tel:${listerInfo!!.phone}"))
                                                ctx.startActivity(intent)
                                            }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Call, contentDescription = "Call",
                                                    tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("https://wa.me/${listerInfo!!.phone.replace("+","").replace(" ","")}"))
                                                ctx.startActivity(intent)
                                            }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp",
                                                    tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                                if (listerInfo?.email?.isNotEmpty() == true) {
                                    val ctx = LocalContext.current
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE87722).copy(alpha = 0.08f))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null,
                                            tint = Color(0xFFE87722), modifier = Modifier.size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Email", fontSize = 11.sp, color = Color.Gray)
                                            Text(listerInfo!!.email, fontSize = 13.sp, color = Color.DarkGray)
                                        }
                                        IconButton(onClick = {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO,
                                                android.net.Uri.parse("mailto:${listerInfo!!.email}"))
                                            ctx.startActivity(intent)
                                        }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Send, contentDescription = "Email",
                                                tint = Color(0xFFE87722), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}


