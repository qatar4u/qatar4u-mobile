package net.qatar4u.mobile.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.qatar4u.mobile.data.Cities
import net.qatar4u.mobile.prayer.PrayerTimes
import net.qatar4u.mobile.weather.Weather
import net.qatar4u.mobile.weather.WeatherNow
import java.time.Instant
import java.time.ZoneId

@Composable
fun MobileWidgetSettings(ar: Boolean, changed: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mobile", Context.MODE_PRIVATE) }
    var cityId by remember { mutableStateOf(prefs.getString("city", "doha") ?: "doha") }
    var enabled by remember { mutableStateOf(prefs.getBoolean("weather", false)) }
    var expanded by remember { mutableStateOf(false) }
    fun text(a: String, e: String) = if (ar) a else e
    Text(text("الصلاة والطقس", "Prayer and weather"), style = MaterialTheme.typography.titleMedium)
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(Cities.byId(cityId)?.name(if (ar) "ar" else "en") ?: cityId) }
        DropdownMenu(expanded, { expanded = false }) {
            Cities.all.forEach { city ->
                DropdownMenuItem(text = { Text(city.name(if (ar) "ar" else "en")) }, onClick = {
                    cityId = city.id; prefs.edit().putString("city", cityId).apply(); expanded = false; changed()
                })
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Switch(checked = enabled, onCheckedChange = { enabled = it; prefs.edit().putBoolean("weather", it).apply(); changed() })
        Text(text("الطقس الاختياري", "Optional weather"))
    }
    Text(text("الصلاة تُحسب محليًا بطريقة أم القرى. عند تفعيل الطقس تُرسل إحداثيات مركز المدينة المختارة إلى Open-Meteo، ويُحدّث مرة كل ساعة أثناء فتح الواجهة فقط.", "Prayer uses local Umm al-Qura calculation. Enabling weather sends the selected city center coordinates to Open-Meteo; it updates hourly only while the launcher is open."), style = MaterialTheme.typography.bodySmall)
}

@Composable
fun MobileWidgets(ar: Boolean, revision: Int) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mobile", Context.MODE_PRIVATE) }
    val city = remember(revision) { Cities.byId(prefs.getString("city", "doha") ?: "doha") ?: Cities.DOHA }
    val enabled = remember(revision) { prefs.getBoolean("weather", false) }
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var weather by remember(city.id) { mutableStateOf<WeatherNow?>(
        prefs.getString("weather_json_${city.id}", null)?.let { Weather.parse(it, prefs.getLong("weather_at_${city.id}", 0)) }
    ) }
    var busy by remember { mutableStateOf(false) }
    var error by remember(city.id) { mutableStateOf(false) }
    suspend fun refresh() {
        if (busy) return
        busy = true
        try {
            val json = Weather.fetch(city.lat, city.lon, city.zone)
            val at = System.currentTimeMillis()
            weather = requireNotNull(Weather.parse(json, at))
            prefs.edit().putString("weather_json_${city.id}", json).putLong("weather_at_${city.id}", at).apply()
            error = false
        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled
        } catch (_: Exception) { error = true
        } finally { busy = false }
    }
    LaunchedEffect(owner, city.id, enabled) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = System.currentTimeMillis()
                if (enabled && !error && now - (weather?.fetchedAt ?: 0L) >= 3_600_000L) refresh()
                delay(60_000L - System.currentTimeMillis() % 60_000L)
            }
        }
    }
    val time = Instant.ofEpochMilli(now).atZone(ZoneId.of(city.zone))
    val times = remember(city.id, time.toLocalDate()) { PrayerTimes.minutes(time.toLocalDate(), city.lat, city.lon, ZoneId.of(city.zone)) }
    val next = PrayerTimes.next(times, time.hour * 60 + time.minute)
    val names = if (ar) listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء") else listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(city.name(if (ar) "ar" else "en") + " • " + time.toLocalTime().withSecond(0).withNano(0), style = MaterialTheme.typography.titleMedium)
            Text((if (ar) "الصلاة القادمة: " else "Next prayer: ") + names[next] + " • " + PrayerTimes.clock(times[next], true), color = Q4u.Gold)
            Text(names.indices.joinToString("  •  ") { names[it] + " " + PrayerTimes.clock(times[it], true) }, style = MaterialTheme.typography.bodySmall)
            if (enabled) {
                weather?.let { current ->
                    Text("${current.temperatureC}°C" + if (error || now - current.fetchedAt > Weather.STALE_MS) (if (ar) " • قراءة محفوظة قديمة" else " • stale cached reading") else "")
                }
                if (error) Text(if (ar) "تعذر تحديث الطقس. أعد المحاولة عند الاتصال." else "Weather update failed. Retry when connected.")
                TextButton(onClick = { scope.launch { refresh() } }, enabled = !busy) { Text(if (busy) (if (ar) "جارٍ التحديث…" else "Updating…") else (if (ar) "تحديث الطقس" else "Refresh weather")) }
            }
        }
    }
}
