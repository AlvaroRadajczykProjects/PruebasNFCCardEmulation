package com.alvaradasan.pruebasnfccardemulation.ui.view.nfc

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
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcRWViewModel

/**
 * Composables de la UI de la ventana de NFC lectura/escritura de datos de la UI
 * Estos composables no tienen niguna implementación de funcionalidad NFC, sólo de llamadas a partir de su UI
 */

@Composable
fun NfcRWView(nfcRWViewModel: NfcRWViewModel, data: String?) {
    var mutableData by remember { mutableStateOf(data ?: "") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { NfcRWViewTopBar() },
        bottomBar = { NfcRWViewBottomBar() }
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
            TextField(
                minLines = 10,
                maxLines = 10,
                value = mutableData,
                onValueChange = { mutableData = it }
            )
            Button(onClick = {
                showDialog = true
                dialogText = "Con NFC activado, acerca el dispositivo NFC al móvil para detectarlo"
                nfcRWViewModel.writeNdefData (mutableData) {
                    showDialog = true
                    dialogText = if(it != NfcManager.ERROR) "Se ha escrito sin problemas" else "Se ha producido un error al intentar escribir en la etiqueta"
                }
            }) {
                Text("Escribir")
            }
        }

        if(showDialog) NfcMainViewDialog(dialogText) {
            showDialog = false
            nfcRWViewModel.stopRead()
        }
    }
}

@Composable
private fun NfcRWViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "NFC Read-Write"
    )
}

@Composable
private fun NfcRWViewBottomBar() {

}

@Composable
@Preview
private fun NfcRWViewPreview() {
    PruebasNFCCardEmulationTheme {
        NfcFindInfoView()
    }
}