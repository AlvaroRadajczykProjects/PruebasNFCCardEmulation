package com.alvaradasan.pruebasnfccardemulation.ui.view.cardemulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.alvaradasan.pruebasnfccardemulation.ui.view.Views

/**
 * Composables de la ventana de la UI de la ventana principal de emulacion de una tarjeta (o de su lectura con la antena del dispositivo)
 * Estos composables no tienen niguna implementación de funcionalidad NFC/APDU
 */

@Composable
fun CardEmulationMainView(navController : NavController?) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { CardEmulationMainViewTopBar() },
        bottomBar = { CardEmulationMainViewBottomBar() }
    ) { itPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(itPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                navController?.navigate("${Views.CARD_EMULATION_ANTENNA}")
            }) {
                Text("Escuchar con Antena APDU (IsoDep)")
            }
            Button(onClick = {
                navController?.navigate("${Views.CARD_EMULATION_CARD}")
            }) {
                Text("Emular tarjeta APDU")
            }
        }
    }
}

@Composable
private fun CardEmulationMainViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "Card Emulation"
    )
}

@Composable
private fun CardEmulationMainViewBottomBar() {
}

@Composable
@Preview
private fun CardEmulationMainViewPreview() {
    PruebasNFCCardEmulationTheme {
        CardEmulationMainView(null)
    }
}