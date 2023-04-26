package cc.rigoligo.jlusmartcard.ui.comp

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.network.RequestGetPaymentCode
import cc.rigoligo.jlusmartcard.network.requests.GetPaymentCode
import cc.rigoligo.jlusmartcard.session.MasterSession
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

private enum class PaymentQRCodeState {
    Loaded,
    Loading,
    LoadFailed
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PaymentQrCodeDialog(onDismiss: () -> Unit) {
    var paymentQRCodeState by remember { mutableStateOf(PaymentQRCodeState.Loading) }
    var paymentQRCodeImage by remember { mutableStateOf(ImageBitmap(1, 1)) }
    val paymentCodeList = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    val refreshPaymentQRCode: suspend () -> Unit = refreshPaymentQRCodeLambda@{
        paymentQRCodeState = PaymentQRCodeState.Loading
        if (paymentCodeList.isEmpty()) {
            // Fetch some code first
            val response = GetPaymentCode(
                RequestGetPaymentCode(
                    MasterSession.accountId,
                    MasterSession.sessionKey,
                    MasterSession.deviceUniqueId
                )
            )

            if (response.status.code != 0 || response.codes.isEmpty()) {
                paymentQRCodeState = PaymentQRCodeState.LoadFailed
                Log.e("PaymentQrCodeFetch", response.status.message)
                return@refreshPaymentQRCodeLambda
            } else {
                response.codes.forEach { paymentCodeList.add(it) }
            }
        }

        val code = paymentCodeList.removeFirstOrNull()
        if (code == null) {
            paymentQRCodeState = PaymentQRCodeState.LoadFailed
            Log.e("PaymentQrCodeFetch", "List is empty!")
            return@refreshPaymentQRCodeLambda
        }
        val matx = QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, 400, 400)
        var pixels = IntArray(matx.height * matx.width)

        for (y in 0 until matx.height)
            for (x in 0 until matx.width)
                pixels[y * matx.width + x] = if (matx.get(x, y)) Color.BLACK else Color.WHITE

        val bitmap = Bitmap.createBitmap(matx.width, matx.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matx.width, 0, 0, matx.width, matx.height)

        paymentQRCodeImage = bitmap.asImageBitmap()
        paymentQRCodeState = PaymentQRCodeState.Loaded
    }

    LaunchedEffect(Unit) {
        paymentCodeList.clear()
        refreshPaymentQRCode()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(
                            Icons.Default.Close,
                            stringResource(id = R.string.cdes_welcome_close_qrcode)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // QR Code
                    AnimatedContent(
                        targetState = paymentQRCodeState,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { paymentQRCodeState ->
                        when (paymentQRCodeState) {
                            PaymentQRCodeState.Loaded -> Image(
                                paymentQRCodeImage,
                                stringResource(id = R.string.cdes_welcome_qrcode),
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp)
                            )
                            PaymentQRCodeState.Loading -> CircularProgressIndicator()
                            PaymentQRCodeState.LoadFailed -> Icon(
                                Icons.Default.Warning,
                                stringResource(id = R.string.cdes_welcome_qrcode_failed_to_load)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            refreshPaymentQRCode()
                        }
                    },
                    enabled = paymentQRCodeState != PaymentQRCodeState.Loading
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Text(stringResource(id = R.string.welcome_refresh_qrcode))
                }
            }
        }
    }
}