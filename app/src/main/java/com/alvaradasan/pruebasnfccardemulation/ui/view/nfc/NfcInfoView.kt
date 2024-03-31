package com.alvaradasan.pruebasnfccardemulation.ui.view.nfc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun NfcInfoView(info: String?) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { NfcInfoViewTopBar() },
        bottomBar = { NfcInfoViewBottomBar() }
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
            info?.let { itInfo ->
                if(itInfo.isNotEmpty()){
                    val split = itInfo.split(";")
                    split.forEach { elem ->
                        if(elem.isNotEmpty()) Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            text = elem
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NfcInfoViewTopBar() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        text = "NFC - Info"
    )
}

@Composable
private fun NfcInfoViewBottomBar() {

}

@Composable
@Preview
private fun NfcInfoViewPreview() {
    PruebasNFCCardEmulationTheme {
        NfcInfoView(null)
    }
}