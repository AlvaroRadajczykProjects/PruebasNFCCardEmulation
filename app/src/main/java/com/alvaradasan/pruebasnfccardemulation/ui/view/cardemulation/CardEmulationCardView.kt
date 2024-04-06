package com.alvaradasan.pruebasnfccardemulation.ui.view.cardemulation

import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcMainViewDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaradasan.pruebasnfccardemulation.MyHostApduService
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme

/**
 * Composables de la ventana de la UI de la ventana modo emulacion de tarjeta
 * Estos composables no tienen niguna implementación de funcionalidad NFC/APDU
 */

@Composable
fun CardEmulationCardView() {
    var mutableData by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { CardEmulationCardViewTopBar() },
        bottomBar = { CardEmulationCardViewBottomBar() }
    ) { itPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(itPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Justify,
                text = "Esta aplicación, con estar abierta, ya se mantiene a la espera de que un dispositivo que actúe como lector (antena) se "
                + "acerque lo suficiente a este dispositivo para inciar la comunicación mediante el protocolo APDU. Se ha implementado un ejemplo "
                + "Simple en el que se envía un comando APDU sencillo y el único propósito que tiene es iniciar la comunicación. Prueba a modificar "
                + "el contenido del cuadro de texto, y acercar otro dispositivo que tenga activo el modo de lector antena NFC. Cuando los dispositivos "
                + "entren en contacto, este dispositivo que actúa como tarjeta emulada enviará el mensaje del cuadro de texto al dispositivo lector"
            )
            TextField(
                minLines = 9,
                maxLines = 9,
                value = mutableData,
                onValueChange = {
                    mutableData = it
                    MyHostApduService.sendingContent = it
                }
            )
        }

        if(showDialog) NfcMainViewDialog(dialogText) {
            showDialog = false
        }
    }
}

@Composable
private fun CardEmulationCardViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "Card Emulation - Card"
    )
}

@Composable
private fun CardEmulationCardViewBottomBar() {

}

@Composable
@Preview
private fun CardEmulationCardViewPreview() {
    PruebasNFCCardEmulationTheme {
        CardEmulationCardView()
    }
}