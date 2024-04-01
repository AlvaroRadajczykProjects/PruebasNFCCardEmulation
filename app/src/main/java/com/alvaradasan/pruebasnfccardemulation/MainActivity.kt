package com.alvaradasan.pruebasnfccardemulation

import android.app.Activity
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.alvaradasan.pruebasnfccardemulation.manager.CardEmulationManager
import com.alvaradasan.pruebasnfccardemulation.manager.NfcManager
import com.alvaradasan.pruebasnfccardemulation.ui.theme.PruebasNFCCardEmulationTheme
import com.alvaradasan.pruebasnfccardemulation.ui.view.NavigationView
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation.CardEmulationAntennaViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.cardemulation.CardEmulationAntennaViewModelFactory
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcMainViewModelFactory
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcRWViewModel
import com.alvaradasan.pruebasnfccardemulation.ui.viewmodel.nfc.NfcRWViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val flow : Flow<Activity> = flow { emit(this@MainActivity) }
        val adapter = NfcAdapter.getDefaultAdapter(this)
        val requestedRead = AtomicBoolean(false)

        val nfcManager = NfcManager(flow, adapter, requestedRead)
        val cardEmulationManager = CardEmulationManager(flow, adapter, requestedRead)

        val nfcMainViewModel: NfcMainViewModel by viewModels { NfcMainViewModelFactory(nfcManager) }
        val nfcRWViewModel: NfcRWViewModel by viewModels { NfcRWViewModelFactory(nfcManager) }
        val cardEmulationAntennaViewModel : CardEmulationAntennaViewModel by viewModels { CardEmulationAntennaViewModelFactory(cardEmulationManager) }

        setContent {
            PruebasNFCCardEmulationTheme {
                NavigationView(
                    nfcMainViewModel = nfcMainViewModel,
                    nfcRWViewModel = nfcRWViewModel,
                    cardEmulationAntennaViewModel = cardEmulationAntennaViewModel
                )
            }
        }
    }
}