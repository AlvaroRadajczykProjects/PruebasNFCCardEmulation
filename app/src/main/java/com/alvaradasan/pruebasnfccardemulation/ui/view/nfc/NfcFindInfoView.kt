package com.alvaradasan.pruebasnfccardemulation.ui.view.nfc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme

/**
 * Composables de la ventana de la UI del modo de búsqueda de toda la información posible en la etiqueta (aún no implementada)
 * Estos composables no tienen niguna implementación de funcionalidad NFC
 */

@Composable
fun NfcFindInfoView() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { NfcFindInfoViewTopBar() },
        bottomBar = { NfcFindInfoViewBottomBar() }
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
                    .fillMaxHeight()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Justify,
                text = "EL objetivo de esta funcionalidad es, para todas las tecnologías disponibles en el dispositivo leído, intentar encontrar la mayor cantidad de información posible. Esto es complicado, es posible que incluso algunas etiquetas no se puedan siquiera reconocer, como es posible que se haya visto en la opción de mostrar info"
            )
            Text(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Justify,
                text = "Esta funcionalidad aún no está implementada"
            )
        }
    }
}

@Composable
private fun NfcFindInfoViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "NFC - Find info"
    )
}

@Composable
private fun NfcFindInfoViewBottomBar() {

}

@Composable
@Preview
private fun NfcFindInfoViewPreview() {
    PruebasNFCCardEmulationTheme {
        NfcFindInfoView()
    }
}