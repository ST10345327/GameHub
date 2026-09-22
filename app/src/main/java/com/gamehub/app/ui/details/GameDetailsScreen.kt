package com.gamehub.app.ui.details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gamehub.app.R
import com.gamehub.app.data.model.LibraryStatus
import com.gamehub.app.data.model.SimilarGame
import com.gamehub.app.ui.common.messageRes
import com.gamehub.app.ui.components.FilterPill
import com.gamehub.app.ui.components.GameCover
import com.gamehub.app.ui.components.RatingBadge
import androidx.compose.material.icons.filled.CompareArrows

@Composable
fun GameDetailsScreen(viewModel: GameDetailsViewModel, onBack: () -> Unit, onCompareClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
            state.details == null -> Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(state.error?.messageRes() ?: R.string.error_unknown),
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = viewModel::retry) { Text(stringResource(R.string.common_retry)) }
            }
            else -> {
                val details = state.details!!
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box {
                        GameCover(url = details.game.coverUrl, modifier = Modifier.fillMaxWidth().aspectRatio(1.4f))
                        RatingBadge(rating = details.game.rating, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
                    }

                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(details.game.name, style = MaterialTheme.typography.headlineMedium, color = colors.onBackground)

                        Text(
                            text = listOfNotNull(
                                details.game.releaseYear?.toString(),
                                details.game.genres.joinToString(", ").ifBlank { null },
                                details.game.platforms.joinToString(", ").ifBlank { null }
                            ).joinToString("  \u2022  "),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant
                        )

                        ActionRow(
                            isFavourite = state.status.isFavourite,
                            isInWishlist = state.status.isInWishlist,
                            libraryStatus = state.status.libraryStatus,
                            isUpdating = state.isUpdating,
                            onToggleFavourite = viewModel::toggleFavourite,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onSetLibraryStatus = viewModel::setLibraryStatus,
                            onCompareClick = onCompareClick
                        )

                        if (!details.description.isNullOrBlank()) {
                            Text(details.description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                        }
                        if (details.developers.isNotEmpty()) InfoRow(stringResource(R.string.details_developer), details.developers.joinToString(", "))
                        if (details.publishers.isNotEmpty()) InfoRow(stringResource(R.string.details_publisher), details.publishers.joinToString(", "))
                        if (details.similarGames.isNotEmpty()) {
                            Text(stringResource(R.string.details_similar_games), style = MaterialTheme.typography.titleMedium, color = colors.onBackground)
                        }
                    }

                    if (details.similarGames.isNotEmpty()) {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(details.similarGames, key = { it.id }) { similar -> SimilarGameCard(similar) }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(12.dp).size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
        }
    }
}

@Composable
private fun ActionRow(
    isFavourite: Boolean,
    isInWishlist: Boolean,
    libraryStatus: LibraryStatus?,
    isUpdating: Boolean,
    onToggleFavourite: () -> Unit,
    onToggleWishlist: () -> Unit,
    onSetLibraryStatus: (LibraryStatus?) -> Unit,
    onCompareClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onToggleFavourite, enabled = !isUpdating) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.details_favourite),
                    tint = if (isFavourite) colors.primary else colors.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleWishlist, enabled = !isUpdating) {
                Icon(
                    imageVector = if (isInWishlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = stringResource(R.string.details_wishlist),
                    tint = if (isInWishlist) colors.primary else colors.onSurfaceVariant
                )
            }
            IconButton(onClick = onCompareClick) {
                Icon(Icons.Filled.CompareArrows, contentDescription = stringResource(R.string.details_compare), tint = colors.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LibraryStatus.entries.forEach { status ->
                FilterPill(
                    text = stringResource(status.labelRes()),
                    selected = libraryStatus == status,
                    onClick = { onSetLibraryStatus(if (libraryStatus == status) null else status) }
                )
            }
        }
    }
}

private fun LibraryStatus.labelRes(): Int = when (this) {
    LibraryStatus.WANT_TO_PLAY -> R.string.library_want_to_play
    LibraryStatus.PLAYING -> R.string.library_playing
    LibraryStatus.COMPLETED -> R.string.library_completed
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SimilarGameCard(game: SimilarGame) {
    Column(modifier = Modifier.width(110.dp)) {
        AsyncImage(
            model = game.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(game.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
    }
}