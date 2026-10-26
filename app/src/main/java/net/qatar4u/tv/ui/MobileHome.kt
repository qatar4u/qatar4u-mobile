package net.qatar4u.mobile.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.qatar4u.mobile.apps.AppEntry
import net.qatar4u.mobile.apps.Apps
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MobileHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("mobile", Context.MODE_PRIVATE) }
    var ar by remember { mutableStateOf(prefs.getBoolean("ar", true)) }
    var wallpaper by remember { mutableStateOf(prefs.getString("wallpaper", "gold") ?: "gold") }
    var customUri by remember { mutableStateOf(prefs.getString("wallpaper_uri", "") ?: "") }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()) }
    var favoritesOnly by remember { mutableStateOf(false) }
    val pickWallpaper = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ok = runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.isSuccess
            if (ok) {
                customUri = uri.toString(); wallpaper = "custom"
                prefs.edit().putString("wallpaper_uri", customUri).putString("wallpaper", wallpaper).apply()
            } else Toast.makeText(context, if (ar) "تعذر حفظ إذن الصورة" else "Could not retain image access", Toast.LENGTH_SHORT).show()
        }
    }
    val wallpaperBitmap by produceState<android.graphics.Bitmap?>(null, customUri, wallpaper) {
        value = if (wallpaper == "custom" && customUri.isNotEmpty()) withContext(Dispatchers.IO) {
            runCatching {
                val uri = android.net.Uri.parse(customUri)
                val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
                if (options.outWidth <= 0 || options.outHeight <= 0) return@runCatching null
                options.inSampleSize = 1
                while (options.outWidth / options.inSampleSize > 1440 || options.outHeight / options.inSampleSize > 2560) options.inSampleSize *= 2
                options.inJustDecodeBounds = false
                context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
        } else if (wallpaper.startsWith("asset:")) withContext(Dispatchers.IO) {
            runCatching {
                val name = wallpaper.removePrefix("asset:")
                val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                context.assets.open("wallpapers/$name").use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
        } else null
    }
    var widgetRevision by remember { mutableStateOf(0) }
    var tab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var refresh by remember { mutableStateOf(0) }
    var appFilter by remember { mutableStateOf(0) }
    var confirmUninstall by remember { mutableStateOf<AppEntry?>(null) }
    var confirmClean by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<AppEntry?>(null) }
    fun label(a: String, e: String) = if (ar) a else e
    fun open(action: String) {
        if (runCatching { context.startActivity(Intent(action)) }.isFailure)
            Toast.makeText(context, label("هذا الخيار غير متاح على الجهاز", "Unavailable on this device"), Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(refresh) { apps = withContext(Dispatchers.IO) { Apps.installed(context).sortedBy { it.label.lowercase() } } }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Q4uTheme(Q4u.Gold) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides
                if (ar) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
        val colors = when (wallpaper) {
            "maroon" -> listOf(Color(0xFF39152A), Color(0xFF080B12))
            "blue" -> listOf(Color(0xFF142E46), Color(0xFF080B12))
            "plain" -> listOf(Q4u.Background, Q4u.Background)
            else -> listOf(Color(0xFF352D1D), Q4u.Background)
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors))) {
        wallpaperBitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.30f)))
        Scaffold(containerColor = Color.Transparent, bottomBar = {
            NavigationBar {
                listOf(label("التطبيقات", "Apps"), label("العناية", "Care"), label("الإدارة", "Manager"), label("الإعدادات", "Settings")).forEachIndexed { i, name ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i; refresh++ }, icon = { Text(listOf("◉", "✦", "▦", "⚙")[i]) }, label = { Text(name) })
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Text("Qatar4u", color = Q4u.Gold, style = MaterialTheme.typography.titleLarge)
                Text("Mobile", style = MaterialTheme.typography.titleMedium)
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(SimpleDateFormat("EEE, d MMM • HH:mm", if (ar) Locale("ar") else Locale.ENGLISH).format(Date()), color = Q4u.TextSecondary)
                    }
                    if (tab == 0) {
                        item(span = { GridItemSpan(maxLineSpan) }) { MobileWidgets(ar, widgetRevision) }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(label("ابحث عن تطبيق", "Search apps")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = !favoritesOnly }, label = { Text(label("المفضلة فقط", "Favorites only")) })
                        }
                        items(apps.filter { it.label.contains(query, true) && (!favoritesOnly || it.pkg in favorites) }, key = { it.pkg }) { app ->
                            val bitmap by produceState<android.graphics.Bitmap?>(null, app.pkg) {
                                value = withContext(Dispatchers.IO) { Apps.icon(context, app.pkg, 112) }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Box(Modifier.size(64.dp).clip(CircleShape).background(Q4u.Surface).clickable { Apps.launch(context, app) }, contentAlignment = Alignment.Center) {
                                    bitmap?.let { Image(it.asImageBitmap(), app.label, Modifier.fillMaxSize().padding(8.dp)) }
                                }
                                Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { selected = app }, contentPadding = PaddingValues(0.dp)) { Text(label("إدارة", "Manage"), style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) { TextButton(onClick = { refresh++ }) { Text(label("تحديث التطبيقات", "Refresh apps")) } }
                    } else if (tab == 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val info = remember(refresh, tab) {
                                val mem = ActivityManager.MemoryInfo()
                                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mem)
                                val disk = StatFs(context.filesDir.absolutePath)
                                fun gb(v: Long) = String.format(Locale.US, "%.1f GB", v / 1073741824.0)
                                "${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}\n" +
                                label("الذاكرة المتاحة / الإجمالي", "Available / total RAM") + ": ${gb(mem.availMem)} / ${gb(mem.totalMem)}\n" +
                                label("التخزين المتاح / الإجمالي", "Available / total storage") + ": ${gb(disk.availableBytes)} / ${gb(disk.totalBytes)}"
                            }
                            Text(info, style = MaterialTheme.typography.bodyMedium)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { refresh++ }) { Text(label("تحديث بيانات الجهاز", "Refresh device data")) }
                                Button(onClick = { confirmClean = true }) { Text(label("تنظيف مؤقتات هذا التطبيق", "Clear this app's cache")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }) { Text(label("إدارة مساحة الهاتف", "Phone storage")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS) }) { Text(label("إدارة تطبيقات الهاتف", "Manage phone apps")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }) { Text(label("توفير البطارية", "Battery saver")) }
                                Text(label("التنظيف يخص مؤقتات هذا التطبيق فقط. إدارة تطبيقات الهاتف وحذفها تتم من شاشة النظام بعد موافقتك. لا نغلق التطبيقات أو ندّعي تسريع الذاكرة.", "Cleaning removes only this app's temporary files. Manage or remove other apps through Android confirmation. No task killing or RAM boost claims."), style = MaterialTheme.typography.bodySmall)
                                Text(label("للاستخدام الخفيف: استخدم تطبيقات الهاتف الأساسية، وافتح خدمات الفيديو والشبكات من المتصفح عند الحاجة بدل تشغيل عدة تطبيقات في الخلفية.", "For lighter use, keep essential phone apps and open occasional video or social services in your browser."), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else if (tab == 2) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(label("إدارة التطبيقات", "App manager"), style = MaterialTheme.typography.titleLarge)
                                Text(label("التطبيقات المرئية للتطبيق فقط؛ افتح إعدادات النظام للقائمة الكاملة. التصنيف لا يعني أن التطبيق غير ضروري.", "Only apps visible to this launcher are listed. Open system settings for the full list. A category does not mean an app is unnecessary."), style = MaterialTheme.typography.bodySmall)
                                OutlinedTextField(query, { query = it }, label = { Text(label("ابحث بالاسم أو الحزمة", "Search name or package")) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(label("الكل", "All"), label("المستخدم", "User"), label("النظام", "System")).forEachIndexed { i, text ->
                                        FilterChip(selected = appFilter == i, onClick = { appFilter = i }, label = { Text(text) })
                                    }
                                }
                            }
                        }
                        val filtered = apps.filter { (appFilter == 0 || (appFilter == 2) == it.isSystem) && (it.label.contains(query, true) || it.pkg.contains(query, true)) }
                        item(span = { GridItemSpan(maxLineSpan) }) { Text(label("النتائج: ", "Results: ") + filtered.size) }
                        items(filtered, key = { "manage:" + it.pkg }, span = { GridItemSpan(maxLineSpan) }) { app ->
                            OutlinedCard(onClick = { selected = app }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                                    Text(app.pkg, style = MaterialTheme.typography.bodySmall)
                                    Text(if (app.isSystem) label("تطبيق نظام • إدارة من Android", "System app • manage in Android") else label("تطبيق مستخدم", "User app"), color = Q4u.Gold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            OutlinedButton(onClick = { open(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS) }) { Text(label("قائمة Android الكاملة", "Full Android app list")) }
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { ar = !ar; prefs.edit().putBoolean("ar", ar).apply() }) { Text("العربية / English") }
                                OutlinedButton(onClick = { open(Settings.ACTION_HOME_SETTINGS) }) { Text(label("اختيار الشاشة الرئيسية", "Choose home launcher")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }) { Text(label("خيارات المطوّر", "Developer options")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) }) { Text(label("التطبيقات الافتراضية", "Default apps")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_ACCESSIBILITY_SETTINGS) }) { Text(label("إمكانية الوصول", "Accessibility")) }
                                OutlinedButton(onClick = { open(Settings.ACTION_SETTINGS) }) { Text(label("إعدادات Android", "Android settings")) }
                                Text(label("الوضع الخفيف الافتراضي: خلفية ثابتة، بدون فيديو أو حركة مستمرة، وبدون تحديث طقس في الخلفية. الأيقونات دائرية داخل هذه الواجهة.", "Light defaults: static background, no looping video or animation, and no background weather polling. Icons are circular inside this launcher."))
                                Text(label("الخلفية", "Wallpaper"), style = MaterialTheme.typography.titleMedium)
                                listOf("gold" to label("ذهبي هادئ", "Soft gold"), "maroon" to label("عنابي", "Maroon"), "blue" to label("أزرق ليلي", "Midnight blue"), "plain" to label("أسود سادة", "Plain black")).forEach { (id, title) ->
                                    FilterChip(selected = wallpaper == id, onClick = { wallpaper = id; prefs.edit().putString("wallpaper", id).apply() }, label = { Text(title) })
                                }
                                listOf(
                                    "0-p01-west-bay-night.jpg" to label("الخليج الغربي", "West Bay"),
                                    "0-p02-lusail-katara-towers.jpg" to label("أبراج كتارا", "Katara Towers"),
                                    "0-p03-katara-amphitheatre.jpg" to label("كتارا", "Katara"),
                                    "0-p04-dhow-sunset.jpg" to label("غروب البحر", "Dhow sunset"),
                                    "0-p05-souq-lanterns.jpg" to label("فوانيس السوق", "Souq lanterns")
                                ).forEach { (file, title) ->
                                    FilterChip(selected = wallpaper == "asset:$file", onClick = {
                                        wallpaper = "asset:$file"; prefs.edit().putString("wallpaper", wallpaper).apply()
                                    }, label = { Text(title) })
                                }
                                OutlinedButton(onClick = { pickWallpaper.launch(arrayOf("image/*")) }) { Text(label("اختيار صورة من الجهاز", "Choose image from device")) }
                                if (wallpaper == "custom" && wallpaperBitmap == null) Text(label("الصورة غير متاحة؛ تظهر الخلفية الافتراضية. اختر صورة أخرى.", "Image unavailable; default background shown. Choose another image."))
                                MobileWidgetSettings(ar) { widgetRevision++ }
                                Text("Qatar4u • 1.0.0")
                            }
                        }
                    }
                }
            }
        }
        }
        if (confirmClean) AlertDialog(onDismissRequest = { confirmClean = false }, title = { Text(label("تنظيف المؤقتات؟", "Clear temporary files?")) }, text = { Text(label("يحذف ملفات cache الخاصة بـ Qatar4u Mobile فقط، دون الصور أو الحسابات أو بيانات التطبيقات الأخرى.", "Deletes only Qatar4u Mobile cache, keeping photos, accounts and other apps' data.")) }, confirmButton = {
            TextButton(onClick = {
                confirmClean = false
                scope.launch {
                    val ok = withContext(Dispatchers.IO) { runCatching { context.cacheDir.listFiles()?.map { it.deleteRecursively() }?.all { it } ?: true }.getOrDefault(false) }
                    Toast.makeText(context, if (ok) label("تم تنظيف المؤقتات", "Cache cleared") else label("تعذر حذف بعض الملفات", "Some files could not be removed"), Toast.LENGTH_SHORT).show()
                    refresh++
                }
            }) { Text(label("تنظيف", "Clear")) }
        }, dismissButton = { TextButton(onClick = { confirmClean = false }) { Text(label("إلغاء", "Cancel")) } })
        confirmUninstall?.let { app ->
            AlertDialog(onDismissRequest = { confirmUninstall = null }, title = { Text(label("إلغاء تثبيت التطبيق؟", "Uninstall app?")) }, text = {
                Text(app.label + "\n" + app.pkg + "\n" + label("قد تُحذف بيانات التطبيق المحلية. إعادة تثبيته لا تضمن استعادتها. سيطلب Android تأكيدًا آخر.", "Local app data may be deleted and reinstalling may not restore it. Android will ask for another confirmation."))
            }, confirmButton = { TextButton(onClick = {
                confirmUninstall = null
                if (!Apps.uninstall(context, app.pkg)) Toast.makeText(context, label("تعذر فتح إلغاء التثبيت", "Could not open uninstall"), Toast.LENGTH_SHORT).show()
            }) { Text(label("متابعة إلى Android", "Continue to Android")) } }, dismissButton = { TextButton(onClick = { confirmUninstall = null }) { Text(label("إلغاء", "Cancel")) } })
        }
        selected?.let { app ->
            AlertDialog(onDismissRequest = { selected = null }, title = { Text(app.label) }, text = {
                Column {
                    Text(app.pkg, style = MaterialTheme.typography.bodySmall)
                    Text(if (app.isSystem) label("تطبيق نظام: راجع وظائفه قبل تغيير إعداداته. التعطيل، إن كان متاحًا، يتم من شاشة Android.", "System app: review its purpose first. Disable only through Android if available.") else label("يمكن إلغاء تثبيت التطبيق بعد تأكيدك. قد تفقد بياناته المحلية.", "Uninstall requires your confirmation and may delete its local data."))
                    TextButton(onClick = {
                        if (!Apps.openAppInfo(context, app.pkg)) Toast.makeText(context, label("تعذر فتح المعلومات", "Could not open app info"), Toast.LENGTH_SHORT).show()
                        selected = null
                    }) { Text(label("الأذونات والتخزين والبطارية", "Permissions, storage and battery")) }
                    TextButton(onClick = {
                        favorites = if (app.pkg in favorites) favorites - app.pkg else favorites + app.pkg
                        prefs.edit().putStringSet("favorites", favorites).apply()
                    }) { Text(if (app.pkg in favorites) label("إزالة من المفضلة", "Remove favorite") else label("إضافة للمفضلة", "Add favorite")) }
                    if (!app.isSystem) TextButton(onClick = { selected = null; confirmUninstall = app }) { Text(label("إلغاء التثبيت…", "Uninstall…"), color = Q4u.Danger) }
                }
            }, confirmButton = { TextButton(onClick = { selected = null }) { Text(label("إغلاق", "Close")) } })
        }
        }
    }
}
