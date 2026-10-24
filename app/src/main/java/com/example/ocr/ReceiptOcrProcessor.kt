package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrExtractionResult(
    val possibleMerchants: List<String>,
    val possiblePrices: List<Double>,
    val possibleDates: List<Long>,
    val rawText: String,
    val bestMerchant: String? = possibleMerchants.firstOrNull(),
    val bestPrice: Double? = possiblePrices.firstOrNull(),
    val bestDate: Long? = possibleDates.firstOrNull()
)

object ReceiptOcrProcessor {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun processUri(context: Context, uri: Uri): OcrExtractionResult = withContext(Dispatchers.IO) {
        val bitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to decode receipt image: ${e.message}", e)
        }
        processBitmap(bitmap)
    }

    suspend fun processBitmap(bitmap: Bitmap): OcrExtractionResult = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

        val rawText = visionText.text
        val lines = visionText.textBlocks.flatMap { it.lines.map { line -> line.text.trim() } }

        // 1. Extract Merchants
        val merchants = extractMerchants(lines)

        // 2. Extract Prices
        val prices = extractPrices(lines, rawText)

        // 3. Extract Dates
        val dates = extractDates(rawText)

        OcrExtractionResult(
            possibleMerchants = merchants,
            possiblePrices = prices,
            possibleDates = dates,
            rawText = rawText
        )
    }

    private fun extractMerchants(lines: List<String>): List<String> {
        val candidates = linkedSetOf<String>()
        val skipKeywords = setOf(
            "receipt", "invoice", "welcome", "tax", "order", "store",
            "cashier", "register", "terminal", "customer", "copy",
            "thank you", "thanks", "subtotal", "total", "visa",
            "mastercard", "amex", "card", "debit", "credit", "approval"
        )

        // Look at the top 8 lines of the receipt for merchant names
        val topLines = lines.take(8)
        for (line in topLines) {
            val clean = line.replace(Regex("[^a-zA-Z0-9 &.'-]"), "").trim()
            if (clean.length in 3..35) {
                val lower = clean.lowercase()
                val isExcluded = skipKeywords.any { lower.contains(it) } || clean.all { it.isDigit() }
                if (!isExcluded) {
                    candidates.add(clean)
                }
            }
        }

        return candidates.toList().take(5)
    }

    private fun extractPrices(lines: List<String>, rawText: String): List<Double> {
        val prices = linkedSetOf<Double>()

        // Look for lines that explicitly mention total or balance
        val totalKeywords = listOf("total", "amount", "balance", "net", "due", "paid", "charge", "subtotal")
        val priceRegex = Pattern.compile("(?:\\$|USD|EUR|€|£)?\\s*([0-9]{1,4}[.,][0-9]{2})\\b")

        // First pass: lines with total/amount keywords
        for (line in lines) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                val matcher = priceRegex.matcher(line)
                while (matcher.find()) {
                    val rawVal = matcher.group(1)?.replace(",", ".")
                    val price = rawVal?.toDoubleOrNull()
                    if (price != null && price > 0.0 && price < 50000.0) {
                        prices.add(price)
                    }
                }
            }
        }

        // Second pass: all price numbers across entire text
        val globalMatcher = priceRegex.matcher(rawText)
        while (globalMatcher.find()) {
            val rawVal = globalMatcher.group(1)?.replace(",", ".")
            val price = rawVal?.toDoubleOrNull()
            if (price != null && price > 0.0 && price < 50000.0) {
                prices.add(price)
            }
        }

        // Sort: prices found near 'total' keywords come first, then largest amounts
        return prices.sortedDescending().take(6)
    }

    private fun extractDates(rawText: String): List<Long> {
        val dates = linkedSetOf<Long>()
        val formats = listOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "MM-dd-yyyy",
            "yyyy/MM/dd",
            "dd.MM.yyyy",
            "MMM d, yyyy",
            "MMMM d, yyyy",
            "d MMM yyyy",
            "MM/dd/yy",
            "dd/MM/yy"
        )

        val datePatterns = listOf(
            "\\b(20[123][0-9][-/.](?:0[1-9]|1[0-2])[-/.](?:0[1-9]|[12][0-9]|3[01]))\\b",
            "\\b((?:0[1-9]|1[0-2])[-/.](?:0[1-9]|[12][0-9]|3[01])[-/.](?:20[123][0-9]|[12][0-9]))\\b",
            "\\b((?:0[1-9]|[12][0-9]|3[01])[-/.](?:0[1-9]|1[0-2])[-/.](?:20[123][0-9]|[12][0-9]))\\b",
            "\\b([A-Za-z]{3,9}\\s+[0-9]{1,2},?\\s+20[123][0-9])\\b",
            "\\b([0-9]{1,2}\\s+[A-Za-z]{3,9}\\s+20[123][0-9])\\b"
        )

        val now = System.currentTimeMillis()
        val minSensibleDate = now - (3650L * 24 * 60 * 60 * 1000) // 10 years ago
        val maxSensibleDate = now + (3650L * 24 * 60 * 60 * 1000) // 10 years future

        for (regexStr in datePatterns) {
            val pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(rawText)
            while (matcher.find()) {
                val match = matcher.group(1)?.replace(",", "")?.trim() ?: continue
                for (fmt in formats) {
                    try {
                        val sdf = SimpleDateFormat(fmt, Locale.US)
                        sdf.isLenient = false
                        val parsed = sdf.parse(match)
                        if (parsed != null && parsed.time in minSensibleDate..maxSensibleDate) {
                            dates.add(parsed.time)
                            break
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        return dates.toList().sortedDescending().take(4)
    }
}
