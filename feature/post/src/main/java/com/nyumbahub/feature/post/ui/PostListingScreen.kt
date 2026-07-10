package com.nyumbahub.feature.post.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
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
import com.nyumbahub.core.domain.model.ListingType
import com.nyumbahub.core.domain.model.PropertyType
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.post.viewmodel.PostListingViewModel
import com.nyumbahub.feature.post.viewmodel.PostUiState
import java.util.UUID

private fun mapPropertyType(purpose: String, category: String): PropertyType = when {
    category == "Land / Plot" -> PropertyType.LAND
    category == "Commercial" -> PropertyType.COMMERCIAL
    category == "Rooms For Rent" -> PropertyType.STUDIO
    purpose == "Apartments" -> PropertyType.APARTMENT
    purpose == "Find Roommates" -> PropertyType.STUDIO
    else -> PropertyType.HOUSE
}

private fun mapFurnished(furnished: String): Boolean =
    furnished == "Furnished" || furnished == "Semi-Furnished"

private fun buildAmenities(furnished: String, rentPeriod: String): List<String> {
    val list = mutableListOf<String>()
    if (furnished == "Semi-Furnished") list.add("Semi-Furnished")
    if (rentPeriod.isNotBlank()) list.add("Rent Period: $rentPeriod")
    return list
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListingScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onLoginRequired: () -> Unit = {},
    onMotorsSelected: () -> Unit = {},
    viewModel: PostListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var step by remember { mutableIntStateOf(0) }
    var listingPurpose by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("") }
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
        val doSubmit = { urls: List<String> ->
            viewModel.submit(
                listerId      = uid,
                title         = title,
                description   = description,
                price         = price,
                currency      = currency,
                bedrooms      = bedrooms,
                bathrooms     = bathrooms,
                sizeSqm       = sizeSqm,
                city          = city,
                district      = district,
                neighbourhood = neighbourhood,
                imageUrls     = urls,
                listingType   = if (transactionType == "For Sale") ListingType.SALE else ListingType.RENT,
                propertyType  = mapPropertyType(listingPurpose, category),
                furnished     = mapFurnished(furnished),
                amenities     = buildAmenities(furnished, rentPeriod)
            )
        }
        if (selectedImages.isEmpty()) { doSubmit(emptyList()); return }
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
                            doSubmit(urls)
                        }
                    }
                }
                .addOnFailureListener {
                    completed++
                    if (completed == selectedImages.size) {
                        isUploadingImages = false
                        doSubmit(urls)
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
            0 -> StepPurpose(padding) { purpose ->
                if (purpose == "Motors") {
                    onMotorsSelected()
                } else {
                    listingPurpose = purpose
                    if (purpose == "Find Roommates") {
                        transactionType = "For Rent"
                        step = 2
                    } else {
                        step = 1
                    }
                }
            }
            1 -> StepTransactionType(padding) { t -> transactionType = t; step = 2 }
            2 -> StepCategory(padding, transactionType) { cat -> category = cat; step = 3 }
            3 -> StepDetails(
                padding, transactionType, title, description, price, currency,
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
                onNext = { step = 4 }
            )
            4 -> StepImages(
                padding = padding,
                selectedImages = selectedImages,
                onImagesSelected = { selectedImages = it },
                onNext = { step = 5 }
            )
            5 -> StepLocation(
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
fun StepTransactionType(padding: PaddingValues, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("For Sale or For Rent?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Choose how you want to list this property", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))
        listOf(
            Triple("For Sale", Icons.Default.ShoppingCart, "List this property for sale"),
            Triple("For Rent", Icons.Default.DateRange, "List this property for rent")
        ).forEach { (label, icon, sub) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelect(label) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(NavyPrimary.copy(alpha = 0.09f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(sub, style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFBBBBBB))
                }
            }
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
        Text("Place an Ad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("What are you listing?", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))
        listOf(
            Triple("Motors",         Icons.Default.Star, "Sell, rent or import a vehicle"),
            Triple("Apartments",     Icons.Default.Home,     "Apartments & flats for rent or sale"),
            Triple("Houses",         Icons.Default.Home,          "Houses & villas for rent or sale"),
            Triple("Find Roommates", Icons.Default.Person,        "Find someone to share with")
        ).forEach { (label, icon, sub) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelect(label) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(NavyPrimary.copy(alpha = 0.09f),
                        androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(sub, style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFBBBBBB))
                }
            }
        }
    }
}

@Composable
fun StepCategory(padding: PaddingValues, transactionType: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Choose the right category:",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        val cats = if (transactionType == "For Rent")
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
    transactionType: String,
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
        if (transactionType == "For Rent") {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMotorScreen(
    padding: PaddingValues,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var make         by remember { mutableStateOf("") }
    var model        by remember { mutableStateOf("") }
    var year         by remember { mutableStateOf("") }
    var price        by remember { mutableStateOf("") }
    var currency     by remember { mutableStateOf("USD") }
    var mileage      by remember { mutableStateOf("") }
    var condition    by remember { mutableStateOf("Used") }
    var transmission by remember { mutableStateOf("Automatic") }
    var fuel         by remember { mutableStateOf("Petrol") }
    var color        by remember { mutableStateOf("") }
    var city         by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading  by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf("") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) selectedImages = (selectedImages + uris).take(10)
    }

    fun submit() {
        if (make.isBlank() || model.isBlank() || price.isBlank()) { error = "Make, model and price are required"; return }
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run { error = "Please sign in first"; return }
        isUploading = true
        val storage = FirebaseStorage.getInstance()
        val urls = mutableListOf<String>()
        val doSave = { photoUrls: List<String> ->
            val data = mapOf(
                "make"         to make,
                "model"        to model,
                "year"         to (year.toIntOrNull() ?: 0),
                "price"        to (price.toDoubleOrNull() ?: 0.0),
                "currency"     to currency,
                "mileage"      to (mileage.toIntOrNull() ?: 0),
                "condition"    to condition,
                "transmission" to transmission,
                "fuel"         to fuel,
                "color"        to color,
                "city"         to city,
                "description"  to description,
                "photos"       to photoUrls,
                "listerId"     to uid,
                "status"       to "ACTIVE",
                "createdAt"    to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance().collection("motors").add(data)
                .addOnSuccessListener { isUploading = false; onSuccess() }
                .addOnFailureListener { e -> isUploading = false; error = e.message ?: "Failed" }
        }
        if (selectedImages.isEmpty()) { doSave(emptyList()); return }
        var done = 0
        selectedImages.forEach { uri ->
            val ref = storage.reference.child("motors/$uid/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { dl -> urls.add(dl.toString()); done++; if (done == selectedImages.size) doSave(urls) }
            }.addOnFailureListener { done++; if (done == selectedImages.size) doSave(urls) }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Post a Motor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Make *") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model *") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(1f), singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Mileage (km)") }, modifier = Modifier.weight(1f), singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price *") }, modifier = Modifier.weight(1f), singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency") }, modifier = Modifier.width(90.dp), singleLine = true)
        }
        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

        Text("Condition", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("New","Used","Certified Pre-Owned").forEach { opt ->
                SelectChip(opt, condition == opt) { condition = opt }
            }
        }
        Text("Transmission", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Automatic","Manual").forEach { opt ->
                SelectChip(opt, transmission == opt) { transmission = opt }
            }
        }
        Text("Fuel", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Petrol","Diesel","Electric","Hybrid").forEach { opt ->
                SelectChip(opt, fuel == opt) { fuel = opt }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, NavyPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .background(NavyPrimary.copy(alpha = 0.05f))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(36.dp))
                Text("Add Photos (${selectedImages.size}/10)", color = NavyPrimary, fontWeight = FontWeight.Medium)
            }
        }
        if (selectedImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(model = ImageRequest.Builder(context).data(uri).build(), contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { selectedImages = selectedImages - uri },
                            modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color.Red, RoundedCornerShape(11.dp))) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)

        if (isUploading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
                Text("Submit Motor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}


