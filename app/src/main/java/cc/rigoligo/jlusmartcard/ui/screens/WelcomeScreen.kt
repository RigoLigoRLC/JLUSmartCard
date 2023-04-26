package cc.rigoligo.jlusmartcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.session.BalanceDetails
import cc.rigoligo.jlusmartcard.session.MasterSession
import cc.rigoligo.jlusmartcard.ui.activities.LocalNavController
import cc.rigoligo.jlusmartcard.ui.comp.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun WelcomeScreen() {
    var sessionStarted by remember { mutableStateOf(false) }
    val sessionState by MasterSession.state._loginState

    val navController = LocalNavController.current
    val balanceDetails = remember { mutableStateOf(BalanceDetails(0, 0)) }
    val balanceQuerying = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showQrCode by remember { mutableStateOf(false) }

    // Session startup (auto login, etc). Use Unit as remembered key to run only once
    LaunchedEffect(Unit) {
        val startupStatus = MasterSession.startup()
        sessionStarted = true;
        if (startupStatus) {
            // Post successful-startup tasks on welcome screen
            balanceQuerying.value = true
            balanceDetails.value =
                MasterSession.queryAccountBalance()
            balanceQuerying.value = false
        }
    }

    Column(
        modifier = Modifier
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App banner card
        AppLogoBannerCard()

        // Welcome card
        AnimatedVisibility(
            visible = sessionStarted,
            enter = slideInVertically() + fadeIn()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(elevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    ) {
                        val loginState = MasterSession.loginState
                        val loggedIn = loginState == MasterSession.LoginState.LoggedIn
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .height(IntrinsicSize.Min),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = GreetingsWording(MasterSession.studentName),
                                style = MaterialTheme.typography.h5
                            )
                            Spacer(modifier = Modifier.size(0.dp))
                            Divider()
                            if (!loggedIn) {
                                Text(text = LoginStatusWording(loginState))
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(text = stringResource(id = R.string.welcome_balance))
                                        Text(
                                            text = "${(balanceDetails.value.DatabaseBalance + balanceDetails.value.UnsettledAmount) / 100.0}",
                                            style = MaterialTheme.typography.h2
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(IntrinsicSize.Max),
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        Text(text = "${stringResource(id = R.string.welcome_db_balance)} ${balanceDetails.value.DatabaseBalance / 100.0}")
                                        Text(text = "${stringResource(id = R.string.welcome_unsettled_balance)} ${balanceDetails.value.UnsettledAmount / 100.0}")
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        withContext(Dispatchers.IO) {
                                                            balanceQuerying.value = true
                                                            balanceDetails.value =
                                                                MasterSession.queryAccountBalance()
                                                            balanceQuerying.value = false
                                                        }
                                                    }
                                                },
                                                enabled = !balanceQuerying.value
                                            ) {
                                                Icon(Icons.Filled.Refresh, null/*TODO*/)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!loggedIn) {
                            Button(
                                onClick = {
                                    navController.navigate("login")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.welcome_login_btn))
                            }
                        }
                    }
                } // Card [WelcomeCard]
            }

        } // AnimatedVisibility (visible = sessionStarted)

        AnimatedVisibility(
            visible = sessionState == MasterSession.LoginState.LoggedIn,
            enter = slideInVertically() + fadeIn()
        ) {
            Card(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(0.dp, 8.dp)
                ) {
                    // Recommended actions
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CreditCard, null)
                            Text(text = stringResource(id = R.string.welcome_recharge))
                        }
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                    IconButton(
                        onClick = {
                            showQrCode = !showQrCode
                        },
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode, null)
                            Text(text = stringResource(id = R.string.welcome_pay_with_qr_code))
                        }
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                    IconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Checklist, null)
                            Text(text = stringResource(id = R.string.welcome_transactions))
                        }
                    }
                }
            }
        }

        // Spinning load animation when starting the session
        AnimatedVisibility(
            visible = !sessionStarted,
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(25.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
        }

        if (showQrCode) {
            PaymentQrCodeDialog {
                showQrCode = !showQrCode
            }
        }
    }
}