package com.nyumbahub.feature.listings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class Room(
    val id: String = "",
    val title: String = "",
    val neighbourhood: String = "",
    val city: String = "",
    val price: Double = 0.0,
    val currency: String = "USD",
    val isShared: Boolean = false,
    val amenities: List<String> = emptyList(),
    val description: String = "",
    val photos: List<String> = emptyList()
)

val demoRooms = listOf(
    Room("r1", "Self-contained Room - Kimironko", "Kimironko", "Kigali", 120.0,
        amenities = listOf("WiFi", "Water", "Security")),
    Room("r2", "Shared Room - Remera", "Remera", "Kigali", 60.0, isShared = true,
        amenities = listOf("WiFi", "Kitchen")),
    Room("r3", "Studio Room - Nyamirambo", "Nyamirambo", "Kigali", 150.0,
        amenities = listOf("WiFi", "Water", "Power backup")),
    Room("r4", "Single Room - Gikondo", "Gikondo", "Kigali", 80.0,
        amenities = listOf("Water", "Security")),
    Room("r5", "Executive Room - Kacyiru", "Kacyiru", "Kigali", 200.0,
        amenities = listOf("WiFi", "Water", "Power backup", "Parking")),
    Room("r6", "Room - Musanze", "Musanze Centre", "Musanze", 70.0,
        amenities = listOf("Water", "WiFi"))
)

val roomFilters = listOf("All", "Self-contained", "Shared", "Studio", "Executive")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    onBack: () -> Unit,
    onRoomClick: (String) -> Unit,
    onPostRoom: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var rooms by remember { mutableStateOf(demoRooms) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("rooms")
            .get()
            .addOnSuccessListener { snap ->
                val fetched = snap.documents.mapNotNull { doc ->
                    try {
                        Room(
                            id           = doc.id,
                            title        = doc.getString("title") ?: "",
                            neighbourhood= doc.getString("neighbourhood") ?: "",
                            city         = doc.getString("city") ?: "",
                            price        = doc.getDouble("price") ?: 0.0,
                            currency     = doc.getString("currency") ?: "USD",
                            isShared     = doc.getBoolean("isShared") ?: false,
                            amenities    = (doc.get("amenities") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            description  = doc.getString("description") ?: ""
                        )
                    } catch (e: Exception) { null }
                }
                rooms = if (fetched.isEmpty()) demoRooms else fetched
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val filtered = when (selectedFilter) {
        "Shared"         -> rooms.filter { it.isShared }
        "Self-contained" -> rooms.filter { !it.isShared && it.title.contains("Self", ignoreCase = true) }
        "Studio"         -> rooms.filter { it.title.contains("Studio", ignoreCase = true) }
        "Executive"      -> rooms.filter { it.title.contains("Executive", ignoreCase = true) }
        else             -> rooms
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rooms for Rent", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPostRoom, containerColor = OrangeAccent) {
                Icon(Icons.Default.Add, contentDescription = "Post Room", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(roomFilters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick  = { selectedFilter = filter },
                            label    = { Text(filter, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("${filtered.size} rooms available",
                            fontSize = 13.sp, color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(filtered) { room ->
                        RoomCard(room = room, onClick = { onRoomClick(room.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            Box(
                modifier = Modifier.width(110.dp).fillMaxHeight()
                    .background(NavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (room.photos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(room.photos.first()).build(),
                        contentDescription = room.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                }
                if (room.isShared) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = OrangeAccent
                    ) {
                        Text("Shared", color = Color.White, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(room.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = Color.Gray, modifier = Modifier.size(13.dp))
                    Text("${room.neighbourhood}, ${room.city}",
                        fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    room.amenities.take(3).forEach { amenity ->
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF0F0F0)) {
                            Text(amenity, fontSize = 10.sp, color = Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Text("${room.currency} ${"%,.0f".format(room.price)}/mo",
                    color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}



