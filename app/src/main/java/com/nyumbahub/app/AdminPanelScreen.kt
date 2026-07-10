package com.nyumbahub.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(onBack: () -> Unit) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    if (!isAuthenticated) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Access", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A1A2E),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF1A1A2E)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.size(72.dp).background(NavyPrimary, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Text("VAF UBWENGE TECH", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("Admin Panel", fontSize = 13.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; passwordError = "" },
                            label = { Text("Password", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            isError = passwordError.isNotEmpty(),
                            supportingText = if (passwordError.isNotEmpty()) {{ Text(passwordError, color = Color.Red) }} else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = NavyPrimary, unfocusedBorderColor = Color.Gray
                            )
                        )
                        Button(
                            onClick = {
                                FirebaseFirestore.getInstance().collection("config").document("admin").get()
                                    .addOnSuccessListener { doc ->
                                        if (password == (doc.getString("password") ?: "")) isAuthenticated = true
                                        else passwordError = "Incorrect password"
                                    }.addOnFailureListener { passwordError = "Network error" }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Enter", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        return
    }

    val tabs = listOf("Listings", "Users", "Motors", "Subscriptions", "Notifications", "Ads")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = { isAuthenticated = false }) { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A2E), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF16213E), contentColor = Color.White, edgePadding = 8.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 12.sp) })
                }
            }
            when (selectedTab) {
                0 -> AdminListingsTab()
                1 -> AdminUsersTab()
                2 -> AdminMotorsTab()
                3 -> AdminSubscriptionsTab()
                4 -> AdminNotificationsTab()
                5 -> AdminAdsTab()
            }
        }
    }
}

@Composable
private fun AdminUsersTab() {
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var verifyingId by remember { mutableStateOf<String?>(null) }

    fun reload() {
        isLoading = true
        FirebaseFirestore.getInstance().collection("users").get()
            .addOnSuccessListener { snap ->
                users = snap.documents.map { doc ->
                    (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id }
                }
                isLoading = false
            }.addOnFailureListener { isLoading = false }
    }

    LaunchedEffect(Unit) { reload() }

    val commissioners = users.filter { (it["role"] as? String) == "commissioner" || (it["role"] as? String) == "service_provider" }
    val regularUsers = users.filter { (it["role"] as? String) != "commissioner" && (it["role"] as? String) != "service_provider" }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatRow("Total users", "${users.size}", Icons.Default.People, NavyPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${commissioners.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = OrangeAccent)
                            Text("Commissioners", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${regularUsers.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyPrimary)
                            Text("Regular Users", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (commissioners.isNotEmpty()) {
                    Text("Service Providers / Commissioners", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrangeAccent)
                    Spacer(Modifier.height(6.dp))
                }
            }
            items(commissioners, key = { it["_id"].toString() + "_c" }) { user ->
                AdminUserCard(user = user, isCommissioner = true, verifyingId = verifyingId,
                    onVerify = { uid ->
                        verifyingId = uid
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update(mapOf("isVerified" to true, "verifiedAt" to System.currentTimeMillis()))
                            .addOnSuccessListener {
                                users = users.map { u -> if (u["_id"] == uid) u.toMutableMap().also { it["isVerified"] = true } else u }
                                verifyingId = null
                            }.addOnFailureListener { verifyingId = null }
                    },
                    onDelete = { uid ->
                        FirebaseFirestore.getInstance().collection("users").document(uid).delete()
                            .addOnSuccessListener { users = users.filter { it["_id"] != uid } }
                    }
                )
            }
            item {
                if (regularUsers.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Regular Users", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                    Spacer(Modifier.height(6.dp))
                }
            }
            items(regularUsers, key = { it["_id"].toString() }) { user ->
                AdminUserCard(user = user, isCommissioner = false, verifyingId = verifyingId,
                    onVerify = { uid ->
                        verifyingId = uid
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update(mapOf("isVerified" to true, "verifiedAt" to System.currentTimeMillis()))
                            .addOnSuccessListener {
                                users = users.map { u -> if (u["_id"] == uid) u.toMutableMap().also { it["isVerified"] = true } else u }
                                verifyingId = null
                            }.addOnFailureListener { verifyingId = null }
                    },
                    onDelete = { uid ->
                    FirebaseFirestore.getInstance().collection("users").document(uid).delete()
                        .addOnSuccessListener { users = users.filter { it["_id"] != uid } }
                })
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: Map<String, Any>,
    isCommissioner: Boolean,
    verifyingId: String?,
    onVerify: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val uid = user["_id"] as? String ?: ""
    val isVerified = user["isVerified"] as? Boolean ?: false
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Remove this user permanently?") },
            confirmButton = { TextButton(onClick = { onDelete(uid); showDeleteDialog = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = if (isCommissioner) OrangeAccent else NavyPrimary, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (user["displayName"] as? String ?: user["email"] as? String ?: "?").firstOrNull()?.uppercase() ?: "?",
                            color = Color.White, fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(user["displayName"] as? String ?: "No name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (isVerified) Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF1A3C5E), modifier = Modifier.size(14.dp))
                    }
                    Text(user["email"] as? String ?: "No email", fontSize = 12.sp, color = Color.Gray)
                }
                if (isCommissioner && !isVerified) {
                    if (verifyingId == uid) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = NavyPrimary)
                    } else {
                        Button(
                            onClick = { onVerify(uid) },
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) { Text("Verify", fontSize = 11.sp) }
                    }
                }
                if (isCommissioner && isVerified) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF2E7D32).copy(alpha = 0.15f)) {
                        Text("VERIFIED", fontSize = 9.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            Text("Phone: ${user["phone"] ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
            Text("Plan: ${user["plan"] ?: "free"}", fontSize = 12.sp, color = NavyPrimary)
            if (isCommissioner) Text("Agency: ${user["agency"] ?: "N/A"}", fontSize = 12.sp, color = OrangeAccent)
            TextButton(onClick = { showDeleteDialog = true }, contentPadding = PaddingValues(0.dp)) {
                Text("Delete User", color = Color.Red, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AdminListingsTab() {
    var listings by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun reload() {
        isLoading = true
        FirebaseFirestore.getInstance().collection("listings").get().addOnSuccessListener { snap ->
            listings = snap.documents.map { doc ->
                (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id }
            }
            isLoading = false
        }.addOnFailureListener { isLoading = false }
    }

    LaunchedEffect(Unit) { reload() }

    if (showAddDialog) {
        AdminAddListingDialog(onDismiss = { showAddDialog = false }, onSave = { data ->
            FirebaseFirestore.getInstance().collection("listings").add(data)
                .addOnSuccessListener { ref ->
                    listings = listings + data.toMutableMap().also { it["_id"] = ref.id }
                }
            showAddDialog = false
        })
    }

    if (showDeleteDialog != null) {
        val idToDelete = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Listing") },
            text = { Text("Delete this listing permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseFirestore.getInstance().collection("listings").document(idToDelete).delete()
                        .addOnSuccessListener {
                            listings = listings.filter { it["_id"] != idToDelete }
                            showDeleteDialog = null
                        }
                        .addOnFailureListener { showDeleteDialog = null }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    showEditDialog?.let { listing ->
        AdminEditListingDialog(listing = listing, onDismiss = { showEditDialog = null }, onSave = { updatedData ->
            val id = listing["_id"] as? String ?: return@AdminEditListingDialog
            FirebaseFirestore.getInstance().collection("listings").document(id).update(updatedData)
                .addOnSuccessListener {
                    listings = listings.map { l -> if (l["_id"] == id) updatedData.toMutableMap().also { it["_id"] = id } else l }
                }
            showEditDialog = null
        })
    }

    // Stats
    val houses = listings.count { (it["propertyType"] as? String)?.lowercase() in listOf("house", "villa", "bungalow") }
    val apartments = listings.count { (it["propertyType"] as? String)?.lowercase() in listOf("apartment", "flat", "studio") }
    val land = listings.count { (it["propertyType"] as? String)?.lowercase() in listOf("land", "plot") }
    val rooms = listings.count { (it["propertyType"] as? String)?.lowercase() in listOf("room", "roommate") }
    var motorsCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("motors").get()
            .addOnSuccessListener { snap -> motorsCount = snap.size() }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                AdminStatRow("Total listings", "${listings.size}", Icons.Default.Home, NavyPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Triple("Houses", houses, NavyPrimary),
                        Triple("Apartments", apartments, OrangeAccent),
                        Triple("Land", land, Color(0xFF2E7D32)),
                        Triple("Rooms", rooms, Color(0xFF7B1FA2)),
                        Triple("Motors", motorsCount, Color(0xFF00838F))
                    ).forEach { (label, count, color) ->
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$count", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
                                Text(label, fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add New Listing")
                }
            }
            items(listings, key = { it["_id"].toString() }) { listing ->
                AdminListingCard(listing = listing,
                    onDelete = { showDeleteDialog = listing["_id"] as? String },
                    onEdit = { showEditDialog = listing }
                )
            }
        }
    }
}

@Composable
private fun AdminListingCard(listing: Map<String, Any>, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(listing["title"] as? String ?: "No title", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(shape = RoundedCornerShape(4.dp), color = if ((listing["status"] as? String) == "ACTIVE") Color(0xFF2E7D32) else Color.Gray) {
                    Text(listing["status"] as? String ?: "UNKNOWN", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Text("ID: ${listing["_id"]}", fontSize = 10.sp, color = Color.Gray)
            Text("Price: ${listing["currency"]} ${listing["price"]}", fontSize = 12.sp, color = OrangeAccent)
            listing["propertyType"]?.let { Text("Type: $it", fontSize = 11.sp, color = Color.Gray) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(36.dp), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f).height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminEditListingDialog(listing: Map<String, Any>, onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    var title by remember { mutableStateOf(listing["title"] as? String ?: "") }
    var price by remember { mutableStateOf(listing["price"]?.toString() ?: "") }
    var description by remember { mutableStateOf(listing["description"] as? String ?: "") }
    var status by remember { mutableStateOf(listing["status"] as? String ?: "ACTIVE") }
    var photoUrlsText by remember { mutableStateOf(((listing["photos"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList<String>()).joinToString("\n")) }
    val statusOptions = listOf("ACTIVE", "FEATURED", "PAUSED", "SOLD", "RENTED", "EXPIRED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Listing", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Text("Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusOptions.take(3).forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s, fontSize = 10.sp) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statusOptions.drop(3).forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s, fontSize = 10.sp) }) }
                }
                HorizontalDivider()
                Text("Photo URLs (one per line)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = photoUrlsText, onValueChange = { photoUrlsText = it }, label = { Text("Image URLs") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6)
            }
        },
        confirmButton = {
            Button(onClick = {
                val photos = photoUrlsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                onSave(mapOf("title" to title, "price" to (price.toDoubleOrNull() ?: 0.0), "description" to description, "status" to status, "photos" to photos))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminAddListingDialog(onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var neighbourhood by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("1") }
    var bathrooms by remember { mutableStateOf("1") }
    var currency by remember { mutableStateOf("USD") }
    var listingType by remember { mutableStateOf("RENT") }
    var status by remember { mutableStateOf("ACTIVE") }
    var photoUrlsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Listing", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = neighbourhood, onValueChange = { neighbourhood = it }, label = { Text("Neighbourhood") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = bedrooms, onValueChange = { bedrooms = it }, label = { Text("Beds") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = bathrooms, onValueChange = { bathrooms = it }, label = { Text("Baths") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Text("Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("RENT", "SALE").forEach { t -> FilterChip(selected = listingType == t, onClick = { listingType = t }, label = { Text(t) }) }
                }
                Text("Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ACTIVE", "FEATURED").forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) }) }
                }
                HorizontalDivider()
                Text("Photo URLs (one per line)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = photoUrlsText, onValueChange = { photoUrlsText = it }, label = { Text("Image URLs") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 5)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isEmpty()) return@Button
                val photos = photoUrlsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                onSave(mapOf("title" to title, "description" to description, "price" to (price.toDoubleOrNull() ?: 0.0), "currency" to currency, "bedrooms" to (bedrooms.toIntOrNull() ?: 1), "bathrooms" to (bathrooms.toIntOrNull() ?: 1), "type" to listingType, "status" to status, "photos" to photos, "listerId" to "admin", "createdAt" to System.currentTimeMillis(), "location" to mapOf("city" to city, "district" to district, "neighbourhood" to neighbourhood, "country" to "Rwanda")))
            }, colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminMotorsTab() {
    var motors by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("motors").get().addOnSuccessListener { snap ->
            motors = snap.documents.map { doc -> (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id } }
            isLoading = false
        }.addOnFailureListener { isLoading = false }
    }

    if (showDeleteDialog != null) {
        val idToDelete = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Motor") },
            text = { Text("Delete this motor listing permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = null
                    FirebaseFirestore.getInstance().collection("motors").document(idToDelete).delete()
                        .addOnSuccessListener { motors = motors.filter { it["_id"] != idToDelete } }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { AdminStatRow("Total motors", "${motors.size}", Icons.Default.DirectionsCar, OrangeAccent) }
            items(motors, key = { it["_id"].toString() }) { motor ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${motor["year"]} ${motor["make"]} ${motor["model"]}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Price: ${motor["currency"]} ${motor["price"]}", color = OrangeAccent, fontSize = 13.sp)
                        Text("City: ${motor["city"]}", fontSize = 12.sp, color = Color.Gray)
                        Button(onClick = { showDeleteDialog = motor["_id"] as? String }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.height(34.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSubscriptionsTab() {
    var subs by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSetPlanDialog by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subscriptions").get().addOnSuccessListener { snap ->
            subs = snap.documents.map { doc -> (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id } }
        }
        db.collection("users").get().addOnSuccessListener { snap ->
            users = snap.documents.map { doc -> (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id } }
            isLoading = false
        }.addOnFailureListener { isLoading = false }
    }

    showSetPlanDialog?.let { user ->
        AdminSetPlanDialog(user = user, onDismiss = { showSetPlanDialog = null }, onSave = { uid, plan, discount ->
            val data = mapOf("plan" to plan, "updatedAt" to System.currentTimeMillis(), "userId" to uid, "discountPercent" to discount, "approvedByAdmin" to true)
            FirebaseFirestore.getInstance().collection("subscriptions").document(uid).set(data)
                .addOnSuccessListener { subs = subs.filter { it["_id"] != uid } + data.toMutableMap().also { it["_id"] = uid } }
            showSetPlanDialog = null
        })
    }

    val freeCnt = subs.count { (it["plan"] as? String) == "free" || it["plan"] == null }
    val proCnt = subs.count { (it["plan"] as? String) == "pro" }
    val bizCnt = subs.count { (it["plan"] as? String) == "business" }
    val pendingCnt = subs.count { it["approvedByAdmin"] != true && (it["plan"] as? String) != "free" }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = NavyPrimary)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Subscription Overview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AdminSubStat("Free", "$freeCnt", Color.White)
                            AdminSubStat("Pro", "$proCnt", OrangeAccent)
                            AdminSubStat("Business", "$bizCnt", Color(0xFFFFD700))
                            AdminSubStat("Pending", "$pendingCnt", Color(0xFFFF6B6B))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("All Users — Manage Subscriptions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            items(users, key = { it["_id"].toString() }) { user ->
                val uid = user["_id"] as? String ?: ""
                val sub = subs.find { it["_id"] == uid }
                val currentPlan = (sub?.get("plan") as? String) ?: "free"
                val discount = (sub?.get("discountPercent") as? Long)?.toInt() ?: 0
                val approved = sub?.get("approvedByAdmin") as? Boolean ?: (currentPlan == "free")
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user["displayName"] as? String ?: "No name", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(user["email"] as? String ?: uid, fontSize = 11.sp, color = Color.Gray)
                            if (discount > 0) Text("Discount: $discount%", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = when (currentPlan) { "pro" -> OrangeAccent; "business" -> Color(0xFFFFD700); else -> Color.Gray }) {
                                Text(currentPlan.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            if (!approved && currentPlan != "free") {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF6B6B).copy(alpha = 0.15f)) {
                                    Text("PENDING APPROVAL", fontSize = 9.sp, color = Color(0xFFFF6B6B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            OutlinedButton(onClick = { showSetPlanDialog = user.toMutableMap().also { it["currentPlan"] = currentPlan; it["currentDiscount"] = discount } }, modifier = Modifier.height(34.dp), contentPadding = PaddingValues(horizontal = 10.dp)) {
                                Text("Manage", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSetPlanDialog(user: Map<String, Any>, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    val uid = user["_id"] as? String ?: ""
    var selectedPlan by remember { mutableStateOf(user["currentPlan"] as? String ?: "free") }
    var discountText by remember { mutableStateOf((user["currentDiscount"] as? Int)?.toString() ?: "0") }
    val plans = listOf("free", "pro", "business")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage: ${user["displayName"] as? String ?: "User"}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("UID: $uid", fontSize = 10.sp, color = Color.Gray)
                Text("Select Plan:", fontWeight = FontWeight.SemiBold)
                plans.forEach { plan ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedPlan = plan }.padding(vertical = 4.dp)) {
                        RadioButton(selected = selectedPlan == plan, onClick = { selectedPlan = plan }, colors = RadioButtonDefaults.colors(selectedColor = NavyPrimary))
                        Text(plan.replaceFirstChar { it.uppercase() }, fontSize = 15.sp)
                    }
                }
                HorizontalDivider()
                Text("Discount %", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = discountText, onValueChange = { discountText = it }, label = { Text("Discount (0-100)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), placeholder = { Text("0") })
                Text("Saving: ${discountText.toIntOrNull() ?: 0}% off", fontSize = 12.sp, color = Color(0xFF2E7D32))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(uid, selectedPlan, discountText.toIntOrNull()?.coerceIn(0, 100) ?: 0) }, colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Approve & Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminSubStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun AdminNotificationsTab() {
    var targetUid by remember { mutableStateOf("") }
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }
    var notifType by remember { mutableStateOf("general") }
    var isSending by remember { mutableStateOf(false) }
    var sendResult by remember { mutableStateOf("") }
    val typeOptions = listOf("general", "inquiry", "listing", "price_drop")

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Send Notification to User", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = targetUid, onValueChange = { targetUid = it }, label = { Text("User UID (empty = all users)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notifTitle, onValueChange = { notifTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notifBody, onValueChange = { notifBody = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    Text("Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        typeOptions.forEach { t -> FilterChip(selected = notifType == t, onClick = { notifType = t }, label = { Text(t, fontSize = 11.sp) }) }
                    }
                    if (sendResult.isNotEmpty()) {
                        Text(sendResult, color = if (sendResult.startsWith("Sent") || sendResult.startsWith("OK")) Color(0xFF2E7D32) else Color.Red, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            if (notifTitle.isEmpty() || notifBody.isEmpty()) { sendResult = "Title and message are required"; return@Button }
                            isSending = true; sendResult = ""
                            val db = FirebaseFirestore.getInstance()
                            val notifData = mapOf("title" to notifTitle, "body" to notifBody, "type" to notifType, "isRead" to false, "createdAt" to System.currentTimeMillis())
                            if (targetUid.isNotEmpty()) {
                                db.collection("notifications").document(targetUid).collection("items").add(notifData)
                                    .addOnSuccessListener { isSending = false; sendResult = "OK Notification sent"; notifTitle = ""; notifBody = "" }
                                    .addOnFailureListener { e -> isSending = false; sendResult = "Error: ${e.message}" }
                            } else {
                                db.collection("users").get().addOnSuccessListener { snap ->
                                    var done = 0; val total = snap.documents.size
                                    if (total == 0) { isSending = false; sendResult = "No users found"; return@addOnSuccessListener }
                                    snap.documents.forEach { userDoc ->
                                        db.collection("notifications").document(userDoc.id).collection("items").add(notifData)
                                            .addOnCompleteListener { done++; if (done == total) { isSending = false; sendResult = "Sent to $total users"; notifTitle = ""; notifBody = "" } }
                                    }
                                }.addOnFailureListener { e -> isSending = false; sendResult = "Error: ${e.message}" }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text(if (targetUid.isEmpty()) "Send to All Users" else "Send to User", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatRow(label: String, value: String, icon: ImageVector, color: Color) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

data class HomeAd(
    val id: String = "", val title: String = "", val subtitle: String = "",
    val imageUrl: String = "", val linkUrl: String = "",
    val backgroundColor: String = "#1A3C5E", val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Composable
private fun AdminAdsTab() {
    var ads by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("home_ads").get().addOnSuccessListener { snap ->
            ads = snap.documents.map { doc -> (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id } }
            isLoading = false
        }.addOnFailureListener { isLoading = false }
    }

    if (showDeleteDialog != null) {
        val idToDelete = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Ad") },
            text = { Text("Remove this ad from the home screen?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = null
                    FirebaseFirestore.getInstance().collection("home_ads").document(idToDelete).delete()
                        .addOnSuccessListener { ads = ads.filter { it["_id"] != idToDelete } }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (showAddDialog) {
        AdminAddAdDialog(onDismiss = { showAddDialog = false }, onSave = { adData ->
            FirebaseFirestore.getInstance().collection("home_ads").add(adData)
                .addOnSuccessListener { ref -> ads = ads + adData.toMutableMap().also { it["_id"] = ref.id } }
            showAddDialog = false
        })
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NavyPrimary) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Home Screen Ads", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Shown at 'Advertise here' banner", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Ad")
                    }
                }
            }
            if (ads.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No ads yet.", color = Color.Gray, fontSize = 13.sp) } }
            }
            items(ads, key = { it["_id"].toString() }) { ad ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(ad["title"] as? String ?: "No title", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(4.dp), color = if (ad["isActive"] == true) Color(0xFF2E7D32) else Color.Gray) {
                                Text(if (ad["isActive"] == true) "ACTIVE" else "INACTIVE", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(ad["subtitle"] as? String ?: "", fontSize = 12.sp, color = Color.Gray)
                        if ((ad["imageUrl"] as? String).orEmpty().isNotEmpty()) {
                            Text("Image URL set", fontSize = 10.sp, color = Color(0xFF2E7D32))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val id = ad["_id"] as? String ?: return@OutlinedButton
                                val newActive = !(ad["isActive"] as? Boolean ?: true)
                                FirebaseFirestore.getInstance().collection("home_ads").document(id).update("isActive", newActive)
                                    .addOnSuccessListener { ads = ads.map { a -> if (a["_id"] == id) a.toMutableMap().also { it["isActive"] = newActive } else a } }
                            }, modifier = Modifier.weight(1f).height(36.dp), contentPadding = PaddingValues(0.dp)) {
                                Text(if (ad["isActive"] == true) "Deactivate" else "Activate", fontSize = 11.sp)
                            }
                            Button(onClick = { showDeleteDialog = ad["_id"] as? String }, modifier = Modifier.weight(1f).height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red), contentPadding = PaddingValues(0.dp)) {
                                Text("Delete", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAddAdDialog(onDismiss: () -> Unit, onSave: (Map<String, Any>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var bgColor by remember { mutableStateOf("#1A3C5E") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Home Screen Ad", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Ad Title *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://your-image.com/banner.jpg", fontSize = 11.sp) })
                OutlinedTextField(value = bgColor, onValueChange = { bgColor = it }, label = { Text("Background Color (hex)") }, modifier = Modifier.fillMaxWidth())
                Text("Preview:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(NavyPrimary, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title.ifEmpty { "Ad Title" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (subtitle.isNotEmpty()) Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isEmpty()) return@Button
                onSave(mapOf("title" to title, "subtitle" to subtitle, "imageUrl" to imageUrl, "backgroundColor" to bgColor, "isActive" to true, "createdAt" to System.currentTimeMillis()))
            }, colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Add Ad") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
