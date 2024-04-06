package com.alvaradasan.pruebasnfccardemulation.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme

/**
 * Composables de la ventana principal de esta app
 * Estos composables no tienen niguna implementación de funcionalidad NFC
 */

@Composable
fun MainView(navController: NavController?) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { MainViewTopBar() },
        bottomBar = { MainViewBottomBar() }
    ) { itPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(itPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController?.navigate("${Views.NFC_MAIN}") }) {
                Text("NFC")
            }
            Button(onClick = { navController?.navigate("${Views.CARD_EMULATION_MAIN}") }) {
                Text("Card Emulation")
            }
        }
    }
}

@Composable
private fun MainViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "PruebasNFCCardEmulation"
    )
}

@Composable
private fun MainViewBottomBar() {
}

@Preview
@Composable
private fun MainViewPreview() {
    PruebasNFCCardEmulationTheme {
        MainView(null)
    }
}