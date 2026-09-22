package com.gamehub.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamehub.app.R
import com.gamehub.app.data.model.Game
import com.gamehub.app.ui.components.GameCard
import com.gamehub.app.ui.components.GameCardPlaceholder
import com.gamehub.app.ui.components.SectionTitleRow
import com.gamehub.app.ui.theme.GameHubRedDeep

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    userName: String,
    onSearchClick: () -> Unit,
    onSurpriseMeClick: () -> Unit,
    onGameClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeHeader(userName = userName, onNotificationsClick = onNotificationsClick, modifier = Modifier.padding(horizontal = 20.dp))
        SearchBarPlaceholder(onClick = onSearchClick, modifier = Modifier.padding(horizontal = 20.dp))
        SurpriseMeBanner(onClick = onSurpriseMeClick, modifier = Modifier.padding(horizontal = 20.dp))

        GameRowSection(
            title = "${stringResource(R.string.home_trending)} \uD83D\uDD25",
            games = state.trending,
            isLoading = state.isLoading,
            onGameClick = onGameClick
        )
        GameRowSection(
            title = stringResource(R.string.home_popular),
            games = state.popular,
            isLoading = state.isLoading,
            onGameClick = onGameClick
        )
    }
}

@Composable
private fun HomeHeader(userName: String, onNotificationsClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = "${stringResource(R.string.home_welcome)} \uD83D\uDC4B", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            if (userName.isNotBlank()) {
                Text(text = userName, style = MaterialTheme.typography.headlineMedium, color = colors.onBackground)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.surfaceVariant).clickable(onClick = onNotificationsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = colors.onSurface)
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(8.dp).clip(CircleShape).background(colors.primary))
            }
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
                Text(text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "G", style = MaterialTheme.typography.titleMedium, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun SearchBarPlaceholder(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(text = stringResource(R.string.home_search_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun SurpriseMeBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(colors.primary, GameHubRedDeep)))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.VideogameAsset, contentDescription = null, tint = Color.White) }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.home_surprise_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = stringResource(R.string.home_surprise_subtitle), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
        Icon(Icons.Filled.Casino, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun GameRowSection(title: String, games: List<Game>, isLoading: Boolean, onGameClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitleRow(title = title, modifier = Modifier.padding(horizontal = 20.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isLoading) {
                items(3) { GameCardPlaceholder() }
            } else {
                items(games, key = { it.id }) { game ->
                    GameCard(
                        title = game.name,
                        subtitle = listOfNotNull(game.releaseYear?.toString(), game.genres.firstOrNull()).joinToString(" \u00B7 "),
                        coverUrl = game.coverUrl,
                        rating = game.rating,
                        onClick = { onGameClick(game.id) }
                    )
                }
            }
        }
    }
}