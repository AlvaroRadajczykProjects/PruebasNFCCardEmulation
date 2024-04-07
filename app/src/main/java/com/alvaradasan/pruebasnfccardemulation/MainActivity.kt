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

/**
 * Actividad principal de la aplicación. Solamente se crean los ViewModels necesarios para poder interactuar con la capa
 * de negocio y la UI de la aplicación (simple implementación del patrón MVVM)
 *
 * @constructor Create empty constructor for MainActivity.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Flujo utilizado para que NfcManager y CardEmulationManager puedan utilizar esta actividad para algunos de sus métodos */
        val flow : Flow<Activity> = flow { emit(this@MainActivity) }
        /** Adaptador con la antena NFC utilizado para buscar dispositivos NFC reales o emulados  */
        val adapter = NfcAdapter.getDefaultAdapter(this)
        /** Booleano atómico utilizado para que los ViewModel utilicen el adaptador y lo mantengan activo un rato después una lectura, sin que se produzcan colisiones */
        val requestedRead = AtomicBoolean(false)

        /** Gestor de NFC utilizado para las herramientas NFC */
        val nfcManager = NfcManager(flow, adapter, requestedRead)
        /** Gestor del lector NFC que soporta el protocolo APDU, para conectarse a uno de estos dispositivos emulados */
        val cardEmulationManager = CardEmulationManager(flow, adapter, requestedRead)

        /** ViewModels creados, cada uno para su UI en específico */
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