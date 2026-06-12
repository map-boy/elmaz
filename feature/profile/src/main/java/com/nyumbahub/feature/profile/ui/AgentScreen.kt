package com.nyumbahub.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class Agent(
    val id: String,
    val name: String,
    val agency: String,
    val district: String,
    val city: String,
    val phone: String,
    val totalListings: Int,
    val yearsExperience: Int,
    val rating: Float,
    val isVerified: Boolean = false,
    val speciality: String = "Residential"
)

val demoAgents = listOf(
    Agent("a1", "Jean Pierre Habimana", "Kigali Realty Group", "Gasabo", "Kigali",
        "+250 788 123 456", totalListings = 34, yearsExperience = 7,
        rating = 4.8f, isVerified = true, speciality = "Luxury Villas"),
    Agent("a2", "Marie Claire Uwimana", "Rwanda Property Hub", "Nyarugenge", "Kigali",
        "+250 722 234 567", totalListings = 21, yearsExperience = 4,
        rating = 4.5f, isVerified = true, speciality = "Apartments"),
    Agent("a3", "Eric Nshimiyimana", "HomeFinder Rwanda", "Kicukiro", "Kigali",
        "+250 733 345 678", totalListings = 15, yearsExperience = 3,
        rating = 4.2f, isVerified = false, speciality = "Rentals"),
    Agent("a4", "Diane Mukamana", "Lake Kivu Properties", "Rubavu", "Gisenyi",
        "+250 788 456 789", totalListings = 28, yearsExperience = 6,
        rating = 4.7f, isVerified = true, speciality = "Lakefront"),
    Agent("a5", "Patrick Bizimana", "Northern Homes", "Musanze", "Musanze",
        "+250 722 567 890", totalListings = 12, yearsExperience = 2,
        rating = 4.0f, isVerified = false, speciality = "Residential"),
    Agent("a6", "Alice Nyiransengimana", "Premium Estates Rwanda", "Gasabo", "Kigali",
        "+250 733 678 901", totalListings = 45, yearsExperience = 10,
        rating = 4.9f, isVerified = true, speciality = "Commercial")
)

val agentCityFilters = listOf("All", "Kigali", "Gisenyi", "Musanze")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    onBack: () -> Unit,
    onAgentClick: (String) -> Unit
) {
    var agents by remember { mutableStateOf(demoAgents) }
    var selectedCity by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("agents").get()
            .addOnSuccessListener { snap ->
                val fetched = snap.documents.mapNotNull { doc ->
                    try {
                        Agent(
                            id              = doc.id,
                            name            = doc.getString("name") ?: "",
                            agency          = doc.getString("agency") ?: "",
                            district        = doc.getString("district") ?: "",
                            city            = doc.getString("city") ?: "",
                            phone           = doc.getString("phone") ?: "",
                            totalListings   = doc.getLong("totalListings")?.toInt() ?: 0,
                            yearsExperience = doc.getLong("yearsExperience")?.toInt() ?: 0,
                            rating          = doc.getDouble("rating")?.toFloat() ?: 0f,
                            isVerified      = doc.getBoolean("isVerified") ?: false,
                            speciality      = doc.getString("speciality") ?: "Residential"
                        )
                    } catch (e: Exception) { null }
                }
                if (fetched.isNotEmpty()) agents = fetched + demoAgents.filter { d -> fetched.none { it.id == d.id } }
            }
    }
    var searchQuery  by remember { mutableStateOf("") }

    val filtered = agents
        .filter { if (selectedCity == "All") true else it.city == selectedCity }
        .filter { if (searchQuery.isEmpty()) true else it.name.contains(searchQuery, ignoreCase = true) || it.agency.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find an Agent", fontWeight = FontWeight.SemiBold) },
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
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Surface(color = Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name or agency...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(agentCityFilters) { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick  = { selectedCity = city },
                                label    = { Text(city, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("${filtered.size} agents found",
                        fontSize = 13.sp, color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                items(filtered) { agent ->
                    AgentCard(agent = agent, onClick = { onAgentClick(agent.id) })
                }
            }
        }
    }
}

@Composable
private fun AgentCard(agent: Agent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        agent.name.first().uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(agent.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f))
                        if (agent.isVerified) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified",
                                tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(agent.agency, fontSize = 13.sp, color = Color.Gray,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            tint = Color.Gray, modifier = Modifier.size(13.dp))
                        Text("${agent.district}, ${agent.city}",
                            fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AgentStat("⭐ ${agent.rating}", "Rating")
                AgentStat("${agent.totalListings}", "Listings")
                AgentStat("${agent.yearsExperience} yrs", "Experience")
                AgentStat(agent.speciality, "Speciality")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call", fontSize = 13.sp)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Message", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun AgentStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

