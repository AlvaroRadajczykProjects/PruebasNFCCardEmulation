package com.alvaradasan.pruebasnfccardemulation.ui.view.cardemulation

import com.alvaradasan.pruebasnfccardemulation.ui.view.nfc.NfcMainViewDialog
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
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation.CardEmulationAntennaViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcRWViewModel

@Composable
fun CardEmulationAntennaView(cardEmulationAntennaViewModel: CardEmulationAntennaViewModel) {
    var mutableData by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { CardEmulationAntennaViewTopBar() },
        bottomBar = { CardEmulationAntennaViewBottomBar() }
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
            Button(onClick = {
                showDialog = true
                dialogText = "Con NFC activado en ambos móviles, acércalos por su cara trasera"
                cardEmulationAntennaViewModel.establishConnection {
                    dialogText = it
                    showDialog = true
                }
            }) {
                Text("Leer tarjeta emulada")
            }
        }

        if(showDialog) NfcMainViewDialog(dialogText) {
            showDialog = false
            cardEmulationAntennaViewModel.stopRead()
        }
    }
}

@Composable
private fun CardEmulationAntennaViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "Card Emulation - Antenna"
    )
}

@Composable
private fun CardEmulationAntennaViewBottomBar() {
}

@Composable
@Preview
private fun CardEmulationAntennaViewPreview() {
    PruebasNFCCardEmulationTheme {
        CardEmulationAntennaView(CardEmulationAntennaViewModel(null))
    }
}