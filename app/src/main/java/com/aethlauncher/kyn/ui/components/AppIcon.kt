package com.aethlauncher.kyn.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import com.aethlauncher.kyn.data.models.ShortcutRef
import com.aethlauncher.kyn.launcher.resolver.AppIconLoader

/** Ikon aplikasi: ambil dari cache dulu, jika belum ada dimuat di background. */
@Composable
fun AppIcon(
    ref: ShortcutRef,
    size: Dp,
    loader: AppIconLoader,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(ref.key) { mutableStateOf(loader.cached(ref)) }

    LaunchedEffect(ref.key) {
        if (bitmap == null) bitmap = loader.load(ref)
    }

    val current = bitmap
    if (current != null) {
        val image = remember(current) { current.asImageBitmap() }
        Image(bitmap = image, contentDescription = null, modifier = modifier.size(size))
    } else {
        Spacer(modifier.size(size))
    }
}
