package com.triangles.imoves.features.movies.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.triangles.domain.features.moves.model.MovieDetails
import com.triangles.imoves.R
import com.triangles.imoves.features.common.MoviePoster
import com.triangles.imoves.features.common.RatingBadge

@Composable
fun MovieSummary(
    details: MovieDetails,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            MoviePoster(
                imageUrl = details.posterImage,
                contentDescription = stringResource(R.string.movie_poster, details.title),
                modifier = Modifier
                    .width(112.dp)
                    .aspectRatio(2f / 3f),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = details.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            details.releaseYear()?.let { year ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = year,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (details.voteAverage > 0.0) {
                    RatingBadge(rating = details.voteAverage)
                } else {
                    Text(
                        text = stringResource(R.string.not_rated),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (details.voteCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.votes_count,
                        details.voteCount,
                        details.voteCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (details.originalTitle.isNotBlank() && details.originalTitle != details.title) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.original_title, details.originalTitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun MovieDetails.releaseYear(): String? =
    releaseDate.takeIf { it.length >= 4 }?.take(4)
