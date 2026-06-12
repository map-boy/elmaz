package com.nyumbahub.feature.listings.ui

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

data class PropertySection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val categories: List<String>
)

val houseSections = listOf(
    PropertySection(
        title = "Houses for Rent",
        subtitle = "Find a house to rent in Rwanda",
        icon = Icons.Default.Home,
        color = NavyPrimary,
        categories = listOf("Residential House", "Villa", "Townhouse", "Bungalow", "Guest House")
    ),
    PropertySection(
        title = "Houses for Sale",
        subtitle = "Buy a house or land in Rwanda",
        icon = Icons.Default.Sell,
        color = Color(0xFF2E7D32),
        categories = listOf("House for Sale", "Villa for Sale", "Townhouse for Sale", "Land / Plot", "New Build")
    ),
    PropertySection(
        title = "Off-Plan Projects",
        subtitle = "Invest in upcoming developments",
        icon = Icons.Default.Construction,
        color = OrangeAccent,
        categories = listOf("Off-Plan Houses", "Gated Communities", "Real Estate Projects")
    )
)

val apartmentSections = listOf(
    PropertySection(
        title = "Apartments for Rent",
        subtitle = "Find an apartment to rent in Rwanda",
        icon = Icons.Default.Apartment,
        color = NavyPrimary,
        categories = listOf("Studio Apartment", "1 Bedroom", "2 Bedrooms", "3 Bedrooms", "4+ Bedrooms", "Penthouse")
    ),
    PropertySection(
        title = "Apartments for Sale",
        subtitle = "Buy an apartment in Rwanda",
        icon = Icons.Default.Sell,
        color = Color(0xFF2E7D32),
        categories = listOf("Studio for Sale", "1 Bed for Sale", "2 Bed for Sale", "3 Bed for Sale", "Luxury Apartment")
    ),
    PropertySection(
        title = "Short Stay",
        subtitle = "Daily & monthly furnished apartments",
        icon = Icons.Default.Hotel,
        color = OrangeAccent,
        categories = listOf("Daily Rental", "Weekly Rental", "Monthly Furnished", "Serviced Apartment")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesCategoryScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPostClick: () -> Unit = {}
) {
    PropertyCategoryScreen(
        title = "Houses",
        sections = houseSections,
        accentColor = NavyPrimary,
        onBack = onBack,
        onCategoryClick = onCategoryClick,
        onPostClick = onPostClick,
        postLabel = "List Your House"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentsCategoryScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPostClick: () -> Unit = {}
) {
    PropertyCategoryScreen(
        title = "Apartments",
        sections = apartmentSections,
        accentColor = Color(0xFF6A1B9A),
        onBack = onBack,
        onCategoryClick = onCategoryClick,
        onPostClick = onPostClick,
        postLabel = "List Your Apartment"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyCategoryScreen(
    title: String,
    sections: List<PropertySection>,
    accentColor: Color,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onPostClick: () -> Unit,
    postLabel: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sections.forEach { section ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(section.color.copy(alpha = 0.08f))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                                    .background(section.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(section.icon, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.title, fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp, color = section.color)
                                Text(section.subtitle, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        HorizontalDivider()
                        section.categories.forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onCategoryClick(cat) }
                                    .padding(horizontal = 20.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier.size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(section.color)
                                    )
                                    Text(cat, fontSize = 14.sp)
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

            Card(
                modifier = Modifier.fillMaxWidth().clickable { onPostClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(postLabel, fontWeight = FontWeight.Bold,
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
