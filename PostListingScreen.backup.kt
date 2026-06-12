package com.nyumbahub.feature.post.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.post.viewmodel.PostListingViewModel
import com.nyumbahub.feature.post.viewmodel.PostUiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListingScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onLoginRequired: () -> Unit = {},
    viewModel: PostListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var step by remember { mutableIntStateOf(0) }
    var listingPurpose by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var bedrooms by remember { mutableStateOf("") }
    var bathrooms by remember { mutableStateOf("") }
    var sizeSqm by remember { mutableStateOf("") }
    var furnished by remember { mutableStateOf("") }
    var rentPeriod by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var neighbourhood by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploadingImages by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is PostUiState.Success) onSuccess()
    }

    fun uploadImagesAndSubmit(uid: String) {
        if (selectedImages.isEmpty()) {
            viewModel.title.value = title
            viewModel.description.value = description
            viewModel.price.value = price
            viewModel.currency.value = currency
            viewModel.bedrooms.value = bedrooms.toIntOrNull() ?: 1
            viewModel.bathrooms.value = bathrooms.toIntOrNull() ?: 1
            viewModel.sizeSqm.value = sizeSqm
            viewModel.city.value = city
            viewModel.district.value = district
            viewModel.neighbourhood.value = neighbourhood
            viewModel.submit(uid)
            return
        }
        isUploadingImages = true
        val storage = FirebaseStorage.getInstance()
        val urls = mutableListOf<String>()
        var completed = 0
        selectedImages.forEach { uri ->
            val ref = storage.reference.child("listings/${uid}/${UUID.randomUUID()}.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        urls.add(downloadUri.toString())
                        completed++
                        if (completed == selectedImages.size) {
                            uploadedImageUrls = urls
                            isUploadingImages = false
                            viewModel.title.value = title
                            viewModel.description.value = description
                            viewModel.price.value = price
                            viewModel.currency.value = currency
                            viewModel.bedrooms.value = bedrooms.toIntOrNull() ?: 1
                            viewModel.bathrooms.value = bathrooms.toIntOrNull() ?: 1
                            viewModel.sizeSqm.value = sizeSqm
                            viewModel.city.value = city
                            viewModel.district.value = district
                            viewModel.neighbourhood.value = neighbourhood
                            viewModel.submit(uid)
                        }
                    }
                }
                .addOnFailureListener {
                    completed++
                    if (completed == selectedImages.size) {
                        isUploadingImages = false
                        viewModel.submit(uid)
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place an Ad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (step == 0) onBack() else step-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when (step) {
            0 -> StepPurpose(padding) { purpose -> listingPurpose = purpose; step = 1 }
            1 -> StepCategory(padding, listingPurpose) { cat -> category = cat; step = 2 }
            2 -> StepDetails(
                padding, listingPurpose, title, description, price, currency,
                bedrooms, bathrooms, sizeSqm, furnished, rentPeriod,
                onTitleChange = { title = it },
                onDescChange = { description = it },
                onPriceChange = { price = it },
                onCurrencyChange = { currency = it },
                onBedroomsChange = { bedrooms = it },
                onBathroomsChange = { bathrooms = it },
                onSizeChange = { sizeSqm = it },
                onFurnishedChange = { furnished = it },
                onRentPeriodChange = { rentPeriod = it },
                onNext = { step = 3 }
            )
            3 -> StepImages(
                padding = padding,
                selectedImages = selectedImages,
                onImagesSelected = { selectedImages = it },
                onNext = { step = 4 }
            )
            4 -> StepLocation(
                padding, city, district, neighbourhood,
                onCityChange = { city = it },
                onDistrictChange = { district = it },
                onNeighbourhoodChange = { neighbourhood = it },
                isLoading = uiState is PostUiState.Loading || isUploadingImages,
                errorMessage = (uiState as? PostUiState.Error)?.message,
                onNext = {
                    val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) onLoginRequired()
                    else uploadImagesAndSubmit(uid)
                }
            )
        }
    }
}

@Composable
fun StepImages(
    padding: PaddingValues,
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesSelected((selectedImages + uris).take(10))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add Photos", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("Add up to 10 photos. First photo will be the cover.",
            style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, NavyPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .background(NavyPrimary.copy(alpha = 0.05f))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null,
                    tint = NavyPrimary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Tap to add photos", color = NavyPrimary, fontWeight = FontWeight.Medium)
                Text("${selectedImages.size}/10 selected", fontSize = 12.sp, color = Color.Gray)
            }
        }

        if (selectedImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(uri).build(),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onImagesSelected(selectedImages - uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                .background(Color.Red, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove",
                                tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        if (uri == selectedImages.first()) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart),
                                color = OrangeAccent,
                                shape = RoundedCornerShape(bottomStart = 8.dp)
                            ) {
                                Text("Cover", fontSize = 9.sp, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
        ) {
            Text(if (selectedImages.isEmpty()) "Skip Photos" else "Next",
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun StepPurpose(padding: PaddingValues, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("What would you like to do?",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Select the purpose of your listing",
            style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))
        listOf(
            Triple("For Rent",  Icons.Default.Home,   "Rent out your property"),
            Triple("For Sale",  Icons.Default.Home,   "Sell your property"),
            Triple("Roommate",  Icons.Default.Person, "Find a roommate")
        ).forEach { (label, icon, sub) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelect(label) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null,
                        tint = NavyPrimary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Text(sub, style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                        tint = Color(0xFFBBBBBB))
                }
            }
        }
    }
}

@Composable
fun StepCategory(padding: PaddingValues, purpose: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Choose the right category:",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        val cats = if (purpose == "For Rent")
            listOf("Residential","Commercial","Rooms For Rent","Monthly Short Term","Daily Short Term")
        else listOf("Residential","Commercial","Land / Plot")
        cats.forEach { cat ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(cat) },
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(cat, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                        tint = Color(0xFFBBBBBB))
                }
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepDetails(
    padding: PaddingValues,
    purpose: String,
    title: String, description: String, price: String, currency: String,
    bedrooms: String, bathrooms: String, sizeSqm: String, furnished: String, rentPeriod: String,
    onTitleChange: (String) -> Unit, onDescChange: (String) -> Unit,
    onPriceChange: (String) -> Unit, onCurrencyChange: (String) -> Unit,
    onBedroomsChange: (String) -> Unit, onBathroomsChange: (String) -> Unit,
    onSizeChange: (String) -> Unit, onFurnishedChange: (String) -> Unit,
    onRentPeriodChange: (String) -> Unit, onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = onTitleChange,
            label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = description, onValueChange = onDescChange,
            label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = price, onValueChange = onPriceChange,
                label = { Text("Price *") }, modifier = Modifier.weight(1f), singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = currency, onValueChange = onCurrencyChange,
                label = { Text("Currency") }, modifier = Modifier.width(90.dp), singleLine = true)
        }
        OutlinedTextField(value = sizeSqm, onValueChange = onSizeChange,
            label = { Text("Size (sqm)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
        Text("Bedrooms *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Studio","1","2","3","4","5","6+").forEach { opt ->
                SelectChip(opt, bedrooms == opt) { onBedroomsChange(opt) }
            }
        }
        Text("Bathrooms", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1","2","3","4","5+").forEach { opt ->
                SelectChip(opt, bathrooms == opt) { onBathroomsChange(opt) }
            }
        }
        Text("Furnishing", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Furnished","Unfurnished","Semi-Furnished").forEach { opt ->
                SelectChip(opt, furnished == opt) { onFurnishedChange(opt) }
            }
        }
        if (purpose == "For Rent") {
            Text("Rent Period", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Yearly","Quarterly","Monthly","Daily").forEach { opt ->
                    SelectChip(opt, rentPeriod == opt) { onRentPeriodChange(opt) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
            Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SelectChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .border(1.dp,
                if (isSelected) NavyPrimary else Color(0xFFCCCCCC),
                RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label,
            color = if (isSelected) NavyPrimary else Color(0xFF444444),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp)
    }
}

@Composable
fun StepLocation(
    padding: PaddingValues,
    city: String, district: String, neighbourhood: String,
    onCityChange: (String) -> Unit, onDistrictChange: (String) -> Unit,
    onNeighbourhoodChange: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Select Location", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text("Help buyers find your property easily",
            style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888))
        OutlinedTextField(value = city, onValueChange = onCityChange,
            label = { Text("City *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, null) })
        OutlinedTextField(value = district, onValueChange = onDistrictChange,
            label = { Text("District *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Place, null) })
        OutlinedTextField(value = neighbourhood, onValueChange = onNeighbourhoodChange,
            label = { Text("Neighbourhood") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Place, null) })
        if (errorMessage != null) {
            Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
        if (isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(color = NavyPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Uploading...", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Submit Listing", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}


