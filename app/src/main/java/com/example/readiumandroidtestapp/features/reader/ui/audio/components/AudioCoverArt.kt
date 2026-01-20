package com.example.readiumandroidtestapp.features.reader.ui.audio.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import java.io.File

@Composable
fun AudioCoverArt(
    book: Book,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = book.cover?.let { File(it) },
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth(fraction = 0.8f)
            .aspectRatio(ratio = 1f)
            .clip(shape = RoundedCornerShape(size = 32.dp)),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.book_2),
    )
}
