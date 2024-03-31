package com.alvaradasan.pruebasnfccardemulation.ui.view.nfc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme
import com.alvaradasan.pruebasnfccardemulation.ui.view.Views
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModel

@Composable
fun NfcMainView(nfcMainViewModel: NfcMainViewModel, navController: NavController?) {
    val uiState by nfcMainViewModel.uiState.collectAsStateWithLifecycle()
    val dialogText = uiState.dialogText

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showAlertDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { NfcMainViewTopBar() },
        bottomBar = { NfcMainViewBottomBar() }
    ) { itPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(itPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                showDialog = true
                nfcMainViewModel.updateUiState { itUi ->
                    itUi.copy(dialogText = "Con NFC activado, acerca el dispositivo NFC al móvil para detectarlo")
                }
                nfcMainViewModel.getInfoResult {
                    if(it == NfcManager.ERROR) {
                        nfcMainViewModel.updateUiState { itUi ->
                            itUi.copy(dialogText = "Se ha producido un error al intentar leer la información de la etiqueta")
                        }
                    } else {
                        showDialog = false
                        navController?.navigate("${Views.NFC_INFO}/${it}")
                    }
                }
            }) {
                Text("Obtener Info")
            }

            Button(onClick = {
                navController?.navigate("${Views.NFC_FIND_INFO}")
            }) {
                Text("Buscar Info y Datos")
            }

            Button(onClick = {
                showDialog = true
                showDialog = true
                nfcMainViewModel.updateUiState { itUi ->
                    itUi.copy(dialogText = "Con NFC activado, acerca el dispositivo NFC al móvil para detectarlo")
                }
                nfcMainViewModel.readNdefData {
                    if(it == NfcManager.ERROR) {
                        nfcMainViewModel.updateUiState { itUi ->
                            itUi.copy(dialogText = "Se ha producido un error al intentar leer el dato en la tarjeta (si soporta android.nfc.tech.NdefFormatable, ha sido un error de la operación)")
                        }
                    } else {
                        showDialog = false
                        if(it.isEmpty()) navController?.navigate("${Views.NFC_NDEF_READ}")
                        else navController?.navigate("${Views.NFC_NDEF_READ}/${it}")
                    }
                }
            }) {
                Text("Leer NDEF")
            }

            Button(onClick = {
                showAlertDialog = true
            }) {
                Text("Formatear a NDEF")
            }
        }

        if(showDialog) NfcMainViewDialog (dialogText) {
            showDialog = false
            nfcMainViewModel.stopRead()
        }

        if(showAlertDialog) NfcMainViewAlertDialog (
            onAccept = {
                showAlertDialog = false
                showDialog = true
                nfcMainViewModel.updateUiState { itUi ->
                    itUi.copy(dialogText = "Con NFC activado, acerca el dispositivo NFC al móvil, y no lo separes hasta que se muestre un mensaje, para FORMATEARLO SIN PROBLEMAS. La etiqueta debe soportar la tecnología android.nfc.tech.NdefFormatable")
                }
                nfcMainViewModel.formatToNdef {
                    if(it == NfcManager.ERROR) {
                        nfcMainViewModel.updateUiState { itUi ->
                            itUi.copy(dialogText = "Se ha producido un error a la hora de formatear la etiqueta a formato Ndef")
                        }
                    } else {
                        nfcMainViewModel.updateUiState { itUi ->
                            itUi.copy(dialogText = "La etiqueta se ha formateado al formato Ndef exitosamente")
                        }
                    }
                }
            }
        ) {
            showAlertDialog = false
        }
    }
}

@Composable
private fun NfcMainViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        text = "NFC"
    )
}

@Composable
private fun NfcMainViewBottomBar() {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcMainViewDialog(text : String, onDismiss : () -> Unit = {}) {
    AlertDialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .wrapContentSize()
        ) {
            Box (
                modifier = Modifier.
                padding(16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = text,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcMainViewAlertDialog(onAccept : () -> Unit = {}, onDismiss : () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize()
        ) {
            Column (
                modifier = Modifier.
                    padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "AVISO: Esta acción borrará los datos de tu tarjeta y la dejará en un estado posiblemente no recuperable (imagina qué podría pasar con el bono transporte...). ¿Estás seguro de que quieres continuar?",
                    textAlign = TextAlign.Center
                )
                Button(onClick = { onAccept() }) {
                    Text("Si")
                }
            }
        }
    }
}

@Preview
@Composable
private fun NfcMainViewPreview() {
    PruebasNFCCardEmulationTheme {
        NfcMainView(NfcMainViewModel(null), null)
    }
}