package com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier

import CommodityViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.ui.theme.amberBlaze
import com.PlugPoint.plugpoint.ui.theme.dimGray
import com.PlugPoint.plugpoint.ui.theme.green1
import com.PlugPoint.plugpoint.ui.theme.pineMist
import com.PlugPoint.plugpoint.ui.theme.red
import com.PlugPoint.plugpoint.ui.theme.scarlet
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.yellow1
import com.PlugPoint.plugpoint.utilis.CommoditiesViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun SupplierCommodityScreen(
    navController: NavController,
    userId: String,
    imgurViewModel: ImgurViewModel
) {
    val commodityViewModel: CommodityViewModel = viewModel(
        factory = CommoditiesViewModelFactory(imgurViewModel)
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) } // Declare this variable
    var isEditing by remember { mutableStateOf(false) }
    var selectedCommodity by remember { mutableStateOf<Commodity?>(null) }

    val commodities by commodityViewModel.commodities.collectAsState()

    LaunchedEffect(userId) {
        commodityViewModel.fetchCommoditiesFromFirestore(userId)
    }
    @Composable
    fun SupplierTopBarCommodity(onAddClick: () -> Unit) {

        val gradientColors = listOf(
            Color(0xFFFFA500), // orange
            Color(0xFFFF8C00), // darkOrange
            Color(0xFFFF7F50), // coral
            Color(0xFFFF6347)  // tomatoOrange
        )

        Box(
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues())
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(vertical = 18.dp, horizontal = 16.dp) // Reduced padding
                .padding(WindowInsets.statusBars.asPaddingValues()) // Add padding for the status bar
        ) {
            Text(
                text = "PlugPoint",
                fontSize = 29.sp, // Reduced font size
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Cursive
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onAddClick() }
            )
        }
    }

    Scaffold(
        topBar = {
            SupplierTopBarCommodity {
                selectedCommodity = null
                isEditing = false
                showDialog = true // Ensure this updates the state
            }
        },
        bottomBar = { SupplierBottomNavBar(navController, userId) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(commodities, key = { it.id }) { commodity ->
                    CommodityListItem(
                        commodity = commodity,
                        onClick = {
                            selectedCommodity = commodity
                            showDialog = false
                            showActionDialog=true
                        }
                    )
                }
            }

            if (showDialog) {
                PostCommodityDialog(
                    onDismiss = { showDialog = false },
                    onPost = { commodity ->
                        if (isEditing && selectedCommodity != null) {
                            commodityViewModel.updateCommodityInFirestore(
                                userId = userId,
                                commodityId = commodity.id,
                                updatedCommodity = commodity,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Commodity updated successfully")
                                    }
                                    selectedCommodity = null
                                    isEditing = false
                                },
                                onFailure = {},
                                context = context
                            )
                        } else {
                            commodityViewModel.addCommodityToFirestore(
                                commodity = commodity,
                                userId = userId,
                                imageUri = null,
                                context = context,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Commodity added successfully")
                                    }
                                },
                                onFailure = {}
                            )

                        }
                        showDialog = false
                    },
                    initialCommodity = selectedCommodity
                )
            }

            if (showActionDialog && selectedCommodity != null) {
                ActionDialog(
                    commodity = selectedCommodity!!,
                    onBooked = {
                        commodityViewModel.updateCommodityInFirestore(
                            userId = userId,
                            commodityId = selectedCommodity!!.id,
                            updatedCommodity = selectedCommodity!!.copy(booked = true),
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Commodity booked")
                                }
                            },
                            onFailure = {},
                            context = context
                        )
                        showActionDialog = false
                    },
                    onEdit = {
                        isEditing = true
                        showDialog = true
                        showActionDialog = false
                    },
                    onDelete = {
                        commodityViewModel.deleteCommodityFromFirestore(
                            userId = userId,
                            commodityId = selectedCommodity!!.id,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Commodity deleted")
                                }
                            },
                            onFailure = {}
                        )
                        showActionDialog = false
                    },
                    onDismiss = { showActionDialog = false }
                )
            }
        }
    }
}

@Composable
fun PostCommodityDialog(
    onDismiss: () -> Unit,
    onPost: (Commodity) -> Unit,
    initialCommodity: Commodity? = null
) {
    var name by remember { mutableStateOf(initialCommodity?.name ?: "") }
    var quantity by remember { mutableStateOf(initialCommodity?.quantity ?: "") }
    var cost by remember { mutableStateOf(initialCommodity?.cost ?: "") }
    var currency by remember { mutableStateOf(initialCommodity?.currency ?: "Ksh") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFA500), Color(0xFFFF8C00))
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = if (initialCommodity == null) "Add Commodity" else "Edit Commodity",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Image Upload Section
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFA500), Color(0xFFFF8C00))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text("Upload Image", color = Color.White)
                }

                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }

                // Name, Quantity, Cost, and Currency Fields
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                TextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost per unit") })

                // Currency Dropdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Currency: ", fontWeight = FontWeight.Bold)
                    Box {
                        Text(
                            text = currency,
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(8.dp)
                                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ksh") },
                                onClick = {
                                    currency = "Ksh"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("$") },
                                onClick = {
                                    currency = "$"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank() && cost.isNotBlank()) {
                        onPost(
                            Commodity(
                                id = initialCommodity?.id ?: "",
                                name = name,
                                quantity = quantity,
                                cost = cost,
                                currency = currency,
                                imageUri = imageUri?.toString(),
                                booked = initialCommodity?.booked ?: false
                            )
                        )
                    }
                },
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFA500), Color(0xFFFF8C00))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFA500), Color(0xFFFF8C00))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
@Composable
fun ActionDialog(
    commodity: Commodity,
    onBooked: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Actions for ${commodity.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBooked,
                    colors = ButtonDefaults.buttonColors(containerColor = dimGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Booked")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Booked")
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = amberBlaze),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = scarlet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = pineMist),
                onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommodityListItem(commodity: Commodity, onClick: () -> Unit) {
    val greyscaleModifier = if (commodity.booked) {
        Modifier
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .then(greyscaleModifier),
            contentAlignment = Alignment.Center
        ) {
            if (commodity.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(commodity.imageUri),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = if (commodity.booked) {
                        androidx.compose.ui.graphics.ColorFilter.tint(
                            Color.Gray,
                            blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                        )
                    } else {
                        null
                    }
                )
            } else {
                Text("Img", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = commodity.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (commodity.booked) Color.Gray else Color.Black,
                textDecoration = if (commodity.booked) TextDecoration.LineThrough else null
            )
            Text(
                text = "Quantity: ${commodity.quantity}",
                fontSize = 12.sp,
                color = if (commodity.booked) Color.Gray else Color.Black,
                textDecoration = if (commodity.booked) TextDecoration.LineThrough else null
            )
        }
        Text(
            text = "${commodity.currency} ${commodity.cost} per unit",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (commodity.booked) Color.Gray else Color.Black,
            textDecoration = if (commodity.booked) TextDecoration.LineThrough else null,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
