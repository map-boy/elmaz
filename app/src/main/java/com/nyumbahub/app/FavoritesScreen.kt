package com.nyumbahub.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyumbahub.core.ui.FavoritesStore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onListingClick: (String) -> Unit,
    onLoginRequired: () -> Unit
) {
    val savedIds = FavoritesStore.savedIds
    LaunchedEffect(Unit) { FavoritesStore.loadFromFirestore() }

    var firestoreListings by remember { mutableStateOf<List<com.nyumbahub.core.domain.model.Listing>>(emptyList()) }
    LaunchedEffect(savedIds.toList()) {
        if (savedIds.isEmpty()) return@LaunchedEffect
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val fetched = mutableListOf<com.nyumbahub.core.domain.model.Listing>()
        var done = 0
        savedIds.forEach { id ->
            if (id.isBlank()) { done++; if (done == savedIds.size) firestoreListings = fetched.toList(); return@forEach }; db.collection("listings").document(id).get()
                .addOnCompleteListener { task ->
                    try { task.result?.toObject(com.nyumbahub.core.domain.model.Listing::class.java)?.let { fetched.add(it) } } catch (e: Exception) { android.util.Log.e("FavoritesScreen", "Failed to parse favorite listing", e) }
                    done++
                    if (done == savedIds.size) firestoreListings = fetched.toList()
                }
        }
    }

    val displayListings = firestoreListings

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (displayListings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No favorites yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap the heart icon on any listing to save it here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF888888))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayListings) { listing ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onListingClick(listing.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("${listing.location.neighbourhood}, ${listing.location.city}",
                                    fontSize = 12.sp, color = Color.Gray)
                                Text("${listing.currency} ${"%,.0f".format(listing.price)}",
                                    color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            IconButton(onClick = { FavoritesStore.toggle(listing.id) }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = OrangeAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
