package com.nyumbahub.feature.motors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class MotorCategory(val label: String, val isNew: Boolean = false)

val motorSubCategories = listOf(
    MotorCategory("Used Cars"),
    MotorCategory("New Cars"),
    MotorCategory("Export Cars"),
    MotorCategory("Rental Cars", isNew = true),
    MotorCategory("Motorcycles"),
    MotorCategory("Auto Accessories & Parts"),
    MotorCategory("Heavy Vehicles"),
    MotorCategory("Boats")
)

data class MotorSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val categories: List<String>
)

val motorSections = listOf(
    MotorSection(
        title = "Buy a Vehicle",
        subtitle = "Browse cars, SUVs, trucks and more for sale",
        icon = Icons.Default.DirectionsCar,
        color = NavyPrimary,
        categories = listOf("Used Cars", "New Cars", "Motorcycles", "Heavy Vehicles", "Boats", "Auto Accessories & Parts")
    ),
    MotorSection(
        title = "Rent a Vehicle",
        subtitle = "Short and long term vehicle rentals in Rwanda",
        icon = Icons.Default.CarRental,
        color = Color(0xFF2E7D32),
        categories = listOf("Rental Cars")
    ),
    MotorSection(
        title = "Import a Vehicle",
        subtitle = "Import vehicles directly from Japan, UAE, Europe",
        icon = Icons.Default.Flight,
        color = OrangeAccent,
        categories = listOf("Export Cars")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorsCategoryScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onSellClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motors", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            motorSections.forEach { section ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Section header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(section.color.copy(alpha = 0.08f))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(section.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(section.icon, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.title, fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp, color = section.color)
                                Text(section.subtitle, fontSize = 12.sp,
                                    color = Color.Gray)
                            }
                        }
                        HorizontalDivider()
                        // Categories under section
                        section.categories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCategoryClick(cat) }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(section.color)
                                    )
                                    Text(cat, fontSize = 15.sp)
                                    if (cat == "Rental Cars") {
                                        Surface(shape = RoundedCornerShape(20.dp), color = OrangeAccent) {
                                            Text("NEW", color = Color.White, fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null, tint = Color.LightGray)
                            }
                            if (cat != section.categories.last()) {
                                HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            }
                        }
                    }
                }
            }

            // Post ad banner
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSellClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sell or Rent Your Vehicle", fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = Color.White)
                        Text("Post a free ad and reach thousands of buyers",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.Add, contentDescription = null,
                        tint = OrangeAccent, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
