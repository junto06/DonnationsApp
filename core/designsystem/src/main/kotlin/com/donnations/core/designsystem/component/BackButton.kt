package com.donnations.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donnations.core.designsystem.icon.ArrowBack

@Composable
fun BackButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(imageVector = ArrowBack, contentDescription = contentDescription)
    }
}
