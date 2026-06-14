package com.example.data

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.*

enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
    MWL(18.0, 17.0),
    ISNA(15.0, 15.0),
    EGYPT(19.5, 17.5),
    KARACHI(18.0, 18.0),
    TEHRAN(17.7, 14.0)
}

enum class AsrJuristic(val shadowFactor: Int) {
    STANDARD(1), // Shafi, Maliki, Hanbali
    HANAFI(2)
}

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val rawTimes: Map<String, Double>
)

object PrayerTimeCalculator {

    private fun d2r(d: Double): Double = d * Math.PI / 180.0
    private fun r2d(r: Double): Double = r * 180.0 / Math.PI

    private fun fixHour(h: Double): Double {
        if (h.isNaN() || h.isInfinite()) return 12.0
        var a = h % 24.0
        if (a < 0.0) a += 24.0
        return a
    }

    private fun fixAngle(a: Double): Double {
        if (a.isNaN() || a.isInfinite()) return 0.0
        var x = a % 360.0
        if (x < 0.0) x += 360.0
        return x
    }

    fun calculateTimes(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        method: CalculationMethod = CalculationMethod.MWL,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD
    ): PrayerTimes {
        val latClamped = latitude.coerceIn(-89.9, 89.9)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val timezoneOffset = TimeZone.getDefault().getOffset(date.time) / 3600000.0

        var y = year.toDouble()
        var m = month.toDouble()
        if (m <= 2) {
            y -= 1.0
            m += 12.0
        }
        val A = floor(y / 100.0)
        val B = 2.0 - A + floor(A / 4.0)
        val jd = floor(365.25 * (y + 4716.0)) + floor(30.6001 * (m + 1.0)) + day.toDouble() + B - 1524.5 - longitude / 360.0

        val T = (jd - 2451545.0) / 36525.0
        val Q = fixAngle(280.46607 + 36000.7698 * T)
        val G = fixAngle(357.5291 + 35999.0503 * T)
        val L = fixAngle(Q + 1.9146 * sin(d2r(G)) + 0.0199 * sin(d2r(2.0 * G)))
        val R = 23.439291 - 0.0130041 * T
        var alpha = r2d(atan2(cos(d2r(R)) * sin(d2r(L)), cos(d2r(L))))
        alpha = fixAngle(alpha) / 15.0
        val equationOfTime = (Q / 15.0 - alpha) * 60.0
        val sinDecl = (sin(d2r(R)) * sin(d2r(L))).coerceIn(-1.0, 1.0)
        val decl = r2d(asin(sinDecl))

        val transit = 12.0 - longitude / 15.0 - equationOfTime / 60.0 + timezoneOffset

        fun hourAngle(angle: Double): Double {
            val num = -sin(d2r(angle)) - sin(d2r(latClamped)) * sinDecl
            val den = cos(d2r(latClamped)) * cos(d2r(decl))
            if (den == 0.0) return 0.0
            val ratio = (num / den).coerceIn(-1.0, 1.0)
            val valRad = acos(ratio)
            return r2d(valRad) / 15.0
        }

        val shadow = asrJuristic.shadowFactor.toDouble()
        val asrAngle = r2d(atan(1.0 / (shadow + tan(d2r(abs(latClamped - decl))))))
        val asrHA = hourAngle(asrAngle)

        val formatTime = { d: Double ->
            if (d.isNaN() || d.isInfinite()) {
                "00:00"
            } else {
                val h = floor(d).toInt()
                val mn = round((d - h) * 60.0).toInt()
                val safeMin = if (mn == 60) 0 else mn
                val safeH = if (mn == 60) (h + 1) % 24 else h
                String.format("%02d:%02d", safeH, safeMin)
            }
        }

        val fajrD = transit - hourAngle(method.fajrAngle)
        val sunriseD = transit - hourAngle(0.833)
        val dhuhrD = transit
        val asrD = transit + asrHA
        val maghribD = transit + hourAngle(0.833)
        val ishaD = transit + hourAngle(method.ishaAngle)

        val rawTimes = mapOf(
            "Fajr" to fixHour(fajrD),
            "Sunrise" to fixHour(sunriseD),
            "Dhuhr" to fixHour(dhuhrD),
            "Asr" to fixHour(asrD),
            "Maghrib" to fixHour(maghribD),
            "Isha" to fixHour(ishaD)
        )

        return PrayerTimes(
            fajr = formatTime(fixHour(fajrD)),
            sunrise = formatTime(fixHour(sunriseD)),
            dhuhr = formatTime(fixHour(dhuhrD)),
            asr = formatTime(fixHour(asrD)),
            maghrib = formatTime(fixHour(maghribD)),
            isha = formatTime(fixHour(ishaD)),
            rawTimes = rawTimes
        )
    }

    // Convert Gregorian date to Hijri Tabular
    fun gregorianToHijri(date: Date = Date()): HijriDate {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var y = year
        var m = month
        if (m < 3) {
            y -= 1
            m += 12
        }
        val A = (y / 100)
        val B = A / 4
        val C = 2 - A + B
        val E = (365.25 * (y + 4716)).toInt()
        val F = (30.6001 * (m + 1)).toInt()
        val jd = C + day + E + F - 1524.5

        val epoch = 1948439.5
        val diff = jd - epoch
        val cycle = floor(diff / 10631.0).toInt()
        val rem = diff % 10631.0
        
        var yearInCycle = 0
        var daysInYearCount = 0.0
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        
        for (i in 1..30) {
            val isLeap = leapYears.contains(i % 30)
            val days = if (isLeap) 355.0 else 354.0
            if (rem < daysInYearCount + days) {
                yearInCycle = i
                break
            }
            daysInYearCount += days
        }
        
        val hijriYear = cycle * 30 + yearInCycle
        val dayInYear = rem - daysInYearCount
        
        var hijriMonth = 1
        var daysInMonthCount = 0.0
        for (i in 1..12) {
            val days = if (i % 2 == 1) 30.0 else if (i == 12 && leapYears.contains(yearInCycle % 30)) 30.0 else 29.0
            if (dayInYear < daysInMonthCount + days) {
                hijriMonth = i
                break
            }
            daysInMonthCount += days
        }
        
        val hijriDay = (dayInYear - daysInMonthCount).toInt() + 1
        return HijriDate(hijriDay, hijriMonth, hijriYear)
    }
}

data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int
) {
    val monthName: String
        get() = when (month) {
            1 -> "Muharram"
            2 -> "Safar"
            3 -> "Rabi' al-Awwal"
            4 -> "Rabi' ath-Thani"
            5 -> "Jumada al-Awwal"
            6 -> "Jumada al-Akhirah"
            7 -> "Rajab"
            8 -> "Sha'ban"
            9 -> "Ramadan"
            10 -> "Shawwal"
            11 -> "Dhu al-Qi'dah"
            12 -> "Dhu al-Hijjah"
            else -> "Unknown"
        }
}
