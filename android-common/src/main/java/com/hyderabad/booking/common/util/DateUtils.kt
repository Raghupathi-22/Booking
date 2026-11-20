package com.hyderabad.booking.common.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The backend works in Asia/Kolkata wall-clock time and sends ISO strings without a zone
 * ("2026-11-20T10:30:00"). minSdk is 24, so we avoid java.time and use SimpleDateFormat.
 */
object DateUtils {

    private val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    private fun formatter(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = IST }

    private fun normalize(iso: String): String {
        val s = iso.trim().replace(' ', 'T')
        return when {
            s.length >= 19 -> s.substring(0, 19)
            s.length == 16 -> "$s:00"
            else -> s
        }
    }

    fun parse(iso: String?): Date? {
        if (iso.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = IST }.parse(normalize(iso))
        } catch (e: Exception) {
            null
        }
    }

    /** "10:30 AM" */
    fun time(iso: String?): String = parse(iso)?.let { formatter("h:mm a").format(it) } ?: ""

    /** "Fri, 20 Nov • 10:30 AM" */
    fun dateTime(iso: String?): String = parse(iso)?.let { formatter("EEE, d MMM • h:mm a").format(it) } ?: (iso ?: "")

    /** "Fri, 20 Nov" */
    fun dayLabel(iso: String?): String = parse(iso)?.let { formatter("EEE, d MMM").format(it) } ?: (iso ?: "")

    fun isFuture(iso: String?): Boolean = parse(iso)?.after(Date()) ?: false

    /** The next [count] dates as yyyy-MM-dd (today first). */
    fun nextDays(count: Int): List<String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = IST }
        val cal = Calendar.getInstance(IST)
        return List(count) {
            val s = fmt.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            s
        }
    }

    /** "Mon" + "20" for a yyyy-MM-dd string, used on the date chips. */
    fun chipParts(date: String): Pair<String, String> {
        val d = parse("${date}T00:00:00") ?: return "" to date
        return formatter("EEE").format(d) to formatter("d").format(d)
    }

    /** "09:00:00" -> "9:00 AM" */
    fun clock(hhmm: String?): String {
        if (hhmm.isNullOrBlank()) return ""
        val d = parse("2000-01-01T${hhmm.take(5)}:00") ?: return hhmm
        return formatter("h:mm a").format(d)
    }

    /** Builds "yyyy-MM-ddTHH:mm:00" from separate date and "HH:mm" parts. */
    fun join(date: String, hhmm: String): String = "${date}T${hhmm.take(5)}:00"
}

object Money {
    /** 500.0 -> "₹500", 499.5 -> "₹499.50" */
    fun rupees(value: Double?): String {
        val v = value ?: 0.0
        return if (v % 1.0 == 0.0) "₹${v.toLong()}" else "₹" + String.format(Locale.US, "%.2f", v)
    }
}

val DAYS_OF_WEEK = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

fun String.pretty(): String =
    lowercase(Locale.getDefault()).replace('_', ' ').replaceFirstChar { it.titlecase(Locale.getDefault()) }
