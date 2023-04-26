package cc.rigoligo.jlusmartcard.ui.comp

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cc.rigoligo.jlusmartcard.R

@Composable
fun AppBannerLogo(modifier: Modifier) {
    Image(
        modifier = modifier.fillMaxWidth(),
        painter = painterResource(id = R.drawable.app_banner),
        contentDescription = stringResource(id = R.string.cdes_app_banner),
        colorFilter = ColorFilter.tint(colorResource(id = if (isSystemInDarkTheme()) R.color.white else R.color.jlu_blue)),
        contentScale = ContentScale.FillWidth
    )
}