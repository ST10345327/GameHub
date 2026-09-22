package com.gamehub.app.ui.search

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamehub.app.R
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GenreFilter
import com.gamehub.app.data.model.PlatformFilter
import com.gamehub.app.data.model.ReleasePeriod
import com.gamehub.app.data.model.SortOption
import com.gamehub.app.ui.common.messageRes
import com.gamehub.app.ui.components.FilterPill
import com.gamehub.app.ui.components.GameCard
import com.gamehub.app.ui.components.GenreChip

@Composable
fun SearchScreen(viewModel: SearchViewModel, onGameClick: (Int) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            SearchField(value = state.query, onValueChange = viewModel::onQueryChange, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (state.filters.activeCount > 0) colors.primary else colors.surfaceVariant)
                    .clickable(onClick = viewModel::openFilterSheet),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = stringResource(R.string.search_filters),
                    tint = if (state.filters.activeCount > 0) colors.onPrimary else colors.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            state.query.isBlank() && state.filters.activeCount == 0 -> GenreBrowseSection(onGenreClick = viewModel::onGenreSelected)
            state.isLoading && state.results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            state.error != null && state.results.isEmpty() -> ErrorState(messageRes = state.error!!.messageRes(), onRetry = viewModel::retry)
            state.hasSearched && state.results.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.search_no_results), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            else -> ResultsGrid(games = state.results, onGameClick = onGameClick)
        }
    }

    if (state.showFilterSheet) {
        FilterSheet(state = state, viewModel = viewModel)
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = if (value.isNotEmpty()) {
            { IconButton(onClick = { onValueChange("") }) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_cancel)) } }
        } else null,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colors.primary
        )
    )
}

@Composable
private fun GenreBrowseSection(onGenreClick: (GenreFilter) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.search_browse_genre), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(GenreFilter.entries) { genre -> GenreChip(text = genre.displayName, onClick = { onGenreClick(genre) }) }
    }
}

@Composable
private fun ResultsGrid(games: List<Game>, onGameClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

@Composable
private fun ErrorState(messageRes: Int, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(messageRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(state: SearchUiState, viewModel: SearchViewModel) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = viewModel::closeFilterSheet, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(stringResource(R.string.search_filters), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)

            FilterSection(stringResource(R.string.search_genre)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterPill(text = stringResource(R.string.search_any), selected = state.filters.genre == null, onClick = { viewModel.onGenreSelected(null) }) }
                    items(GenreFilter.entries) { g -> FilterPill(text = g.displayName, selected = state.filters.genre == g, onClick = { viewModel.onGenreSelected(g) }) }
                }
            }
            FilterSection(stringResource(R.string.search_platform)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterPill(text = stringResource(R.string.search_any), selected = state.filters.platform == null, onClick = { viewModel.onPlatformSelected(null) }) }
                    items(PlatformFilter.entries) { p -> FilterPill(text = p.displayName, selected = state.filters.platform == p, onClick = { viewModel.onPlatformSelected(p) }) }
                }
            }
            FilterSection(stringResource(R.string.search_min_rating, state.filters.minRating)) {
                Slider(
                    value = state.filters.minRating.toFloat(),
                    onValueChange = { viewModel.onMinRatingChange(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
            FilterSection(stringResource(R.string.search_release_period)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(text = stringResource(R.string.period_any), selected = state.filters.period == ReleasePeriod.ANY, onClick = { viewModel.onPeriodSelected(ReleasePeriod.ANY) })
                    FilterPill(text = stringResource(R.string.period_last_year), selected = state.filters.period == ReleasePeriod.LAST_YEAR, onClick = { viewModel.onPeriodSelected(ReleasePeriod.LAST_YEAR) })
                    FilterPill(text = stringResource(R.string.period_last_five_years), selected = state.filters.period == ReleasePeriod.LAST_FIVE_YEARS, onClick = { viewModel.onPeriodSelected(ReleasePeriod.LAST_FIVE_YEARS) })
                    FilterPill(text = stringResource(R.string.period_classic), selected = state.filters.period == ReleasePeriod.CLASSIC, onClick = { viewModel.onPeriodSelected(ReleasePeriod.CLASSIC) })
                }
            }
            FilterSection(stringResource(R.string.search_sort_by)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(text = stringResource(R.string.sort_relevance), selected = state.filters.sort == SortOption.RELEVANCE, onClick = { viewModel.onSortSelected(SortOption.RELEVANCE) })
                    FilterPill(text = stringResource(R.string.sort_rating), selected = state.filters.sort == SortOption.RATING, onClick = { viewModel.onSortSelected(SortOption.RATING) })
                    FilterPill(text = stringResource(R.string.sort_release_date), selected = state.filters.sort == SortOption.RELEASE_DATE, onClick = { viewModel.onSortSelected(SortOption.RELEASE_DATE) })
                    FilterPill(text = stringResource(R.string.sort_title), selected = state.filters.sort == SortOption.TITLE, onClick = { viewModel.onSortSelected(SortOption.TITLE) })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = viewModel::clearFilters, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.search_clear_filters)) }
                Button(onClick = viewModel::closeFilterSheet, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.search_apply)) }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}