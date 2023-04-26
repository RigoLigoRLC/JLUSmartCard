package cc.rigoligo.jlusmartcard.ui.comp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import cc.rigoligo.jlusmartcard.BuildConfig
import cc.rigoligo.jlusmartcard.R
import cc.rigoligo.jlusmartcard.session.BalanceDetails
import cc.rigoligo.jlusmartcard.session.MasterSession
import cc.rigoligo.jlusmartcard.ui.activities.LocalNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Composable
fun AppLogoBannerCard() {
    Card(elevation = 8.dp) {
        Column {
            AppBannerLogo(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            )
            Divider()
            Spacer(modifier = Modifier.size(4.dp))
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 0.dp)
            ) {
                Text(
                    text = "${stringResource(id = R.string.about_author_signature)}, v${BuildConfig.VERSION_NAME}",
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.size(6.dp))
        }
    }
}

@Composable
fun WelcomeCard(username: String?, loginState: MasterSession.LoginState) {
    val navController = LocalNavController.current
    val balanceDetails = remember { mutableStateOf(BalanceDetails(0, 0)) }
    val balanceQuerying = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            val loggedIn = loginState == MasterSession.LoginState.LoggedIn
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .height(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = GreetingsWording(username),
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
    }
}

@Composable
fun GreetingsWording(username: String?): String {
    val hour = LocalDateTime.now().hour
    var ret = if (hour < 5 || hour >= 18)
        stringResource(id = R.string.welcome_goodnight)
    else if (hour in 5..11)
        stringResource(id = R.string.welcome_goodmorning)
    else
        stringResource(id = R.string.welcome_goodafternoon)
    ret += if (username.isNullOrEmpty())
        stringResource(id = R.string.welcome_bang)
    else
        stringResource(id = R.string.welcome_comma) + username + stringResource(id = R.string.welcome_bang)
    return ret;
}

@Composable
fun LoginStatusWording(loginState: MasterSession.LoginState): String {
    return when (loginState) {
        MasterSession.LoginState.LoginExpired -> stringResource(id = R.string.welcome_session_invalid)
        MasterSession.LoginState.NoSessionData -> stringResource(id = R.string.welcome_not_logged_in)
        else -> ""
    }
}

@Preview
@Composable
fun CardsPreview() {
    Column(modifier = Modifier.padding(8.dp)) {
        AppLogoBannerCard()
        Spacer(modifier = Modifier.size(8.dp))
        WelcomeCard("RigoLigo", MasterSession.LoginState.NoSessionData)
    }
}