package com.gamehub.app.ui.surprise

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamehub.app.R
import com.gamehub.app.data.model.GenreFilter
import com.gamehub.app.data.model.PlatformFilter
import com.gamehub.app.ui.common.messageRes
import com.gamehub.app.ui.components.FilterPill
import com.gamehub.app.ui.components.GameCover
import com.gamehub.app.ui.components.RatingBadge

@Composable
fun SurpriseMeScreen(viewModel: SurpriseMeViewModel, onBack: () -> Unit, onViewDetails: (Int) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = colors.onBackground)
            }
            Text(stringResource(R.string.home_surprise_title), style = MaterialTheme.typography.titleLarge, color = colors.onBackground)
        }

        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterPill(text = stringResource(R.string.search_any), selected = state.genre == null, onClick = { viewModel.onGenreSelected(null) }) }
            items(GenreFilter.entries) { g -> FilterPill(text = g.displayName, selected = state.genre == g, onClick = { viewModel.onGenreSelected(g) }) }
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterPill(text = stringResource(R.string.search_any), selected = state.platform == null, onClick = { viewModel.onPlatformSelected(null) }) }
            items(PlatformFilter.entries) { p -> FilterPill(text = p.displayName, selected = state.platform == p, onClick = { viewModel.onPlatformSelected(p) }) }
        }
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator(color = colors.primary)
                state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(state.error!!.messageRes()), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = viewModel::roll) { Text(stringResource(R.string.common_retry)) }
                }
                state.game != null -> {
                    val game = state.game!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            GameCover(url = game.coverUrl, modifier = Modifier.fillMaxWidth().aspectRatio(1.3f).clip(RoundedCornerShape(20.dp)))
                            RatingBadge(rating = game.rating, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(game.name, style = MaterialTheme.typography.headlineMedium, color = colors.onBackground, textAlign = TextAlign.Center)
                        Text(
                            text = listOfNotNull(game.releaseYear?.toString(), game.genres.firstOrNull()).joinToString(" \u00B7 "),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = { onViewDetails(game.id) }, shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text(stringResource(R.string.surprise_view_details)) }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = viewModel::roll, shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text(stringResource(R.string.surprise_try_another)) }
                    }
                }
            }
        }
    }
}