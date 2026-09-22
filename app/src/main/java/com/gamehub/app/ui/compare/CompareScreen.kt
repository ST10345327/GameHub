package com.gamehub.app.ui.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamehub.app.R
import com.gamehub.app.data.model.Game
import com.gamehub.app.data.model.GameDetails
import com.gamehub.app.domain.ComparisonRow
import com.gamehub.app.domain.GameComparison
import com.gamehub.app.domain.Winner
import com.gamehub.app.ui.components.GameCover

@Composable
fun CompareScreen(viewModel: CompareViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = colors.onBackground)
            }
            Text(stringResource(R.string.compare_title), style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompareSlot(
                label = stringResource(R.string.compare_select_game_a), game = state.firstGame?.game,
                isLoading = state.isLoadingFirst, modifier = Modifier.weight(1f)
            )
            CompareSlot(
                label = stringResource(R.string.compare_select_game_b), game = state.secondGame?.game,
                isLoading = state.isLoadingSecond, onClick = viewModel::openPicker,
                onClear = if (state.secondGame != null) viewModel::clearSecondGame else null,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(24.dp))

        val first = state.firstGame
        val second = state.secondGame
        if (first != null && second != null) {
            ComparisonTable(first, second)
        }
    }

    if (state.isPickerOpen) {
        GamePickerSheet(
            query = state.pickerQuery, results = state.pickerResults, isLoading = state.isPickerLoading,
            onQueryChange = viewModel::onPickerQueryChange, onSelect = viewModel::selectSecondGame, onDismiss = viewModel::closePicker
        )
    }
}

@Composable
private fun CompareSlot(
    label: String, game: Game?, isLoading: Boolean, modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null, onClear: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .then(if (onClick != null && game == null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            game != null -> Box(Modifier.fillMaxSize()) {
                GameCover(url = game.coverUrl, modifier = Modifier.fillMaxSize())
                Text(
                    text = game.name, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 2,
                    modifier = Modifier.align(Alignment.BottomStart).background(Color.Black.copy(alpha = 0.55f)).fillMaxWidth().padding(8.dp)
                )
                if (onClear != null) {
                    IconButton(onClick = onClear, modifier = Modifier.align(Alignment.TopEnd).size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_remove), tint = Color.White)
                    }
                }
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = colors.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ComparisonTable(first: GameDetails, second: GameDetails) {
    val colors = MaterialTheme.colorScheme
    val rows = GameComparison.compare(first, second)
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.surfaceVariant).padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.first ?: "-", style = MaterialTheme.typography.bodyMedium,
                    color = if (row.winner == Winner.FIRST) colors.primary else colors.onSurface,
                    fontWeight = if (row.winner == Winner.FIRST) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(row.row.labelRes()), style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant, modifier = Modifier.width(90.dp)
                )
                Text(
                    text = row.second ?: "-", style = MaterialTheme.typography.bodyMedium,
                    color = if (row.winner == Winner.SECOND) colors.primary else colors.onSurface,
                    fontWeight = if (row.winner == Winner.SECOND) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun ComparisonRow.labelRes(): Int = when (this) {
    ComparisonRow.RATING -> R.string.compare_row_rating
    ComparisonRow.RELEASE_DATE -> R.string.compare_row_release_date
    ComparisonRow.GENRE -> R.string.compare_row_genre
    ComparisonRow.PLATFORMS -> R.string.compare_row_platforms
    ComparisonRow.DEVELOPER -> R.string.compare_row_developer
    ComparisonRow.PUBLISHER -> R.string.compare_row_publisher
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamePickerSheet(
    query: String, results: List<Game>, isLoading: Boolean,
    onQueryChange: (String) -> Unit, onSelect: (Game) -> Unit, onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(), containerColor = colors.surface) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            TextField(
                value = query, onValueChange = onQueryChange, singleLine = true,
                placeholder = { Text(stringResource(R.string.compare_pick_game)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant, unfocusedContainerColor = colors.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(results, key = { it.id }) { game ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(game) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(width = 44.dp, height = 56.dp).clip(RoundedCornerShape(8.dp))) {
                                GameCover(url = game.coverUrl, modifier = Modifier.fillMaxSize())
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(game.name, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}