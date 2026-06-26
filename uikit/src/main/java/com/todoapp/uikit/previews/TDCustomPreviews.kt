package com.todoapp.uikit.previews

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TDPreview

@Preview(
    name = "Light (NoBg)",
    showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark (NoBg)",
    showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TDPreviewNoBg

@Preview(
    name = "Light – Wide",
    showBackground = true,
    widthDp = 411,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark – Wide",
    showBackground = true,
    widthDp = 411,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TDPreviewWide

@Preview(
    name = "Light – Dialog",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark – Dialog",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TDPreviewDialog

@Preview(
    name = "Light – Form",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark – Form",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TDPreviewForm
