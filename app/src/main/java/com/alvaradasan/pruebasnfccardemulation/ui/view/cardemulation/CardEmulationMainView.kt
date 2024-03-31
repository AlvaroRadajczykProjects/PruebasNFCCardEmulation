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
import com.alvaradasan.pruebasnfccardemulation.manager.CardEmulationManager
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme
import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcMainViewDialog
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation.CardEmulationMainViewModel

@Composable
fun CardEmulationMainView(cardEmulationViewModel: CardEmulationMainViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Justify,
                text = "Esta aplicación, con estar abierta, ya se mantiene a la espera de que se espere conectar con la tarjeta virtual para"
                + ", en caso de contacto entre un dispositivo que quiere establecer conexión y el dispositivo que simula una tarjeta virtual"
                + ", se inicie un servicio APDU. Este servicio, una vez termina la comunicación, se destruye automáticamente. Si aparece como"
                + "mensaje el número 9000, eso significa que la operación se ha realizado correctamente"
            )
            Button(onClick = {
                showDialog = true
                message = "Acerca el dispositivo con la misma aplicacion abierta para establecer una conexion"
                cardEmulationViewModel.establishConnection {
                    if(it != CardEmulationManager.ERROR) {
                        if(it.isNotEmpty()) message = it
                        else message = "No se han recibido datos aunque no se haya producido un error"
                    } else {
                        message = "Error al intentar establecer la conexion"
                        println("Error al intentar establecer la conexion")
                    }
                }
            }) {
                Text("Establecer conexion")
            }
        }
    }

    if(showDialog) NfcMainViewDialog(message) {
        showDialog = false
        cardEmulationViewModel.stopRead()
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
        CardEmulationMainView(CardEmulationMainViewModel(null))
    }
}