package com.nyumbahub.feature.listings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class OffPlanProject(
    val id: String,
    val title: String,
    val developer: String,
    val neighbourhood: String,
    val city: String,
    val priceFrom: Double,
    val currency: String = "USD",
    val completionYear: Int,
    val completionPercent: Int,
    val propertyType: String,
    val totalUnits: Int,
    val availableUnits: Int,
    val description: String = ""
)

val demoOffPlan = listOf(
    OffPlanProject("op1", "Kigali Heights Residences", "Heights Development Ltd",
        "Kacyiru", "Kigali", 75000.0, completionYear = 2026, completionPercent = 65,
        propertyType = "Apartments", totalUnits = 120, availableUnits = 48,
        description = "Luxury apartments with panoramic city views in the heart of Kigali."),
    OffPlanProject("op2", "Green Valley Estate", "Rwanda Urban Homes",
        "Nyamata", "Bugesera", 45000.0, completionYear = 2027, completionPercent = 30,
        propertyType = "Villas", totalUnits = 60, availableUnits = 52,
        description = "Eco-friendly gated community with solar power and green spaces."),
    OffPlanProject("op3", "Rubavu Lakefront Towers", "Lake Invest Co",
        "Gisenyi", "Rubavu", 90000.0, completionYear = 2026, completionPercent = 80,
        propertyType = "Apartments", totalUnits = 80, availableUnits = 15,
        description = "Premium lakefront apartments with direct Lake Kivu access."),
    OffPlanProject("op4", "Musanze Mountain View", "Northern Developers",
        "Musanze Centre", "Musanze", 55000.0, completionYear = 2027, completionPercent = 20,
        propertyType = "Townhouses", totalUnits = 40, availableUnits = 38,
        description = "Modern townhouses with stunning Virunga volcano views.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffPlanScreen(
    onBack: () -> Unit,
    onProjectClick: (String) -> Unit
) {
    var projects by remember { mutableStateOf(demoOffPlan) }

    LaunchedEffect(Unit) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("offplan").get()
            .addOnSuccessListener { snap ->
                val fetched = snap.documents.mapNotNull { doc ->
                    try {
                        OffPlanProject(
                            id           = doc.id,
                            title        = doc.getString("title") ?: "",
                            developer    = doc.getString("developer") ?: "",
                            neighbourhood= doc.getString("neighbourhood") ?: "",
                            city         = doc.getString("city") ?: "",
                            priceFrom    = doc.getDouble("priceFrom") ?: 0.0,
                            currency     = doc.getString("currency") ?: "USD",
                            completionYear    = doc.getLong("completionYear")?.toInt() ?: 2026,
                            completionPercent = doc.getLong("completionPercent")?.toInt() ?: 0,
                            propertyType      = doc.getString("propertyType") ?: "Apartments",
                            totalUnits        = doc.getLong("totalUnits")?.toInt() ?: 0,
                            availableUnits    = doc.getLong("availableUnits")?.toInt() ?: 0,
                            description       = doc.getString("description") ?: ""
                        )
                    } catch (e: Exception) { null }
                }
                if (fetched.isNotEmpty()) projects = fetched + demoOffPlan.filter { d -> fetched.none { it.id == d.id } }
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Off-Plan Projects", fontWeight = FontWeight.SemiBold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF4FF))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null,
                            tint = NavyPrimary, modifier = Modifier.size(28.dp))
                        Text(
                            "Buy before completion at lower prices. Units are limited.",
                            fontSize = 13.sp, color = Color.DarkGray, lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Text("${projects.size} projects available",
                    fontSize = 13.sp, color = Color.Gray)
            }

            items(projects) { project ->
                OffPlanCard(project = project, onClick = { onProjectClick(project.id) })
            }
        }
    }
}

@Composable
private fun OffPlanCard(project: OffPlanProject, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(NavyPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = OrangeAccent
                ) {
                    Text("OFF-PLAN", color = Color.White, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text("${project.availableUnits} left",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)

                Text("by ${project.developer}", fontSize = 12.sp, color = Color.Gray)

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text("${project.neighbourhood}, ${project.city}",
                        fontSize = 12.sp, color = Color.Gray)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("From ${project.currency} ${"%,.0f".format(project.priceFrom)}",
                            color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(project.propertyType, fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Completion ${project.completionYear}",
                            fontSize = 12.sp, color = Color.Gray)
                        Text("${project.completionPercent}% built",
                            fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                LinearProgressIndicator(
                    progress = { project.completionPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = NavyPrimary,
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}




