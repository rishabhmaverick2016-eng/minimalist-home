package com.minimalist.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalist.launcher.data.AppInfo
import com.minimalist.launcher.ui.theme.getThemePalette
import com.minimalist.launcher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val themeName by viewModel.currentTheme.collectAsState()
    val palette = getThemePalette(themeName)
    val allApps by viewModel.allApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fontSizePref by viewModel.fontSize.collectAsState()

    val itemFontSize = when (fontSizePref) {
        "SMALL" -> 16.sp
        "LARGE" -> 22.sp
        else -> 19.sp
    }

    // Filter apps: exclude hidden apps, and filter by search query
    val visibleApps = allApps
        .filter { !it.isHidden }
        .filter {
            if (searchQuery.isBlank()) true
            else it.displayName.lowercase(Locale.getDefault())
                .contains(searchQuery.lowercase(Locale.getDefault()))
        }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val alphabet = ('A'..'Z').toList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 8.dp, top = 20.dp, bottom = 12.dp)
        ) {
            // SEARCH BAR & BACK BUTTON
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.openScreen("HOME") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = palette.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(palette.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "search apps...",
                                    color = palette.textSecondary,
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = palette.textPrimary,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(palette.textPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        // If only 1 app matches, auto launch on enter
                                        if (visibleApps.size == 1) {
                                            viewModel.onAppClick(visibleApps.first())
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // APP LIST + ALPHABET FAST-INDEX SCRUBBER
            Row(modifier = Modifier.fillMaxSize()) {
                // Apps List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (visibleApps.isEmpty()) {
                        item {
                            Text(
                                text = "no apps found",
                                color = palette.textSecondary,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                    } else {
                        items(visibleApps, key = { it.packageName }) { app ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { viewModel.onAppClick(app) },
                                        onLongClick = { viewModel.openContextMenu(app) }
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = app.displayName,
                                    color = palette.textPrimary,
                                    fontSize = itemFontSize,
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.5.sp
                                )
                                if (app.isFavorite) {
                                    Text(
                                        text = "  ★",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                                if (app.hasMindfulDelay) {
                                    Text(
                                        text = "  ⏳",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Alphabet Fast Scrubber on Right
                if (searchQuery.isEmpty() && visibleApps.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                    ) {
                        alphabet.forEach { letter ->
                            Text(
                                text = letter.toString(),
                                color = palette.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        val targetIndex = visibleApps.indexOfFirst {
                                            it.displayName.startsWith(letter, ignoreCase = true)
                                        }
                                        if (targetIndex >= 0) {
                                            scope.launch {
                                                listState.scrollToItem(targetIndex)
                                            }
                                        }
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
