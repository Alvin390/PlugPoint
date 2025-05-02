package com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier



import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.ui.theme.amberBlaze
import com.PlugPoint.plugpoint.ui.theme.charcoalDust
import com.PlugPoint.plugpoint.ui.theme.dimGray
import com.PlugPoint.plugpoint.ui.theme.pineMist
import com.PlugPoint.plugpoint.ui.theme.rubyWine
import com.PlugPoint.plugpoint.ui.theme.scarlet
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar
import kotlin.text.set
import kotlin.toString


@Composable
fun SupplierCommodityScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val commodities = remember { mutableStateListOf<Commodity>() }
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedCommodity by remember { mutableStateOf<Commodity?>(null) }

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
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(vertical = 18.dp, horizontal = 16.dp) // Reduced padding
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
        topBar = { SupplierTopBarCommodity {
            showDialog = true
            isEditing=true} },
        bottomBar = { SupplierBottomNavBar() }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commodities) { commodity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCommodity = commodity
                                showActionDialog = true
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (commodity.imageUri != null) {
                                androidx.compose.foundation.Image(
                                    painter = rememberAsyncImagePainter(commodity.imageUri),
                                    contentDescription = "Commodity Image",
                                    modifier = Modifier.clip(CircleShape),
                                    colorFilter = if (commodity.booked) androidx.compose.ui.graphics.ColorFilter.tint(
                                        Color.Gray
                                    ) else null
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
                                textDecoration = if (commodity.booked) TextDecoration.LineThrough else null,
                                color = if (commodity.booked) Color.Gray else Color.Black
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Quantity: ${commodity.quantity}",
                                    color = if (commodity.booked) Color.Gray else Color.Black,
                                    fontSize = 14.sp,
                                    textDecoration = if (commodity.booked) TextDecoration.LineThrough else null
                                )
                                Text(
                                    text = "${commodity.price} per unit",
                                    fontSize = 14.sp,
                                    color = if (commodity.booked) Color.Gray else Color.Black,
                                    textDecoration = if (commodity.booked) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }
            if (showDialog) {
                PostCommodityDialog(
                    onDismiss = { showDialog = false },
                    onPost = { commodity ->
                        commodities.add(commodity)
                        showDialog = false
                    }
                )
            }

            if (showActionDialog && selectedCommodity != null) {
                ActionDialog(
                    commodity = selectedCommodity!!,
                    onBooked = {
                        val index = commodities.indexOf(selectedCommodity)
                        if (index != -1) {
                            commodities[index] = selectedCommodity!!.copy(booked = true)
                        }
                        showActionDialog = false
                    },
                    onEdit = {
                        showDialog = true
                        showActionDialog = false
                        isEditing= true
                    },
                    onDelete = {
                        commodities.remove(selectedCommodity)
                        showActionDialog = false
                    },
                    onDismiss = { showActionDialog = false }
                )
            }
        }
    }
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
fun PostCommodityDialog(
    onDismiss: () -> Unit,
    onPost: (Commodity) -> Unit,
    initialCommodity: Commodity? = null
) {
    var name by remember { mutableStateOf(initialCommodity?.name ?: "") }
    var quantity by remember { mutableStateOf(initialCommodity?.quantity ?: "") }
    var price by remember { mutableStateOf(initialCommodity?.price?.removePrefix("Ksh ") ?: "") }
    var selectedCurrency by remember { mutableStateOf(if (initialCommodity?.price?.startsWith("Ksh") == true) "Ksh" else "$") }
    var expanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf(initialCommodity?.imageUri?.let { Uri.parse(it) }) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(if (initialCommodity != null) "Edit Commodity" else "Post Commodity") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Image Preview
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(Color.LightGray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.clip(CircleShape)
                        )
                    } else {
                        Text("No Image", color = Color.White)
                    }
                }

                // Upload Photo Button
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                    onClick = {

                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Photo")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Commodity Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .border(1.dp, Color.Gray)
                            .padding(8.dp)
                            .clickable { expanded = true }
                    ) {
                        Text(
                            text = selectedCurrency,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Ksh", "$").forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency) },
                                    onClick = {
                                        selectedCurrency = currency
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank() && price.isNotBlank()) {
                        onPost(
                            Commodity(
                                name = name,
                                quantity = quantity,
                                price = "$selectedCurrency $price",
                                imageUri = imageUri?.toString()
                            )
                        )
                    }
                }
            ) {
                Text(if (initialCommodity != null) "Save Changes" else "Post Commodity")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun SupplierBottomNavBar() {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    var selectedIndex by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = Color(0xFFFFDEAD),
        contentColor = Color.Black,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = label
                    )
                },
                label = { Text(label, fontSize = 12.sp) },
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8C00),
                    selectedTextColor = Color(0xFFFF8C00),
                    indicatorColor = Color(0xFFFFEFD5)
                )
            )
        }
    }
}
data class Commodity(
    val name: String,
    val quantity: String,
    val price: String,
    val imageUri: String? = null,
    val booked: Boolean = false
)

@Preview
@Composable
private fun commodity_screen_prev() {
    SupplierCommodityScreen(rememberNavController())
}
