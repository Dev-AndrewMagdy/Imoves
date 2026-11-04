package com.triangles.imoves.features.movies.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.triangles.domain.features.moves.model.MovieDetails
import com.triangles.imoves.R
import java.util.Locale

@Composable
fun MovieMetadata(
    details: MovieDetails,
    modifier: Modifier = Modifier,
) {
    val notAvailable = stringResource(R.string.not_available)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MetadataItem(
            label = stringResource(R.string.release_date_label),
            value = details.releaseDate.ifBlank { notAvailable },
            modifier = Modifier.weight(1f),
        )
        MetadataItem(
            label = stringResource(R.string.language_label),
            value = details.originalLanguage
                .uppercase(Locale.getDefault())
                .ifBlank { notAvailable },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetadataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
