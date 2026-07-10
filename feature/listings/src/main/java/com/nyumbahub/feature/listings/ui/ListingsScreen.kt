package com.nyumbahub.feature.listings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.domain.model.*
import com.nyumbahub.core.ui.FavoritesStore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.listings.viewmodel.ListingsViewModel

data class CategoryItem(val label: String, val icon: ImageVector, val type: ListingType?, val propType: PropertyType?, val imageRes: Int? = null)

val categories = listOf(
    CategoryItem("For Rent",   Icons.Default.Home,   ListingType.RENT, null, com.nyumbahub.core.ui.R.drawable.ic_rent),
    CategoryItem("For Sale",   Icons.Default.Star,   ListingType.SALE, null, com.nyumbahub.core.ui.R.drawable.ic_sale),
    CategoryItem("Apartments", Icons.Default.Place,  null, PropertyType.APARTMENT, com.nyumbahub.core.ui.R.drawable.ic_apartment),
    CategoryItem("Villas",     Icons.Default.Filter, null, PropertyType.VILLA, com.nyumbahub.core.ui.R.drawable.ic_villa),
    CategoryItem("Motors",     Icons.Default.Star,   null, null, com.nyumbahub.core.ui.R.drawable.ic_motors),
    CategoryItem("Rooms",      Icons.Default.Home,   null, null, com.nyumbahub.core.ui.R.drawable.ic_rooms)
)

object SearchHistoryStore {
    val history = mutableStateListOf<String>()
    fun add(query: String) {
        if (query.isBlank()) return
        history.remove(query)
        history.add(0, query)
        if (history.size > 10) history.removeAt(history.lastIndex)
    }
}

object RecentlyViewedStore {
    val ids = mutableStateListOf<String>()
    fun add(id: String) {
        if (id.isBlank()) return
        ids.remove(id)
        ids.add(0, id)
        if (ids.size > 10) ids.removeAt(ids.lastIndex)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    onListingClick: (String) -> Unit,
    onLoginRequired: () -> Unit,
    onMotorsClick: () -> Unit = {},
    onRoomsClick: () -> Unit = {},
    onOffPlanClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onHousesClick: () -> Unit = {},
    onApartmentsClick: () -> Unit = {},
    onSearchHistoryClick: (String) -> Unit = {},
    viewModel: ListingsViewModel = hiltViewModel()
) {
    val firebaseListings by viewModel.listings.collectAsState()
    val allListings = firebaseListings

    val forRent    = allListings.filter { it.type == ListingType.RENT }
    val forSale    = allListings.filter { it.type == ListingType.SALE }
    val featured   = allListings.filter { it.status == ListingStatus.FEATURED }
    val houses     = allListings.filter { it.propertyType == PropertyType.HOUSE }
    val apartments = allListings.filter { it.propertyType == PropertyType.APARTMENT }
    val rooms      = allListings.filter { it.propertyType == PropertyType.STUDIO }

    val recentListings = remember(allListings) {
        allListings.sortedByDescending { it.createdAt }.take(10)
    }

    var popularMotors by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("motors").limit(6).get()
            .addOnSuccessListener { snap ->
                val fetched = snap.documents.map { doc ->
                    (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id }
                }
                if (fetched.isNotEmpty()) popularMotors = fetched
            }
    }

    val searchHistory = SearchHistoryStore.history

    Scaffold(containerColor = Color(0xFFF5F5F5)) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Top bar
            item {
                Surface(shadowElevation = 4.dp, color = Color.White) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).clickable { onFilterClick() },
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF0F0F0)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(NavyPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text("Search for property in Rwanda", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onNotificationsClick) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.DarkGray, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // Keep Looking (search history)
            if (searchHistory.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Keep Looking", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { SearchHistoryStore.history.clear() }) {
                                Text("Clear", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(searchHistory) { query ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = NavyPrimary.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable { onSearchHistoryClick(query) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                        Text(query, fontSize = 13.sp, color = NavyPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories
            item {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    categories.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { cat ->
                                CategoryCard(item = cat, modifier = Modifier.weight(1f), onClick = {
                                    when (cat.label) {
                                        "Motors"     -> onMotorsClick()
                                        "Rooms"      -> onRoomsClick()
                                        "Apartments" -> onApartmentsClick()
                                        "Villas"     -> onHousesClick()
                                        else         -> onFilterClick()
                                    }
                                })
                            }
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // Advertise banner
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(36.dp))
                            Column {
                                Text("Advertise Here", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE65100))
                                Text("Reach thousands of property seekers in Rwanda.", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = NavyPrimary)
                    }
                }
            }

            // Recents section
            if (recentListings.isNotEmpty()) {
                item { SectionHeader("Recent Listings") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentListings) { listing ->
                            HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) })
                        }
                    }
                }
            }

            // Featured
            if (featured.isNotEmpty()) {
                item { SectionHeader("Featured Listings") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(featured) { listing -> HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) }) }
                    }
                }
            }

            // Ads banner
            item { HomeAdsBanner() }

            // Popular Motors — only shown when data exists
            if (popularMotors.isNotEmpty()) {
                item { SectionHeader("Popular Motors", onSeeAll = onMotorsClick) }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(popularMotors) { motor ->
                            MotorMiniCardMap(motor = motor, onClick = { onMotorsClick() })
                        }
                    }
                }
            }

            // Rooms for Rent — NEW SECTION
            if (rooms.isNotEmpty()) {
                item { SectionHeader("Rooms for Rent", onSeeAll = onRoomsClick) }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(rooms.take(6)) { listing ->
                            HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) })
                        }
                    }
                }
            }

            // Popular Houses
            val popularHouses = houses.ifEmpty { forSale }
            if (popularHouses.isNotEmpty()) {
                item { SectionHeader("Popular Houses") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(popularHouses.take(6)) { listing -> HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) }) }
                    }
                }
            }

            // Popular Apartments
            if (apartments.isNotEmpty()) {
                item { SectionHeader("Popular Apartments", onSeeAll = onApartmentsClick) }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(apartments.take(6)) { listing -> HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) }) }
                    }
                }
            }

            // Popular for Rent
            if (forRent.isNotEmpty()) {
                item { SectionHeader("Popular for Rent") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(forRent.take(6)) { listing -> HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) }) }
                    }
                }
            }

            // Popular for Sale
            if (forSale.isNotEmpty()) {
                item { SectionHeader("Popular for Sale") }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(forSale.take(6)) { listing -> HorizontalListingCard(listing = listing, onClick = { onListingClick(listing.id) }) }
                    }
                }
            }

            // All Listings
            if (allListings.isNotEmpty()) {
                item { SectionHeader("All Listings") }
                items(allListings) { listing ->
                    VerticalListingCard(
                        listing = listing,
                        onClick = { onListingClick(listing.id) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("See all", color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            modifier = if (onSeeAll != null) Modifier.clickable { onSeeAll() } else Modifier)
    }
}

@Composable
private fun CategoryCard(item: CategoryItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (item.imageRes != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.label,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(NavyPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(28.dp))
                }
            }
            Text(item.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MotorMiniCardMap(motor: Map<String, Any>, onClick: () -> Unit) {
    val context = LocalContext.current
    val photos = (motor["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
    val condition = motor["condition"] as? String ?: ""
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(NavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (photos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(photos.first()).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                }
                if (condition.isNotEmpty()) {
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = if (condition == "New") Color(0xFF2E7D32) else NavyPrimary.copy(alpha = 0.8f)) {
                        Text(condition, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text("${motor["year"] ?: ""} ${motor["make"] ?: ""}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(motor["model"]?.toString() ?: "", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Text("${motor["currency"] ?: "USD"} ${motor["price"] ?: ""}", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HorizontalListingCard(listing: Listing, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.width(200.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(NavyPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (listing.photos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(listing.photos.first()).build(),
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                }
                val isHSaved = FavoritesStore.isSaved(listing.id)
                IconButton(onClick = { FavoritesStore.toggle(listing.id) }, modifier = Modifier.align(Alignment.TopEnd).size(36.dp)) {
                    Icon(
                        if (isHSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isHSaved) OrangeAccent else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (listing.status == ListingStatus.FEATURED) {
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(8.dp), shape = RoundedCornerShape(4.dp), color = OrangeAccent) {
                        Text("FEATURED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text("${listing.currency} ${"%,.0f".format(listing.price)}", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val neighbourhoodText = listing.location.neighbourhood.takeIf {
    it.isNotBlank() && !it.equals("none", ignoreCase = true)
}
Text(
    listOfNotNull("${listing.bedrooms}bd", "${listing.bathrooms}ba", neighbourhoodText).joinToString(" \u00b7 "),
    fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun VerticalListingCard(listing: Listing, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            Box(
                modifier = Modifier.width(110.dp).fillMaxHeight().background(NavyPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (listing.photos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(listing.photos.first()).build(),
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    val isVSaved = FavoritesStore.isSaved(listing.id)
                    IconButton(onClick = { FavoritesStore.toggle(listing.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(if (isVSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isVSaved) OrangeAccent else Color.Gray,
                            modifier = Modifier.size(18.dp))
                    }
                }
                Text("${listing.location.neighbourhood}, ${listing.location.city}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("${listing.currency} ${"%,.0f".format(listing.price)}", color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${listing.bedrooms}bd · ${listing.bathrooms}ba", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

data class HomeAd(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val backgroundColor: String = "#1A3C5E",
    val isActive: Boolean = true
)

@Composable
fun HomeAdsBanner() {
    var ads by remember { mutableStateOf<List<HomeAd>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("home_ads")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snap ->
                ads = snap.documents.mapNotNull { doc ->
                    try {
                        HomeAd(
                            id              = doc.id,
                            title           = doc.getString("title") ?: "",
                            subtitle        = doc.getString("subtitle") ?: "",
                            imageUrl        = doc.getString("imageUrl") ?: "",
                            backgroundColor = doc.getString("backgroundColor") ?: "#1A3C5E",
                            isActive        = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) { null }
                }
            }
    }

    if (ads.isEmpty()) return

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(ads) { ad ->
            Card(
                modifier = Modifier.width(280.dp).height(90.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        try { android.graphics.Color.parseColor(ad.backgroundColor).let { androidx.compose.ui.graphics.Color(it) } }
                        catch (e: Exception) { androidx.compose.ui.graphics.Color(0xFF1A3C5E) }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (ad.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(ad.imageUrl).build(),
                            contentDescription = ad.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                        Text(ad.title, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                        if (ad.subtitle.isNotEmpty()) {
                            Text(ad.subtitle, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1)
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = androidx.compose.ui.graphics.Color(0xFFE87722)) {
                            Text("Sponsored", color = androidx.compose.ui.graphics.Color.White, fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
