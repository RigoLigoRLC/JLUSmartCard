package cc.rigoligo.jlusmartcard.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cc.rigoligo.jlusmartcard.session.MasterSession
import cc.rigoligo.jlusmartcard.ui.screens.LoginScreen
import cc.rigoligo.jlusmartcard.ui.screens.WelcomeScreen
import cc.rigoligo.jlusmartcard.ui.theme.JLUSmartCardTheme
import kotlinx.coroutines.*

// https://stackoverflow.com/a/70616418
val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController found!") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JLUSmartCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
                        MainNavigation()
                    }
                }
            }
        }
        CoroutineScope(Job() + Dispatchers.Main).launch {
            MasterSession.startup()
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = LocalNavController.current;

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen() }
        composable("login") { LoginScreen() }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JLUSmartCardTheme {
        WelcomeScreen()
    }
}