package com.veltrix.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

private val Bg = Color(0xFF090A12)
private val Panel = Color(0xFF151725)
private val Purple = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF35D6E8)
private val Muted = Color(0xFF9AA0B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VeltrixApp() }
    }
}

private enum class Page(val title: String, val icon: ImageVector) {
    Home("Главная", Icons.Default.Home), Mods("Моды", Icons.Default.Extension),
    Builds("Сборки", Icons.Default.Layers), Apps("APK", Icons.Default.Android),
    Files("Файлы", Icons.Default.Folder), Settings("Настройки", Icons.Default.Settings)
}

private data class CatalogItem(val title: String, val category: String, val version: String, val description: String)
private val mods = listOf(
    CatalogItem("OptiFine", "Оптимизация", "1.20.x", "Настройки графики и оптимизация производительности."),
    CatalogItem(" minimap", "Интерфейс", "1.20.x", "Мини-карта для игрового мира."),
    CatalogItem("Shader Pack", "Графика", "1.20.x", "Пакет визуальных эффектов. Совместимость зависит от игры."),
    CatalogItem("Inventory Tweaks", "Удобство", "1.20.x", "Дополнительные настройки инвентаря.")
)
private val builds = listOf(
    CatalogItem("VELTRIX LITE", "Лёгкая сборка", "v1.0", "Минималистичная подборка с упором на производительность."),
    CatalogItem("VELTRIX VISUAL", "Графика", "v1.0", "Визуальная подборка с улучшенным оформлением."),
    CatalogItem("VELTRIX SURVIVAL", "Выживание", "v1.0", "Подборка модификаций для режима выживания.")
)

@Composable
private fun VeltrixApp() {
    var page by remember { mutableStateOf(Page.Home) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    var notice by remember { mutableStateOf("") }
    val context = LocalContext.current

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Purple, secondary = Cyan,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF10121D), contentColor = Color.White) {
                    listOf(Page.Home, Page.Mods, Page.Builds, Page.Apps, Page.Files).forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item; query = "" },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Cyan, selectedTextColor = Cyan,
                                indicatorColor = Purple.copy(alpha = .18f), unselectedIconColor = Muted,
                                unselectedTextColor = Muted)
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("VELTRIX", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("GAME LAUNCHER", color = Cyan, fontSize = 9.sp, letterSpacing = 2.sp)
                    }
                    IconButton(onClick = { page = Page.Settings }) { Icon(Icons.Default.Settings, "Настройки", tint = Muted) }
                }
                AnimatedContent(targetState = page, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "page") { current ->
                    when (current) {
                        Page.Home -> HomeScreen(onOpen = { page = it })
                        Page.Mods -> CatalogScreen("Каталог модов", mods, query, { query = it }, selectedCategory, { selectedCategory = it })
                        Page.Builds -> CatalogScreen("Готовые сборки", builds, query, { query = it }, selectedCategory, { selectedCategory = it })
                        Page.Apps -> ApkScreen(onInstall = { fileName ->
                            try {
                                val folder = File(context.cacheDir, "apks").apply { mkdirs() }
                                val target = File(folder, fileName)
                                context.assets.open("apks/$fileName").use { input -> target.outputStream().use { input.copyTo(it) } }
                                if (android.os.Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Разреши установку для этого приложения, затем нажми APK ещё раз", Toast.LENGTH_LONG).show()
                                } else {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", target)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/vnd.android.package-archive")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Не удалось открыть APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        })
                        Page.Files -> FilesScreen()
                        Page.Settings -> SettingsScreen()
                    }
                }
                if (notice.isNotBlank()) Text(notice, color = Cyan, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (Page) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(Color(0xFF34205F), Color(0xFF111A35), Color(0xFF102B36)))).padding(22.dp)) {
                Column {
                    Text("ТВОЯ ИГРОВАЯ БИБЛИОТЕКА", color = Cyan, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Играй. Настраивай.\nСоздавай.", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Моды, сборки и приложения — в одном месте.", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { onOpen(Page.Mods) }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Открыть каталог") }
                }
            }
        }
        item { SectionTitle("Быстрый доступ", "Всё нужное рядом") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickCard("Моды", "Каталог", Icons.Default.Extension, Modifier.weight(1f)) { onOpen(Page.Mods) }
                QuickCard("Сборки", "Подборки", Icons.Default.Layers, Modifier.weight(1f)) { onOpen(Page.Builds) }
                QuickCard("APK", "Приложения", Icons.Default.Android, Modifier.weight(1f)) { onOpen(Page.Apps) }
            }
        }
        item { SectionTitle("Популярное", "Демо-каталог — добавь свои файлы") }
        items(builds.take(2)) { item -> CatalogCard(item) { onOpen(Page.Builds) } }
        item {
            Text("Версия лаунчера 1.0.0 • Android 8–16", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun QuickCard(title: String, sub: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Panel).clickable(onClick = onClick).padding(12.dp)) {
        Icon(icon, null, tint = Cyan, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(14.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(sub, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun CatalogScreen(title: String, data: List<CatalogItem>, query: String, onQuery: (String) -> Unit, category: String, onCategory: (String) -> Unit) {
    val categories = listOf("Все") + data.map { it.category }.distinct()
    val filtered = data.filter { (category == "Все" || it.category == category) && (it.title + it.description + it.category).contains(query, true) }
    Column(Modifier.fillMaxSize()) {
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), placeholder = { Text("Поиск...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = Panel))
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { c -> FilterChip(selected = category == c, onClick = { onCategory(c) }, label = { Text(c) }, modifier = Modifier.padding(start = if (c == categories.first()) 18.dp else 0.dp)) }
        }
        if (filtered.isEmpty()) EmptyState("Ничего не найдено", "Попробуй изменить запрос")
        else LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { item -> CatalogCard(item) { } }
        }
    }
}

@Composable
private fun CatalogCard(item: CatalogItem, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel).clickable(onClick = onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Purple.copy(alpha = .55f), Cyan.copy(alpha = .25f)))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Extension, null, tint = Color.White, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title.trim(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(item.description, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp)); Text("${item.category}  •  ${item.version}", color = Cyan, fontSize = 10.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Muted)
    }
}

@Composable
private fun ApkScreen(onInstall: (String) -> Unit) {
    val context = LocalContext.current
    val apkNames = remember { context.assets.list("apks")?.filter { it.endsWith(".apk", true) } ?: emptyList() }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Приложения APK", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("APK, добавленные разработчиком в assets/apks", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp, bottom = 15.dp))
        if (apkNames.isEmpty()) EmptyState("Пока нет APK-файлов", "Добавь свои APK в app/src/main/assets/apks и пересобери приложение")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(apkNames) { name ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, null, tint = Cyan, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp)); Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Button(onClick = { onInstall(name) }, colors = ButtonDefaults.buttonColors(containerColor = Purple), contentPadding = PaddingValues(horizontal = 12.dp)) { Text("Установить") }
                }
            }
        }
    }
}

@Composable
private fun FilesScreen() {
    val context = LocalContext.current
    val files = remember { context.getExternalFilesDir("Download")?.listFiles()?.toList() ?: emptyList() }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Мои файлы", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Локальные загрузки приложения", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp, bottom = 15.dp))
        if (files.isEmpty()) EmptyState("Загрузок пока нет", "Здесь будут отображаться файлы, сохранённые лаунчером")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(files) { file -> Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InsertDriveFile, null, tint = Cyan); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(file.name, maxLines = 1); Text("${file.length() / 1024} КБ", color = Muted, fontSize = 10.sp) }
            } }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Настройки", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        SettingRow("Название", "VELTRIX Launcher")
        SettingRow("Версия", "1.0.0")
        SettingRow("Оформление", "Тёмная тема")
        SettingRow("Совместимость", "Android 8.0 и новее")
        SettingRow("Хранилище APK", "assets/apks")
        Text("Важно: устанавливай только APK из источников, которым доверяешь. Android может запросить отдельное разрешение на установку.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(15.dp)).padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Muted); Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 11.sp) }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Inventory2, null, tint = Purple, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    }
}
