package com.nyumbahub.feature.search.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun PropertyFilterScreen(
    onBack: () -> Unit,
    onApply: (PropertyFilter) -> Unit
) {
    var selectedType       by remember { mutableStateOf("Rent") }
    var selectedProperty   by remember { mutableStateOf("Any") }
    var selectedBedrooms   by remember { mutableStateOf("Any") }
    var selectedBathrooms  by remember { mutableStateOf("Any") }
    var selectedFurnishing by remember { mutableStateOf("Any") }
    var minPrice           by remember { mutableStateOf("") }
    var maxPrice           by remember { mutableStateOf("") }
    var selectedDistrict   by remember { mutableStateOf("Any") }

    val typeOptions       = listOf("Rent", "Buy", "Off-Plan")
    val propertyOptions   = listOf("Any", "Apartment", "Villa", "House", "Townhouse", "Room")
    val bedroomOptions    = listOf("Any", "Studio", "1", "2", "3", "4", "5+")
    val bathroomOptions   = listOf("Any", "1", "2", "3", "4+")
    val furnishingOptions = listOf("Any", "Furnished", "Unfurnished", "Semi-Furnished")
    val districtOptions   = listOf("Any", "Gasabo", "Kicukiro", "Nyarugenge", "Rubavu", "Musanze", "Huye")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Properties", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        selectedType       = "Rent"
                        selectedProperty   = "Any"
                        selectedBedrooms   = "Any"
                        selectedBathrooms  = "Any"
                        selectedFurnishing = "Any"
                        minPrice           = ""
                        maxPrice           = ""
                        selectedDistrict   = "Any"
                    }) {
                        Text("Reset", color = OrangeAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = {
                        onApply(
                            PropertyFilter(
                                type        = selectedType,
                                propertyType = selectedProperty,
                                bedrooms    = selectedBedrooms,
                                bathrooms   = selectedBathrooms,
                                furnishing  = selectedFurnishing,
                                minPrice    = minPrice.toDoubleOrNull(),
                                maxPrice    = maxPrice.toDoubleOrNull(),
                                district    = selectedDistrict
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Show Results", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FilterSection("Looking to") {
                ChipRow(typeOptions, selectedType) { selectedType = it }
            }

            FilterSection("Property Type") {
                ChipRow(propertyOptions, selectedProperty) { selectedProperty = it }
            }

            FilterSection("District") {
                ChipRow(districtOptions, selectedDistrict) { selectedDistrict = it }
            }

            FilterSection("Price Range (USD)") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            FilterSection("Bedrooms") {
                ChipRow(bedroomOptions, selectedBedrooms) { selectedBedrooms = it }
            }

            FilterSection("Bathrooms") {
                ChipRow(bathroomOptions, selectedBathrooms) { selectedBathrooms = it }
            }

            FilterSection("Furnishing") {
                ChipRow(furnishingOptions, selectedFurnishing) { selectedFurnishing = it }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
        content()
    }
    HorizontalDivider(color = Color(0xFFEEEEEE))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(option) },
                label    = { Text(option, fontSize = 13.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

data class PropertyFilter(
    val type: String         = "Rent",
    val propertyType: String = "Any",
    val bedrooms: String     = "Any",
    val bathrooms: String    = "Any",
    val furnishing: String   = "Any",
    val minPrice: Double?    = null,
    val maxPrice: Double?    = null,
    val district: String     = "Any"
)



