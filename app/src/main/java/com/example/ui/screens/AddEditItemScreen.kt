package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Category
import com.example.data.VaultItem
import com.example.ocr.OcrExtractionResult
import com.example.ocr.ReceiptOcrProcessor
import com.example.ui.VaultViewModel
import com.example.ui.components.OcrConfirmSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditItemScreen(
    itemId: Long? = null,
    initialScanUri: Uri? = null,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.ELECTRONICS) }
    var storeName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var purchaseDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var warrantyEndDate by remember { mutableStateOf<Long?>(null) }
    var returnDeadlineDate by remember { mutableStateOf<Long?>(null) }
    var documentExpiryDate by remember { mutableStateOf<Long?>(null) }
    var customReminderDate by remember { mutableStateOf<Long?>(null) }
    var reminderEnabled by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var receiptImagePath by remember { mutableStateOf<String?>(null) }

    var isTitleError by remember { mutableStateOf(false) }
    var isOcrScanning by remember { mutableStateOf(false) }
    var ocrResult by remember { mutableStateOf<OcrExtractionResult?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    // If editing existing item, load its values
    if (itemId != null && itemId > 0L) {
        val existingItemState = viewModel.getItem(itemId).collectAsStateWithLifecycle(initialValue = null)
        val existingItem = existingItemState.value
        LaunchedEffect(existingItem) {
            existingItem?.let { item ->
                title = item.title
                category = item.getCategoryEnum()
                storeName = item.storeName
                priceText = if (item.price > 0) String.format(Locale.US, "%.2f", item.price) else ""
                currency = item.currency
                purchaseDate = item.purchaseDate
                warrantyEndDate = item.warrantyEndDate
                returnDeadlineDate = item.returnDeadlineDate
                documentExpiryDate = item.documentExpiryDate
                customReminderDate = item.customReminderDate
                reminderEnabled = item.reminderEnabled
                notes = item.notes
                serialNumber = item.serialNumber
                receiptImagePath = item.receiptImagePath
            }
        }
    }

    // Function to run OCR on image URI and prompt user with candidate selections
    fun runOcrOnUri(uri: Uri) {
        isOcrScanning = true
        coroutineScope.launch {
            try {
                val result = ReceiptOcrProcessor.processUri(context, uri)
                ocrResult = result
            } catch (e: Exception) {
                Toast.makeText(context, "OCR scanning failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isOcrScanning = false
            }
        }
    }

    // Auto-scan initial URI if passed into the screen
    LaunchedEffect(initialScanUri) {
        initialScanUri?.let { uri ->
            receiptImagePath = uri.toString()
            runOcrOnUri(uri)
        }
    }

    // Zero-permission Photo Picker from Google Play Policy guidelines
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            coroutineScope.launch {
                // Copy locally to private app directory to keep user records entirely on-device
                val localPath = withContext(Dispatchers.IO) {
                    try {
                        val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
                        val file = File(receiptsDir, "receipt_${UUID.randomUUID()}.jpg")
                        context.contentResolver.openInputStream(sourceUri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        file.absolutePath
                    } catch (e: Exception) {
                        null
                    }
                }
                receiptImagePath = localPath ?: sourceUri.toString()
                runOcrOnUri(sourceUri)
            }
        }
    }

    fun showDatePicker(initialMillis: Long?, onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        if (initialMillis != null && initialMillis > 0) {
            cal.timeInMillis = initialMillis
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth, 12, 0, 0)
                onSelected(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (itemId != null && itemId > 0) "Edit Record" else "Add Record")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("add_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                isTitleError = true
                                Toast.makeText(context, "Item title is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val priceVal = priceText.toDoubleOrNull() ?: 0.0
                            val newItem = VaultItem(
                                id = itemId ?: 0L,
                                title = title.trim(),
                                category = category.name,
                                storeName = storeName.trim(),
                                price = priceVal,
                                currency = currency,
                                purchaseDate = purchaseDate,
                                warrantyEndDate = warrantyEndDate,
                                returnDeadlineDate = returnDeadlineDate,
                                documentExpiryDate = documentExpiryDate,
                                customReminderDate = customReminderDate,
                                reminderEnabled = reminderEnabled,
                                receiptImagePath = receiptImagePath,
                                notes = notes.trim(),
                                serialNumber = serialNumber.trim(),
                                updatedTimestamp = System.currentTimeMillis()
                            )
                            viewModel.saveItem(newItem) { savedId ->
                                Toast.makeText(context, "Record saved successfully", Toast.LENGTH_SHORT).show()
                                onSaved(savedId)
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_record_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Photo & OCR trigger Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Receipt & OCR Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attach a photo or scan with on-device ML Kit to automatically detect dates, prices, and merchant.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!receiptImagePath.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = receiptImagePath,
                                contentDescription = "Receipt Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { receiptImagePath = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove receipt")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("attach_receipt_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (receiptImagePath == null) "Attach Photo" else "Replace Photo")
                        }

                        if (!receiptImagePath.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    val uri = Uri.parse(receiptImagePath)
                                    runOcrOnUri(uri)
                                },
                                enabled = !isOcrScanning,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rescan_ocr_button")
                            ) {
                                if (isOcrScanning) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run OCR")
                                }
                            }
                        }
                    }
                }
            }

            // Core Item Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Item Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) isTitleError = false
                        },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. MacBook Pro, Dyson Vacuum, Passport") },
                        isError = isTitleError,
                        supportingText = if (isTitleError) {
                            { Text("Item name is required") }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_title_input"),
                        singleLine = true
                    )

                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("category_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            Category.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Store Name
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store / Merchant / Issuer") },
                        placeholder = { Text("e.g. Best Buy, Apple Store, Target") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_name_input"),
                        singleLine = true
                    )

                    // Price & Currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price Paid") },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("price_input"),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = !currencyExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                listOf("USD", "EUR", "GBP", "CAD").forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            currency = curr
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dates & Deadlines Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Dates & Warranty Periods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Purchase Date
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker(purchaseDate) { purchaseDate = it }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Purchase Date", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(VaultItem.formatDate(purchaseDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDatePicker(purchaseDate) { purchaseDate = it } }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Purchase Date")
                        }
                    }

                    // Warranty End Date
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDatePicker(warrantyEndDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(365))) {
                                        warrantyEndDate = it
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Warranty Expiration Date", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    VaultItem.formatDate(warrantyEndDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (warrantyEndDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                if (warrantyEndDate != null) {
                                    IconButton(onClick = { warrantyEndDate = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear warranty date")
                                    }
                                }
                                IconButton(onClick = {
                                    showDatePicker(warrantyEndDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(365))) {
                                        warrantyEndDate = it
                                    }
                                }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Warranty Date")
                                }
                            }
                        }

                        // Quick duration chips for warranty
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { warrantyEndDate = purchaseDate + TimeUnit.DAYS.toMillis(365) },
                                label = { Text("+1 Year") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { warrantyEndDate = purchaseDate + TimeUnit.DAYS.toMillis(730) },
                                label = { Text("+2 Years") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { warrantyEndDate = purchaseDate + TimeUnit.DAYS.toMillis(1095) },
                                label = { Text("+3 Years") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { warrantyEndDate = purchaseDate + TimeUnit.DAYS.toMillis(3650) },
                                label = { Text("Lifetime / 10 Yrs") }
                            )
                        }
                    }

                    // Return Window Deadline
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDatePicker(returnDeadlineDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(30))) {
                                        returnDeadlineDate = it
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Return Window Deadline", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    VaultItem.formatDate(returnDeadlineDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (returnDeadlineDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                if (returnDeadlineDate != null) {
                                    IconButton(onClick = { returnDeadlineDate = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear return date")
                                    }
                                }
                                IconButton(onClick = {
                                    showDatePicker(returnDeadlineDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(30))) {
                                        returnDeadlineDate = it
                                    }
                                }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Return Date")
                                }
                            }
                        }

                        // Quick return chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { returnDeadlineDate = purchaseDate + TimeUnit.DAYS.toMillis(14) },
                                label = { Text("+14 Days") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { returnDeadlineDate = purchaseDate + TimeUnit.DAYS.toMillis(30) },
                                label = { Text("+30 Days") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { returnDeadlineDate = purchaseDate + TimeUnit.DAYS.toMillis(60) },
                                label = { Text("+60 Days") }
                            )
                        }
                    }

                    // Document Expiry (For Passports, Licenses, IDs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker(documentExpiryDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(365))) {
                                    documentExpiryDate = it
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Document Expiry Date", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                VaultItem.formatDate(documentExpiryDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (documentExpiryDate != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            if (documentExpiryDate != null) {
                                IconButton(onClick = { documentExpiryDate = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear document expiry date")
                                }
                            }
                            IconButton(onClick = {
                                showDatePicker(documentExpiryDate ?: (purchaseDate + TimeUnit.DAYS.toMillis(365))) {
                                    documentExpiryDate = it
                                }
                            }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Document Expiry")
                            }
                        }
                    }

                    // Reminder Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Deadline Reminders", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("Alerts on local device before expiration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                }
            }

            // Identification & Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Identification & Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number / Asset Tag") },
                        placeholder = { Text("e.g. SN-894109401") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("serial_number_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Coverage Terms") },
                        placeholder = { Text("Add repair terms, contact support numbers, model variations...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("notes_input"),
                        maxLines = 4
                    )
                }
            }
        }
    }

    // OCR Confirmation Bottom Sheet
    ocrResult?.let { result ->
        OcrConfirmSheet(
            extractionResult = result,
            onDismiss = { ocrResult = null },
            onApply = { merchant, price, date ->
                merchant?.let { storeName = it }
                price?.let { priceText = String.format(Locale.US, "%.2f", it) }
                date?.let { purchaseDate = it }
                ocrResult = null
                Toast.makeText(context, "Applied verified OCR values to form", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
