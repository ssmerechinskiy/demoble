package com.example.bletest.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.example.bletest.R

@Composable
fun MyListItem(
    title: String,
    subTitle: String,
    @DrawableRes imageId: Int, onItemClick : () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(LocalSizes.current.paddingMedium)
            .clickable { onItemClick() }
            .semantics {
                contentDescription = title
            }
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(LocalSizes.current.paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(LocalSizes.current.paddingTiny),
        ) {
            Image(
                painterResource(imageId),
                contentDescription = "Device",
                modifier = Modifier
                    .size(LocalSizes.current.listItemIconSize)
                    .clip(CircleShape)
            )
            Column(
                Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ErrorView(
    message : String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(LocalSizes.current.paddingSmall)
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
            .semantics { contentDescription = contentDescriptionErrorView }
    ) {
        Image(
            painterResource(id = R.drawable.error_icon),
            contentDescription = "ErrorView",
            modifier = modifier
                .weight(2f)
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

const val contentDescriptionErrorView = "error view"