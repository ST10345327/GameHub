package com.gamehub.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gamehub.app.R
import com.gamehub.app.ui.components.ComingSoonCard
import com.gamehub.app.ui.components.SectionHeader

/**
 * Home / Discover screen (Part 1, section 4.3).
 * Phase 3 fills the "Trending now" and "Popular games" rows with IGDB data.
 * [userName] is empty until Phase 2 supplies the logged-in user.
 */
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSurpriseMeClick: () -> Unit,
    userName: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (userName.isNotBlank()) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        SearchBarPlaceholder(onClick = onSearchClick)
        SurpriseMeCard(onClick = onSurpriseMeClick)

        SectionHeader(R.string.home_trending)
        ComingSoonCard()

        SectionHeader(R.string.home_popular)
        ComingSoonCard()
    }
}

/** Looks like a search field but simply opens the Search tab when tapped. */
@Composable
private fun SearchBarPlaceholder(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.home_search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The one strong red block on the screen - Surprise Me is GameHub's signature feature. */
@Composable
private fun SurpriseMeCard(onClick: () -> Unit) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.VideogameAsset,
            contentDescription = null,
            tint = onPrimary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = stringResource(R.string.home_surprise_title),
                style = MaterialTheme.typography.titleMedium,
                color = onPrimary
            )
            Text(
                text = stringResource(R.string.home_surprise_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}