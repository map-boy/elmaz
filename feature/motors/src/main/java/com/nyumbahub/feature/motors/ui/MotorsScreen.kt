package com.nyumbahub.feature.motors.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class Motor(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val currency: String = "USD",
    val mileage: Int = 0,
    val transmission: String = "",
    val fuelType: String = "",
    val condition: String = "",
    val city: String = "",
    val neighbourhood: String = "",
    val description: String = "",
    val photos: List<String> = emptyList(),
    val listerId: String = "",
    val category: String = "Cars"
)

val motorCategories = listOf("All", "Cars", "SUV", "Trucks", "Motorcycles", "Buses", "Vans")

val motorBrands = listOf("All Brands", "Toyota", "Honda", "Mercedes", "BMW", "Mitsubishi", "Nissan", "Hyundai", "Kia", "Ford", "Volkswagen")

val priceRanges = listOf("Any Price", "Under $5K", "$5K-$15K", "$15K-$30K", "$30K-$60K", "Over $60K")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorsScreen(
    onBack: () -> Unit,
    onMotorClick: (String) -> Unit,
    onPostMotor: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedBrand by remember { mutableStateOf("All Brands") }
    var selectedPrice by remember { mutableStateOf("Any Price") }
    var searchQuery by remember { mutableStateOf("") }
    var motors by remember { mutableStateOf<List<Motor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("motors")
            .get()
            .addOnSuccessListener { snap ->
                val fetched = snap.documents.mapNotNull { doc ->
                    try {
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
                }
                motors = fetched
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val filtered = motors.filter { m ->
        val catMatch = selectedCategory == "All" || m.category.equals(selectedCategory, ignoreCase = true)
        val brandMatch = selectedBrand == "All Brands" || m.make.equals(selectedBrand, ignoreCase = true)
        val priceMatch = when (selectedPrice) {
            "Under $5K"    -> m.price < 5000
            "$5K-$15K"     -> m.price in 5000.0..15000.0
            "$15K-$30K"    -> m.price in 15000.0..30000.0
            "$30K-$60K"    -> m.price in 30000.0..60000.0
            "Over $60K"    -> m.price > 60000
            else           -> true
        }
        val searchMatch = searchQuery.isBlank() ||
            "${m.make} ${m.model} ${m.year}".contains(searchQuery, ignoreCase = true) ||
            m.city.contains(searchQuery, ignoreCase = true)
        catMatch && brandMatch && priceMatch && searchMatch
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Motors", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
                // Search bar
                Surface(color = NavyPrimary) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search make, model, city...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        singleLine = true
                    )
                }
                // Category chips
                Surface(color = Color.White, shadowElevation = 2.dp) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(motorCategories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick  = { selectedCategory = cat },
                                label    = { Text(cat, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPostMotor, containerColor = OrangeAccent) {
                Icon(Icons.Default.Add, contentDescription = "Post Motor", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Expandable filter panel
            if (showFilters) {
                Surface(color = Color.White, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Brand", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(motorBrands) { brand ->
                                FilterChip(
                                    selected = selectedBrand == brand,
                                    onClick  = { selectedBrand = brand },
                                    label    = { Text(brand, fontSize = 11.sp) },
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangeAccent,
                                        selectedLabelColor     = Color.White
                                    )
                                )
                            }
                        }
                        Text("Price Range", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(priceRanges) { range ->
                                FilterChip(
                                    selected = selectedPrice == range,
                                    onClick  = { selectedPrice = range },
                                    label    = { Text(range, fontSize = 11.sp) },
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor     = Color.White
                                    )
                                )
                            }
                        }
                        if (selectedBrand != "All Brands" || selectedPrice != "Any Price") {
                            TextButton(onClick = { selectedBrand = "All Brands"; selectedPrice = "Any Price" }) {
                                Text("Clear Filters", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("No motors found", color = Color.Gray, fontSize = 15.sp)
                        if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                            TextButton(onClick = { searchQuery = ""; selectedCategory = "All"; selectedBrand = "All Brands"; selectedPrice = "Any Price" }) {
                                Text("Clear all filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("${filtered.size} vehicle${if (filtered.size != 1) "s" else ""} found",
                            fontSize = 13.sp, color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(filtered, key = { it.id }) { motor ->
                        MotorCard(motor = motor, context = context, onClick = { onMotorClick(motor.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun MotorCard(motor: Motor, context: android.content.Context, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .background(NavyPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (motor.photos.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(motor.photos.first()).build(),
                        contentDescription = "${motor.make} ${motor.model}",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (motor.condition == "New") Color(0xFF2E7D32) else NavyPrimary.copy(alpha = 0.85f)
                ) {
                    Text(motor.condition.ifEmpty { "Used" }, color = Color.White, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = OrangeAccent
                ) {
                    Text("${motor.currency} ${"%,.0f".format(motor.price)}", color = Color.White,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${motor.year} ${motor.make} ${motor.model}",
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    MotorSpecChip(Icons.Default.Speed, "${"%,d".format(motor.mileage)} km")
                    MotorSpecChip(Icons.Default.Settings, motor.transmission)
                    MotorSpecChip(Icons.Default.LocalGasStation, motor.fuelType)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = Color.Gray, modifier = Modifier.size(13.dp))
                    Text("${motor.neighbourhood.ifEmpty { motor.city }}, ${motor.city}",
                        fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun MotorSpecChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    if (label.isBlank()) return
    Surface(shape = RoundedCornerShape(20.dp), color = NavyPrimary.copy(alpha = 0.07f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(11.dp))
            Text(label, fontSize = 10.sp, color = NavyPrimary)
        }
    }
}